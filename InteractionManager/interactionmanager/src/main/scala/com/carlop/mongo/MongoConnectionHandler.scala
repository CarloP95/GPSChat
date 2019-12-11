package com.carlop.mongo
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.slf4j.{LoggerFactory, Logger}
import scala.util.{Success, Failure}
import org.mongodb.scala.{MongoCollection, Document}

object MongoBootstrapHandler {

    final case class ReturnFromCreation(client: MongoClient, msgCollection: MongoCollection[Document], msgPrefixCollection: MongoCollection[Document])

    implicit val executionContext: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    private val log : Logger        = LoggerFactory.getLogger(this.getClass().getName())
    private var client: MongoClient = null
    private var db: MongoDatabase   = null

    private val msgCollectionString = "messages"
    private val msgFPCollectionStr  = "messageFingerprints"

    def apply(mongoUrl: String, dbName: String) : ReturnFromCreation = {
        if (client == null)
            client = MongoClient(mongoUrl)

        if (db == null)
            db = initDb(dbName)
        
        val (msgCollection, msgPrefixCollection) = getDefaultCollections
        ReturnFromCreation(client, msgCollection, msgPrefixCollection)
    }

    private def initDb(dbName: String): MongoDatabase = {
        val db : MongoDatabase = client.getDatabase(dbName)
        db.createCollection(msgCollectionString).andThen( _ => db.createCollection(msgFPCollectionStr) )
            .toFuture() onComplete {
                case Success(_)   => log.info("Default collections have been created successfully.")
                case Failure(err) => log.error(err.getLocalizedMessage)
            }
        db
    }

    private def getDefaultCollections: (MongoCollection[Document], MongoCollection[Document]) =
     (db.getCollection(msgCollectionString), db.getCollection(msgFPCollectionStr))

}