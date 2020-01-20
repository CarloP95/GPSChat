package com.carlop.gpschat.actors

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{Behavior, PostStop, ActorRef}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Signal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import com.carlop.gpschat.actors.MongoWriter.MongoCommand

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