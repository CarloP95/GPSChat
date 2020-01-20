package com.carlop.gpschat


import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{Behavior, PostStop, ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.SupervisorStrategy

import scala.language.postfixOps

import java.{util => ju}

import com.carlop.gpschat.actors.{MQTTActor, MsgAnalyzer, MongoWriter}


object MainGuardian {
    
    sealed trait MainCommand
    final case class StopCommand (time: String) extends MainCommand
    final case class StartCommand (time: String) extends MainCommand

    def apply(): Behavior[MainCommand] =
        //Behaviors.supervise[MainCommand] {
            Behaviors.setup[MainCommand] (context => new MainGuardianBehavior (context))
        //}.onFailure(SupervisorStrategy.resume)

    class MainGuardianBehavior (context: ActorContext[MainCommand]) extends AbstractBehavior[MainCommand] (context) {

        override def onMessage(msg: MainCommand): Behavior[MainCommand] = {
            msg match {
                case StartCommand(time) => 
                    context.log.info(s"Started MainGuardian at time $time.")

                    val user         = "YourUsernameForMongoCluster"
                    val pwd          = "YourPasswordForMongoUser"

                    val mMongoUri    = s"mongodb+srv://$user:$pwd@YourConnectionString"

                    val mMongoWriterActorRef = context.spawn(MongoWriter(mMongoUri), "MongoWriter")
                    val mMsgAnalyzerActorRef = context.spawn(MsgAnalyzer(mMongoWriterActorRef), "MessageAnalyzer")
                    val mMQTTActorRef = context.spawn(MQTTActor(mMsgAnalyzerActorRef), "MQTTActor")

                    val newTime      = new ju.Date toString
                    val topic        = "TopicToWhichYouWantToSubscribe"
                    val mMQTTUrl     = "MQTT_URL_AND_PORT"
                    

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