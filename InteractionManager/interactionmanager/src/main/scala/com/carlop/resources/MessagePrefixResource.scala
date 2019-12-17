package com.carlop.resources

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.server.resources.CoapExchange
import org.eclipse.californium.core.coap.CoAP.ResponseCode._

import org.mongodb.scala.{MongoCollection, Document, Subscription, Observer, ChangeStreamObservable}
import com.mongodb.MongoTimeoutException

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import scala.collection.mutable
import com.mongodb.client.model.changestream.ChangeStreamDocument

class MessagePrefixResource(name: String, visible: Boolean, collection: MongoCollection[Document]) extends CoapResource(name: String, visible: Boolean) {

    setObservable (true)
    var observable: ChangeStreamObservable[Document] = collection.watch()
    var observer = new LatchedObserver[ChangeStreamDocument[Document]]()
    observable.subscribe(observer)
    
    override def handleGET(exchange: CoapExchange): Unit = {
        exchange.respond(CONTENT)
    }

    override def handlePOST(exchange: CoapExchange): Unit = {
        
    }

    override def handlePUT(exchange: CoapExchange): Unit = {
        exchange.respond(CHANGED, "OK".getBytes)
    }

    override def handleDELETE(exchange: CoapExchange): Unit = {
        
    }

    class LatchedObserver[T](val printResults: Boolean = true, val minimumNumberOfResults: Int = 1) extends Observer[T] {
        
        private val latch: CountDownLatch = new CountDownLatch(1)
        private val resultsBuffer: mutable.ListBuffer[T] = new mutable.ListBuffer[T]
        private var subscription: Option[Subscription] = None
        private var error: Option[Throwable] = None
    
        override def onSubscribe(s: Subscription): Unit = {
            subscription = Some(s)
            s.request(Integer.MAX_VALUE)
        }
    
        override def onNext(t: T): Unit = {
            resultsBuffer.append(t)
            if (printResults) println(t)
            if (resultsBuffer.size >= minimumNumberOfResults) latch.countDown()
            changed()
        }
    
        override def onError(t: Throwable): Unit = {
            error = Some(t)
            println(t.getMessage)
            onComplete()
        }
    
        override def onComplete(): Unit = {
            latch.countDown()
        }
    
        def results(): List[T] = resultsBuffer.toList
    
        def await(): Unit = {
          if (!latch.await(120, SECONDS)) throw new MongoTimeoutException("observable timed out")
          if (error.isDefined) throw error.get
        }
    
        def waitForThenCancel(): Unit = {
            if (minimumNumberOfResults > resultsBuffer.size) await()
                subscription.foreach(_.unsubscribe())
        }
      }

}
