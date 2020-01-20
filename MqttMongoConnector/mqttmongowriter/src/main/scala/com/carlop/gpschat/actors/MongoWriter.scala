package com.carlop.gpschat.actors

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.jdk.CollectionConverters.MapHasAsJava

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{Behavior, PostStop, ActorRef}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Signal

import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.Completed
import org.bson.Document

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