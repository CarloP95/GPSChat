package controller

import (
	"encoding/json"
	"fmt"
	"github.com/GPSChat/Bot/chatter"
	"github.com/GPSChat/Bot/commands"
	"github.com/GPSChat/Bot/messages"
	"io/ioutil"
	"k8s.io/klog"
	"net/http"
	"os"
	"sync"
	"time"
)

const (
	HttpPortKey = "BOT_HTTP_PORT"
)

type Controller struct {
	bot            *chatter.Bot
	interval       time.Duration
	commands       string
	commandQueue   chan commands.Command
	resultQueue    chan commands.Result
	periodicQueue  chan commands.Result
	quitQueue      chan interface{}
	waitGroup      sync.WaitGroup
	messageFactory *messages.MessageFactory
}

func NewController(bot *chatter.Bot, interval time.Duration, commandsCli string, lat, lon float64) *Controller {
	commandQueue := make(chan commands.Command, 10)
	resultQueue := make(chan commands.Result, 10)
	periodicQueue := make(chan commands.Result, 10)

	mf := messages.NewMessageFactory(bot.Name, lat, lon)

	return &Controller{
		bot:            bot,
		interval:       interval,
		commandQueue:   commandQueue,
		resultQueue:    resultQueue,
		periodicQueue:  periodicQueue,
		commands:       commandsCli,
		waitGroup:      sync.WaitGroup{},
		messageFactory: mf,
	}
}

func (c *Controller) ServeHTTP(w http.ResponseWriter, r *http.Request) {

	switch r.Method {

	case http.MethodGet:
		fallthrough
	case http.MethodPut:
		fallthrough
	case http.MethodDelete:
		_, err := fmt.Fprintf(w, "Don't know what to %s. Please use POST with this endpoint.", r.Method)
		if err != nil {
			klog.Errorf("Error in responding to %s. Problem was %s", r.Method, err)
		}
		return
	case http.MethodPost:
		defer r.Body.Close()

		bytes, err := ioutil.ReadAll(r.Body)
		if err != nil {
			klog.Errorf("Error while reading body of POST request.\nError: %s", err)
			return
		}
		jsonBody := string(bytes)
		klog.Infof("Received %s from body of POST request", jsonBody)
		var command commands.Command
		if err = json.Unmarshal(bytes, &command); err != nil {
			klog.Errorf("The Body of the request is not a valid Command.\nError: %s", err)
			_, err = fmt.Fprintf(w, "The Body of the request is not a valid Command.\nError: %s", err)
			if err != nil {
				klog.Errorf("While handling above error, something else occurred while writing back to HTTP User.\nError: %s", err)
				return
			}
		}
		//TODO: Check if command is to shut down the bot.
		klog.Info("Forwarding Command to Bot")
		// If is a periodic message, write to c.commands to be executed periodically.
		if command.Interval != time.Duration(0) {
			if trimIdx := len(c.commands) - 1; trimIdx > 0 {
				c.commands = fmt.Sprintf(`%s, %s ]`,
					c.commands[:trimIdx], bytes)
			}
			//Command has become periodic. Now Submit a one shot command to respond to HTTP User
			command.Interval = time.Duration(0)
		}
		c.commandQueue <- command
		res := <-c.resultQueue
		klog.Infof("Result is: %++v", res)
		c.bot.PublishMessage(c.messageFactory.NewShoutMessage(res))

		_, err = fmt.Fprintf(w, "%++v", res)
		if err != nil {
			klog.Errorf("An error occurred while writing the Result back to HTTP user.\nError: %s", err)
		}
	}
}

func listenHTTPRequests(c *Controller) {

	defer c.waitGroup.Done()

	port := os.Getenv(HttpPortKey)
	if len(port) < 1 {
		port = "9490"
	}

	http.Handle("/api/bot/v1", c)

	for {
		err := http.ListenAndServe(":"+port, nil)
		if err != nil {
			klog.Errorf("Error in serving the HTTP Interface: %s", err)
			close(c.quitQueue)
			break
		}
	}
}
// TODO: Interval of command is not respected
func schedulePeriodicCommands(c *Controller) {

	defer c.waitGroup.Done()

	var mockInterval time.Duration
	mockInterval = 5

	ticker := time.NewTicker(mockInterval * time.Second)
	c.quitQueue = make(chan interface{})
	go func() {
		for {
			select {
			case <-ticker.C:
				var comms []commands.Command
				if err := json.Unmarshal([]byte(c.commands), &comms); err != nil {
					klog.Errorf("Something is wrong with commands passed. Should check those.\nError: %s", err) //TODO: Check Commands in creation of controller
				}
				for i := 0; i < len(comms); i += 1 {
					comms[i].Interval = mockInterval
					c.commandQueue <- comms[i]
					var periodicResult commands.Result
					periodicResult = <-c.periodicQueue
					klog.Infof("Periodic Behavior returned with: %++v", periodicResult)
					c.bot.PublishMessage(c.messageFactory.NewShoutMessage(periodicResult))
				}
			case <-c.quitQueue:
				ticker.Stop()
				return
			}
		}
	}()
}

func (c *Controller) Start() {

	c.bot.Start(c.commandQueue, c.resultQueue, c.periodicQueue)

	c.waitGroup.Add(2)
	go schedulePeriodicCommands(c)
	go listenHTTPRequests(c)
	c.waitGroup.Wait()

	klog.Error("All Goroutines in Controller exited. This means that some error occurred. Check Logs.")
}
