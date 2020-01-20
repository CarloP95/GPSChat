# MqttMongoConnector
--------------------
This project written in Scala is implemented to subscribe all messages published to an MQTTBroker topic, to analyze them locally and to send those to MongoDB.
This project uses the awesome [Akka library](https://akka.io/) to implement Actor model.

## Usage

#### Custom implementation
If you want to use this with your software, you must read and change the following lines into [MQTTMongoConnector.scala](https://github.com/CarloP95/GPSChat/blob/master/MqttMongoConnector/mqttmongowriter/src/main/scala/com/carlop/gpschat/MQTTMongoConnector.scala#L28) with your configuration strings.

```scala
class MainGuardianBehavior (context: ActorContext[MainCommand]) extends AbstractBehavior[MainCommand] (context) {

        override def onMessage(msg: MainCommand): Behavior[MainCommand] = {
            msg match {
                case StartCommand(time) => 
                    context.log.info(s"Started MainGuardian at time $time.")

                    val user         = "YourUsernameForMongoCluster"
                    val pwd          = "YourPasswordForMongoUser"

                    val mMongoUri    = s"mongodb+srv://$user:$pwd@YourConnectionString"
                    
                    ...

                    val topic        = "TopicToWhichYouWantToSubscribe"
                    val mMQTTUrl     = "MQTT_URL_AND_PORT"

                    ...
```

#### Start in local

If you want to start in local this project, simply clone this and run with:

```bash
    sbt run
```

#### Start with Docker

###### Build:
If you want to build this with Docker, use the awesome sbt plugin to do this. No need for Dockerfile: 
```bash
    #Package into a container with sbt
    sbt docker:publishLocal
    #Run
    docker run --rm mqtt-to-mongo-writer:1.0
```

###### Pull:
Or you can pull it from my repo:
```bash
    docker pull carlop95/mqtt-to-mongo-writer
    docker run --rm carlop95/mqtt-to-mongo-writer:1.0
```

## Directory Structure
Here i reported the output of the tree command for the src written for this project. You can ignore all the rest.

```
scala
    └── com
        └── carlop
            └── gpschat
                ├── actors
                │   ├── MongoWriter.scala
                │   ├── MQTTActor.scala
                │   └── MsgAnalyzer.scala
                └── MQTTMongoConnector.scala
```

#### gpschat/MQTTMongoConnector.scala
This is just the entrypoint of the scala program. Here the configuration information are retrieved/written and are passed to Actors in creation.

<aside class="warning">
I am conscious that it would be better to have a configuration file, but for the current implementation this has not be done.
</aside>

#### gpschat/actors
In this package all of the actors have been written.

###### MongoWriter.scala
This actor manages the connection with the MongoDB database/cluster. It simply get JSON data from the MsgAnalyzer and then write them into MongoDB.
For now no check is implemented to see if data have been written because i was using Atlas Cluster for this project.

###### MQTTActor.scala
This actor manages the connection with the MQTT Broker and subscribes to the messages passed on a topic. Then it pass these informations to the MsgAnalyzer.

###### MsgAnalyzer.scala
This actor receives messages from the MQTTActor and then it takes the payload of the MQTT message and unmarshall it into a JSON. Then this is passed to MongoWriter.

## Libraries
The libraries that have been used for this project are:
- Akka library for actor model.
- Paho MQTT Client to subscribe to MQTT broker and get messages.
- FasterXML Jackson to unmarshall strings into JSON objects.
- MongoDB Scala Driver for managing the connection with MongoDB.