package controller

import (
	"encoding/json"
	"fmt"
	"github.com/GPSChat/BotManager/bot"
	"github.com/GPSChat/BotManager/docker"
	"github.com/GPSChat/BotManager/messages"
	"io/ioutil"
	"k8s.io/klog"
	"net/http"
	"os"
	"sync"
)

const (
	BotManagerPortKey = "BOT_MANAGER_PORT"
)

type Controller struct {
	waitGroup        sync.WaitGroup
	containerManager docker.ContainerManager
	botManager       bot.BotManager
}

func NewController() *Controller {
	var waitGroup sync.WaitGroup
	var cm *docker.ContainerManager
	var bm *bot.BotManager
	cm, err := docker.NewContainerManager()
	if err != nil {
		klog.Errorf("Error in creation of container manager. Check logs.\nError: %s", err)
		os.Exit(1)
	}
	bm = bot.NewBotManager()

	return &Controller{
		waitGroup:        waitGroup,
		containerManager: *cm,
		botManager:       *bm,
	}
}

func (c *Controller) listenForRequests(address string) {
	defer c.waitGroup.Done()
	klog.Infof("Starting listening to address %s", address)
	for {
		err := http.ListenAndServe(address, nil)
		if err != nil {
			klog.Errorf("Some error occurred while listening for HTTP Requests.. Inspect Logs.")
			return
		}
	}
}

func (c *Controller) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case http.MethodGet:
		fallthrough
	case http.MethodDelete:
		fallthrough
	case http.MethodPut:
		klog.Infof("Received an invalid request. Method is %s", r.Method)
		_, err := fmt.Fprint(w, "Please, use POST with this endpoint")
		if err != nil {
			klog.Errorf("Something's wrong when writing response to HTTP User.\n%s", err)
		}
	case http.MethodPost:
		defer r.Body.Close()
		var message messages.Message
		// Read body of message
		bytes, err := ioutil.ReadAll(r.Body)
		if err != nil {
			klog.Errorf("Error in Reading MSG Body. Something went wrong.")
			_, err = fmt.Fprintf(w, "Your body seems not to be well formatted. Correct it or contact system admin.\nError: %s", err)
			if err != nil {
				klog.Errorf("While handling above exception, another one occurred. Can't send response to HTTP User.\nError: %s", err)
			}
		}
		// Convert into messages.Message
		err = json.Unmarshal(bytes, &message)
		if err != nil {
			klog.Errorf("Some error occurred while processing JSON body.\nError: %s", err)
			_, err = fmt.Fprintf(w, "Your body seems not to be a valid message for BotManager. Please correct it.\n%s", err)
			if err != nil {
				klog.Errorf("While handling above exception, another one occurred. Can't send response to HTTP User.\nError: %s", err)
			}
		}
		// Validate Message
		valid, validForBot, validForManager := message.ValidateMsg()
		if !(valid && (validForBot || validForManager)) {
			klog.Errorf("Received an incorrect Message. Will discard it.")
			_, err = fmt.Fprintf(w, "Your body seems not to be a valid message for BotManager. Please correct it.\n%s", err)
			if err != nil {
				klog.Errorf("While handling above exception, another one occurred. Can't send response to HTTP User.\nError: %s", err)
			}
			return
		}
		klog.Infof("Received a correct Message. Now process it.")
		// Replay message to Bot
		if validForBot {
			resp, err := c.botManager.SendCommandToBot(message.ID, r.Body)
			if err != nil {
				klog.Errorf("Error in sending command to Bot.\nError: %s", err)
				_, err = fmt.Fprintf(w, "Some error occurred while sending the command represented by %++v to Bot. Maybe the Bot can't execute it.\nError: %s ", message, err)
				if err != nil {
					klog.Errorf("While handling above exception, another one occurred. Can't send response to HTTP User.\nError: %s", err)
				}
				return
			}

			klog.Info("Bot respond to HTTP User. Sending response.")
			_, err = fmt.Fprintf(w, "%s", resp)
			if err != nil {
				klog.Errorf("While sending response to HTTP User an exception occurred. Can't send response.\nError: %s", err)
				return
			}

		}
		// Process the message and start containers
		if validForManager {
			botID, url, err := c.containerManager.HandleMessage(message)
			if err != nil {
				klog.Errorf("Some error occurred while executing the command represented by %++v.\nError: %s", message, err)
				_, err = fmt.Fprintf(w, "Some error occurred while executing the command represented by %++v.\nError: %s ", message, err)
				if err != nil {
					klog.Errorf("While handling above exception, another one occurred. Can't send response to HTTP User.\nError: %s", err)
				}
				return
			}
			// Save BotID and URL
			c.botManager.UpdateID4BotURL(botID, url)

			// Return BotID to HTTP User
			_, err = fmt.Fprintf(w, `{"botID" : "%s", "URL": "%s" }`, botID, url)
			if err != nil {
				klog.Errorf("While sending BotID an error occurred. Can't send response to HTTP User.\nError: %s", err)
			}
		}
	}
}

func (c *Controller) Start() {

	port := os.Getenv(BotManagerPortKey)

	if len(port) == 0 {
		port = "9450"
	}
	http.Handle("/api/botmanager/v1", c)
	c.waitGroup.Add(1)
	go c.listenForRequests(":" + port)
	c.waitGroup.Wait()
}
