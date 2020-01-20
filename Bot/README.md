# GPSChat Bot
--------------
This is a Bot written in Go, that simulate the communication with the GPSChat users.

## Usage
This shouldn't be run alone, but should be launched by the [BotManager](https://github.com/CarloP95/GPSChat/tree/master/BotManager) using the UNIX Socket /var/run/docker.sock.
However you can launch it in two ways:


#### Start in local
The first alternative is to start in local, but you must have a complete Golang installation on you personal computer. Then you must compile the application with:
```bash
    #Get all dependencies
    go get -d ./
    #Build
    go build -o Bot
    #Start
    ./Bot [clarguments]
```

#### Start in Docker
You can even test it in Docker by either pulling the image or by building your own image.
The environment variable BOT_HTTP_PORT is to tell the Bot in which port to listen for commands by the BotManager.
You can submit your own commands by simply doing a curl with the correct data passed. See [BotManager](https://github.com/CarloP95/GPSChat/tree/master/BotManager) for more info about that.

Pull:
```bash
    docker pull carlop95/gps-bot
    docker run --rm -p 9490:9490 -e BOT_HTTP_PORT=9490 carlop95/gps-bot
```

Build:
```bash
    docker build -t gps-bot .
    docker run --rm -p 9490:9490 -e BOT_HTTP_PORT=9490 gps-bot
```

## Src structure
Here the components created will be discussed. The output of tree command is here presented:
```
Bot
├── chatter
│   └── bot.go
├── commands
│   └── commands.go
├── controller
│   └── controller.go
├── Dockerfile
├── exe
├── main.go
├── messages
│   ├── message_factory.go
│   └── message_types.go
└── README.md
```
#### chatter/bot.go
This package holds the structure of the Bot that interact with users connected with the application. Is a simple MQTT Client that uses *Paho MQTT library* to publish and receive messages. For now, received messages are only logged to screen.

#### commands/commands.go
This package holds the structures of the command that can be submitted to the Bot and Results that come from the execution of Commands. The exported method `Execute` will simulate the execution of the command (e.g. operation:read object:temperature) and then send back to _BotManager_ and forth to MQTT broker the result of this computation: the first message will be encapsulated in a HTTP Response, the second one will be just like a message that Android User can read with GPSChat (a json sent to a MQTT Broker).

#### controller/controller.go
This packages holds the structure that is used to control all goroutines that gets spawned to control the bot. It will start an HTTP Server to listen for BotManager requests and a goroutine to handle all periodic commands to execute (for now the period is fixed to 5 seconds.)

#### main.go
The main method of this Bot. It will create the Bot with its factory method and then pass it to the Controller to be started. It parses all command line arguments that are the way to give commands when starting the Bot.

#### messages

###### message_factory.go
Is a simple factory used to get a Result structure (the result of the computation of a Command) and encapsulate it into a messaga to be sent to the MQTT Broker.

###### message_types.go
The structures that are in this file reflects the structure of the messages that can be exchanged with the MQTT Broker by the Android Client.

## Libraries
For this project *paho.mqtt.golang* library has been used to sent messages to MQTT Broker, *google/uuid* library to get a unique identifies for messages sent to the network, *k8s.io/klog* for the logger.