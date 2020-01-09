package com.carlop.gpschat


import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{Behavior, PostStop, ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Signal

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.jdk.CollectionConverters.MapHasAsJava

import com.carlop.gpschat.MQTTActor.MQTTActorBehavior
import com.carlop.gpschat.MsgAnalyzer.MsgAnalyzerBehavior
import com.carlop.gpschat.MongoWriter.MongoWriterBehavior
import com.carlop.gpschat.MongoWriter.MongoCommand
import com.carlop.gpschat.MsgAnalyzer.ArrivedMsg
import com.carlop.gpschat.MQTTActor.MqttCommand
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken

import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.Completed
import org.bson.Document
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import java.{util => ju}


object MongoWriter {

    sealed trait MongoCommand
    final case class ConnectCommand (url: String = "", time: String = new ju.Date toString) extends MongoCommand
    final case class CreateCollectionCommand (collectionName: String) extends MongoCommand
    final case class WriteAnalyzedMsgCommand (topic: String, msg: Map[String, Any], time: String = new ju.Date toString) extends MongoCommand

    def apply(url: String = ""): Behavior[MongoCommand] = 
        Behaviors.setup[MongoCommand] (context => new MongoWriterBehavior (context, url))

    class MongoWriterBehavior(context: ActorContext[MongoCommand], url: String = "") extends AbstractBehavior[MongoCommand] (context) {

        var client: MongoClient = _
        lazy val dbName: String         = "gpschat"
        lazy val collectionName: String = "messages"
        var db: MongoDatabase = _
        var collection: MongoCollection[Document] = _

        lazy val COLLECTION_ALREADY_EXISTS_ERROR_STRING = "NamespaceExists"


        override def onMessage(msg: MongoCommand): Behavior[MongoCommand] = {
            msg match {
                case ConnectCommand(url, time) => 
                    context.log.info(s"Received $msg")
                    // Connect with Mongo
                    System.setProperty("org.mongodb.async.type", "netty")
                    client = MongoClient(if (!(url isEmpty)) url else this.url)
                    db = client.getDatabase(dbName)

                    context.self ! CreateCollectionCommand(collectionName)
                    Behaviors.same

                case CreateCollectionCommand(collectionName) => 
                    db createCollection(collectionName) subscribe( (_ : Completed) => { 
                        context.log.info(s"Created collection $collectionName")
                        collection = db getCollection collectionName
                    }, (e: Throwable) => {
                        if (e.getLocalizedMessage().contains(COLLECTION_ALREADY_EXISTS_ERROR_STRING)) {
                            context.log.info("Collection already exists. Getting from DB")
                            collection = db getCollection collectionName
                        } else {
                            context.log.error(e.getLocalizedMessage())
                        }
                    })

                    Behaviors.same

                case WriteAnalyzedMsgCommand(topic, msg, time) => 

                    db match {
                        case MongoDatabase(_) => 
                            val toInsert : Document = new Document(msg.asJava)
                            context.log.info("Received MSG from Analyzer. Ready to write to DB")
                            collection insertOne toInsert subscribe( (_ : Completed) => {
                                context.log.info(s"A Document has been inserted to DB")
                            }, (e: Throwable) => {
                                context.log.info(s"Something bad happened. ${e.getLocalizedMessage()}")
                            })
                            

                        case null => 
                            context.log.info("DB is still not opened. Re-Scheduling write in 5 seconds")
                            context.system.scheduler.scheduleOnce( java.time.Duration.ofSeconds(5) , new Runnable {
                                override def run(): Unit = {
                                    context.self ! WriteAnalyzedMsgCommand(topic, msg, time)
                                }
                            }, context.executionContext)
                    }

                    Behaviors.same

            }
        }

        override def onSignal: PartialFunction[Signal,Behavior[MongoCommand]] = {
            case PostStop => 
                context.log.info(s"Received PostStop Signal. Terminating ${this.getClass()}")
                client.close()
                this
        }
    }
}


// https://www.zymr.com/geolocation-apis-mongodb/
object MsgAnalyzer {

    sealed trait ArrivedMsg
    final case class MqttNewMsg (topic: String, msg: String, time: String) extends ArrivedMsg

    def apply(mongoWriterRef: ActorRef[MongoCommand]): Behavior[ArrivedMsg] =
        Behaviors.setup[ArrivedMsg] (context => new MsgAnalyzerBehavior (context, mongoWriterRef))
    
    class MsgAnalyzerBehavior (context: ActorContext[ArrivedMsg], mongoWriterRef: ActorRef[MongoCommand]) extends AbstractBehavior[ArrivedMsg] (context) {
        override def onMessage (msg: ArrivedMsg): Behavior[ArrivedMsg] = {
            msg match {
                case MqttNewMsg (topic, msg, time) => 
                    
                    val mapper = new ObjectMapper with ScalaObjectMapper
                    mapper.registerModule(DefaultScalaModule)

                    val parsedMsg = mapper.readValue[Map[String, Any]](msg)

                    var extractedMap = parsedMsg
                    
                    extractedMap foreach ( pair => {
                        println (pair._2.getClass())
                    })

                    context.log.info("Analyzed MQTTMsg. Sending to Writer")
                    mongoWriterRef ! MongoWriter.WriteAnalyzedMsgCommand (topic, extractedMap, time)
                    Behaviors.same
            }
        }

        override def onSignal: PartialFunction[Signal,Behavior[ArrivedMsg]] = {
            case PostStop => 
                context.log.info(s"Received PostStop Signal. Terminating ${this.getClass()}")
                this
        }
    }
}




object MQTTActor {

    sealed trait MqttCommand
    final case class StartCommand (topic: String, url: String) extends MqttCommand
    final case class ConnectCommand (url: String, topic: String) extends MqttCommand
    final case class SubscribeCommand (topic: String) extends MqttCommand

    def apply(msgAnalyzerRef: ActorRef[ArrivedMsg]): Behavior[MqttCommand] =
        Behaviors.setup[MqttCommand] (context => new MQTTActorBehavior (context, msgAnalyzerRef))

    class MQTTActorBehavior (context: ActorContext[MqttCommand], msgAnalyzerRef: ActorRef[ArrivedMsg]) extends AbstractBehavior[MqttCommand] (context) {

        var client: MqttClient = _
        var topicFilters: Array[String] = Array()

        override def onMessage (msg: MqttCommand): Behavior[MqttCommand] = {
            msg match {
                case StartCommand (topic, url) =>
                    context.log.info (s"Received Start Command. Starting normal behavior to connect & subscribe to $url with topic $topic")

                    lazy val persistence = new MqttDefaultFilePersistence ()
                    client = new MqttClient (url, MqttClient.generateClientId(), persistence)

                    val handlers = new MqttCallback {
                        override def connectionLost(cause: Throwable): Unit = {
                            topicFilters = Array()
                            context.self ! ConnectCommand (url, topic)
                        }

                        override def messageArrived(topic: String, message: MqttMessage): Unit = {
                            context.log.info("MQTTMsg arrived from network")
                            msgAnalyzerRef ! MsgAnalyzer.MqttNewMsg (topic, message.getPayload.map(_.toChar).mkString, new ju.Date toString)
                        }

                        override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
                            context.log.error("At the current moment, there is no reason for this Actor to push MQTTMessages to the Broker.")
                        }
                    }

                    client setCallback(handlers)
                    context.self ! ConnectCommand (url, topic)                  
                    Behaviors.same
                    
                case ConnectCommand(url, topic) =>
                    context.log.info(s"Received Connect Command. Connecting to URL $url")

                    Try {
                        client connect
                    } match {
                        case Failure(exception) => 
                            context.log.error(s"An exception occurred while connecting with mqtt client: ${exception.getLocalizedMessage()}")
                            context.system.scheduler.scheduleOnce( java.time.Duration.ofSeconds(5) , new Runnable {
                                override def run(): Unit = {
                                    context.self ! ConnectCommand(url, topic)
                                }
                            }, context.executionContext)
                        case Success(_) => context.self ! SubscribeCommand (topic)
                    }
                    
                    Behaviors.same

                case SubscribeCommand(topic) =>
                    context.log.info(s"Received Subscribe Command. Subscribing to topic $topic")

                    Try {
                        if (client isConnected) {
                            client subscribe topic
                        } else {
                            Failure(new Exception("Client is not connected"))
                        }
                    } match {
                        case Failure(exception) => context.log.error(s"An exception occurred while subscribing to topic $topic: ${exception getLocalizedMessage}")
                        case Success(_) => 
                            context.log.info(s"Successfully connected to mqtt broker and subscribed to topic $topic")
                            topicFilters :+ topic
                    }
                    Behaviors.same

            }    
        }
        override def onSignal: PartialFunction[Signal,Behavior[MqttCommand]] = {
            case PostStop => 
                context.log.info (s"Terminating ${this.getClass()}")
                context.log.info (s"Topic Filters to unsubscribe: ${topicFilters mkString}")
                client unsubscribe topicFilters
                Try {
                    client setCallback(null) //Trying to avoid MQTTException but does not work
                    client.disconnect()
                    client close
                } match {
                    case Failure(exception) => context.log.error(s"An exception occurred while disconnecting: ${exception getLocalizedMessage}")
                    case Success(_) => context.log.error(s"Graceful disconnection happened.")
                }
                this
        }


    }
}

object MainGuardian {
    
    sealed trait MainCommand
    final case class StopCommand (time: String) extends MainCommand
    final case class StartCommand (time: String) extends MainCommand

    def apply(): Behavior[MainCommand] =
        Behaviors.setup[MainCommand] (context => new MainGuardianBehavior (context))

    class MainGuardianBehavior (context: ActorContext[MainCommand]) extends AbstractBehavior[MainCommand] (context) {

        override def onMessage(msg: MainCommand): Behavior[MainCommand] = {
            msg match {
                case StartCommand(time) => 
                    context.log.info(s"Started MainGuardian at time $time.")

                    val user         = "yourUsernameInMongo" //TODO: Must use your username.
                    val pwd          = "yourClusterPwd"      //TODO: Must use your password
                    val mMongoUri    = s"mongodb+srv://$user:$pwd@gpschatcluster-o520q.azure.mongodb.net/test?retryWrites=true&w=majority"

                    val mMongoWriterActorRef = context.spawn(MongoWriter(mMongoUri), "MongoWriter")
                    val mMsgAnalyzerActorRef = context.spawn(MsgAnalyzer(mMongoWriterActorRef), "MessageAnalyzer")
                    val mMQTTActorRef = context.spawn(MQTTActor(mMsgAnalyzerActorRef), "MQTTActor")

                    val newTime      = new ju.Date toString
                    val topic        = "topics/loc/#"
                    val mMQTTUrl     = "tcp://localhost:1883" //TODO: Your MQTT Broker URL.
                    

                    mMQTTActorRef ! MQTTActor.StartCommand (topic, mMQTTUrl)
                    mMongoWriterActorRef ! MongoWriter.ConnectCommand ()

                    Behaviors.same

                case StopCommand(time) => 
                    context.log.info(s"Received Stop command at time $time. Stopping..")

                    Behaviors.stopped
            }
        }
    }
}


object MQTTMongoConnector extends App {
    
    println("Started MQTT To Mongo Writer. Initializing Actors...")
    val actorSystemRef = ActorSystem (MainGuardian(), "MainGuardian")

    actorSystemRef ! MainGuardian.StartCommand(new ju.Date toString)    
    Thread.sleep( 3 * 60 * 1000)
    actorSystemRef ! MainGuardian.StopCommand(new ju.Date toString)

}