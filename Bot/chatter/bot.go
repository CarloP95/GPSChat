package chatter

import (
	"encoding/json"
	"github.com/GPSChat/Bot/commands"
	"github.com/GPSChat/Bot/messages"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"k8s.io/klog"
	"os"
	"time"
)

const (
	EnvKeyUsername = "BOT_USERNAME"
	EnvKeyPwd      = "BOT_PWD"
)

type Bot struct {
	Name           string
	id             string
	publishTopic   string
	subscribeTopic string
	url            string
	mqttClient     mqtt.Client
}

func NewBot(name, id, publishTopic, subscribeTopic, url string) *Bot {
	return &Bot{
		Name:           name,
		id:             id,
		publishTopic:   publishTopic,
		subscribeTopic: subscribeTopic,
		url:            url,
	}
}

func receiveMsg(client mqtt.Client, message mqtt.Message) {
	klog.Infof("Received MQTT Message from network. Topic of message is %s\n. \nMessage is %s", message.Topic(), string(message.Payload()))
	// TODO: Let the Bot Reply messages. For Current implementation this will not be done.
}

func executeCommands(commandQueue chan commands.Command, resultQueue, periodicQueue chan commands.Result) {
	for {
		var executeCommand commands.Command
		executeCommand = <-commandQueue

		klog.Infof("Executing command %++v", executeCommand)

		if executeCommand.Interval != time.Duration(0) {
			periodicQueue <- executeCommand.Execute()
		} else {
			resultQueue <- executeCommand.Execute()
		}
	}
}

func (b *Bot) Start(commandQueue chan commands.Command, resultQueue, periodicQueue chan commands.Result) {
	klog.Infof("Starting Bot with name %s and ID %s", b.Name, b.id)

	options := mqtt.NewClientOptions().
		AddBroker(b.url).
		SetAutoReconnect(true).
		SetClientID(b.id).
		SetProtocolVersion(4).
		SetUsername(os.Getenv(EnvKeyUsername)).
		SetPassword(os.Getenv(EnvKeyPwd))

	b.mqttClient = mqtt.NewClient(options)

	if token := b.mqttClient.Connect(); token.Wait() && token.Error() != nil {
		klog.Errorf("Error in connecting with MQTT Broker %s", token.Error())
	}

	if token := b.mqttClient.Subscribe(b.subscribeTopic, 1, receiveMsg); token.Wait() && token.Error() != nil {
		klog.Errorf("Error in subscribing to %s topic. \nError: %s ", b.subscribeTopic, token.Error())
	}

	go executeCommands(commandQueue, resultQueue, periodicQueue)

}

func (b *Bot) PublishMessage(message *messages.GPSChatShoutMessage) {
	payload, err := json.Marshal(message)
	if err != nil {
		klog.Errorf("Can't encode %++v to json.\nError: %s", message, err)
	}
	if token := b.mqttClient.Publish(b.publishTopic, 0, true, payload); token.Wait() && token.Error() != nil {
		klog.Errorf("Can't publish Message %++v on Topic %s.\nError: %s", message, b.publishTopic, token.Error())
		return
	}

	klog.Infof("Message published correctly.")
}
