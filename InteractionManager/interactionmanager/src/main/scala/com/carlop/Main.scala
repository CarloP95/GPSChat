package com.carlop

import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import com.carlop.resources.MessageResource
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapObserveRelation
import org.eclipse.californium.core.coap.MediaTypeRegistry
import com.carlop.coap.InteractionManagerServer
import com.carlop.resources.MessagePrefixResource

import org.slf4j.{LoggerFactory, Logger}
import com.carlop.mongo.MongoBootstrapHandler
import org.mongodb.scala.{MongoCollection, Document}

object Main extends App {
    val log : Logger = LoggerFactory.getLogger("Main App Logger")
    log.info("Connecting with MongoDB")
    
    val result: MongoBootstrapHandler.ReturnFromCreation = MongoBootstrapHandler("mongodb://localhost", "gpschat")

    log.info("Now Creating CoapResources...")

    val msgResource = new MessageResource("message", true, result.msgCollection)
    val msgPrefixResource = new MessagePrefixResource("messagePrefix", true, result.msgPrefixCollection)

    log.info("Starting Coap Server...")

    val coapServer = InteractionManagerServer()
    
    log.info("Done!")
    /*val rel :CoapObserveRelation = client.observe(new CoapHandler {
        override def onError(): Unit = {
            
        }

        override def onLoad(response: CoapResponse): Unit = {
            println(response.getResponseText())
        }
    })

    val anotherClient = new CoapClient(s"$baseEndpoint/messages")
    res = anotherClient.put("Just a message".getBytes, MediaTypeRegistry.TEXT_PLAIN)

    println(rel.isCanceled())
    */

}