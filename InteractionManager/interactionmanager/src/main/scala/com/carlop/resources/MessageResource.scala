package com.carlop.resources

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP.ResponseCode._
import org.eclipse.californium.core.server.resources.CoapExchange
import org.eclipse.californium.core.observe.ObserveRelation
import org.mongodb.scala.{MongoCollection, Document}


class MessageResource(name: String, visible: Boolean, collection: MongoCollection[Document]) extends CoapResource(name: String, visible: Boolean) {

    var message: String = "Just a mess"
    setObservable (true)

    override def handleGET(exchange: CoapExchange): Unit = {
        exchange.respond(CONTENT, message.getBytes)
    }

    override def handlePOST(exchange: CoapExchange): Unit = {
        
    }

    override def handlePUT(exchange: CoapExchange): Unit = {
        message = exchange.getRequestText()
        exchange.respond(CHANGED, "OK".getBytes)
        changed()
    }

    override def handleDELETE(exchange: CoapExchange): Unit = {
        
    }

}
