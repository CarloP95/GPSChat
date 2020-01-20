package com.carlop.gpschat.actors

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{Behavior, PostStop, ActorRef}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Signal

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken

import java.{util => ju}

import com.carlop.gpschat.actors.MsgAnalyzer.ArrivedMsg

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

                    //NOTE: Do not use persistence, since this will not have permissions to write the folder 
                    //lazy val persistence = new MqttDefaultFilePersistence ()
                    //client = new MqttClient (url, MqttClient.generateClientId(), persistence)
                    client = new MqttClient (url, MqttClient.generateClientId(), null)

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