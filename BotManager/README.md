# Bot Manager
-------------
This project is the Bot Manager written in Go that will launch Bots and instruct them to interact with GPSChat Application through the MQTT Broker.

## Usage
This project can be either start in local or run on Docker. I always suggest the second alternative, but you are free to do anyone of the two.

#### Start in local
To start this in local you must have a complete Go installation on your personal computer. Then you must do:

```bash
    #Get all dependencies
    go get -d ./
    #Build
    go build -o BotManager
    #Execute
    ./BotManager
```

#### Start with Docker
You can choose to pull the docker image from docker hub or build it by yourself:

###### Environment variables
You can choose to set the following environment variables that will be used from this component for the following reasons:
```
    BOT_MANAGER_PORT : Port in which this component will listen for requests. Default 9450.
    BOT_MANAGER_EXE_NAME : The executable name that you give to Bot Container to start. Default Bot.
    BOT_HTTP_PORT : The key with which to search the HTTP_PORT in the Bot.
```

###### Pull:
```bash
    docker pull carlop95/bot-manager
    #Must pass docker socket to allow Bots to be spawned
    docker run --rm --v /var/run/docker.sock:/var/run/docker.sock -p 9450:9450 carlop95/bot-manager
```

###### Build:
```bash
    #Build argument is optional to specify which port you want to expose. Default is 9450
    docker build -t bot-manager . [--build-arg exposedPort=9450]
    #Must pass docker socket to allow Bots to be spawned
    docker run --rm --v /var/run/docker.sock:/var/run/docker.sock -p 9450:9450 bot-manager
```

## Directory structure
```bash
BotManager
├── bot
│   └── botManager.go
├── controller
│   └── controller.go
├── docker
│   └── containerManager.go
├── Dockerfile
├── exe
├── main.go
├── messages
│   └── message_types.go
├── README.md
└── utils
    ├── idManager.go
    └── portManager.go
```

#### bot/botManager.go
This component will holds informations about spawned [Bots](https://github.com/CarloP95/GPSChat/tree/master/Bot). In this way it will be possibile to dispatch commands to them.

#### controller/controller.go
This component will spawn and wait for goroutines. For now it waits for the goroutine that starts an HTTP Server listening for requests from clients.
Then it apply the logic to understand if a request is directed through a Bot or to the BotManager to spawn or destroy a Bot.

#### docker/containerManager.go
This component will handle the creation of Docker Containers through the [dockercli](https://godoc.org/github.com/docker/docker/client) to give command to _/var/run/docker.sock_. It uses the [PortManager](#portManager.go) to give ports in an incremental way, to have unique URL for each Bot that is spawned.

#### messages/message_types.go
This package holds the structure of the messages that the BotManager will handle. It holds even validation information.

#### utils

###### idManager.go
This component handles the creation of Unique ID for Bots, to be addressed from each client that request the creation of a *Bot*. It uses *google/uuid* library to get this unique id.

###### portManager.go
This component is used to avoid mapping more containers to the same port. In this way each Bot will have its unique URL in the host.

##### main.go
The entrypoint of the BotManager program. This will start the Controller and waits for it to crash or to terminate its work.

## Libraries
For connecting with *DockerD* the library [docker client](https://godoc.org/github.com/docker/docker/client) has been used, to generate *universal unique id* the library [uuid](https://github.com/google/uuid) has been used, to *logging* the library [klog](https://github.com/kubernetes/klog) has been used.
