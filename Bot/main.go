package main

import (
	"flag"
	"fmt"
	"github.com/GPSChat/Bot/chatter"
	"github.com/GPSChat/Bot/controller"
	"k8s.io/klog"
	"os"
)

const (
	//Default Values for Flag
	defaultPublishTopic = "publishTopic"
	defaultTopicFilter  = "subscribeTopic"
	defaultLat			= 37.5242381
	defaultLon 			= 15.0703784
	defaultId           = "Bot@1234"
	defaultName         = "ExampleBot"
	defaultUrl          = "tcp://ec2-52-90-157-176.compute-1.amazonaws.com:1883"
	defaultCommand		= `[ {"op": "read", "obj": "temperature", "interval": 5} ]`
)

func usage() {
	fmt.Println("Usage:", "\n", "Please provide a -name and -id argument to start. Like the following: ", "$ ./Bot -name CustomBot -id Bot@9999")
	flag.PrintDefaults()
	//Example of commands are : [ {"op": "read", "obj": "temperature"}, {"op": "read", "type": "camera"} ]
}

func main() {

	klog.Info("Started Bot")

	flag.Usage = usage

	name := flag.String("name", defaultName, "The name given to the Bot that is going to be started")
	id := flag.String("id", defaultId, "The ID for the Bot to be unique located in GPSChat")
	publishTopic := flag.String("topic", defaultPublishTopic, "The topic in which to publish results of command")
	topicFilter := flag.String("subscribeTopic", defaultTopicFilter, "The topic in which to activate the subscription to make the Bot receive messages")
	commands := flag.String("commands", defaultCommand, "Commands to be executed from the Bot")
	url := flag.String("url", defaultUrl, "URL of the MQTT Broker")
	interval := flag.Duration("interval", 5, "Interval in seconds to schedule execution of commands on Bot.")
	latitude := flag.Float64("latitude", defaultLat, "Latitude to publish values")
	longitude := flag.Float64("longitude", defaultLon, "Longitude to publish values")

	flag.Parse()

	//Check if ID and name are defaults.. And Error if so
	if *name == defaultName || *id == defaultId {
		usage()
		os.Exit(1)
	}

	//Create the Bot & the Controller
	bot := chatter.NewBot(*name, *id, *publishTopic, *topicFilter, *url)
	control := controller.NewController(bot, *interval, *commands, *latitude, *longitude)

	control.Start()

	klog.Infof("Shutting down Bot %s...", *name)

}
