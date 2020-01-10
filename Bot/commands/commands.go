package commands

import (
	"fmt"
	"k8s.io/klog"
	"math/rand"
	"strings"
	"time"
)

type Command struct {
	Op       string        `json:"op"`
	Obj      string        `json:"obj"`
	Interval time.Duration `json:"interval,omitempty"`
	Value    string        `json:"value,omitempty"`
}

type Result struct {
	Resource []string `json:"resources"`
	Message  string   `json:"message"`
}

var (
	validOps  = map[string]string{"read": "read", "write": "write"}
	validObjs = map[string]string{"temp": "temperature", "cam": "camera", "mag": "magwindow"}
)

const (
	ThiefImage   = "https://www.positanonews.it/photogallery_new/images/2017/04/ladri-appartamento-positano-3182319.660x368.jpg"
	AllCalmImage = "https://www.poesie.reportonline.it/images/stories/cosevarie/finestra_chiusa.jpg"
)

func (c *Command) Execute() Result {
	issuedOp := strings.ToLower(c.Op)
	issuedObj := strings.ToLower(c.Obj)

	//Check if Command is a valid Command.
	var validOp, validObj bool
	for _, op := range validOps {
		if op == issuedOp {
			validOp = true
		}
	}

	for _, obj := range validObjs {
		if obj == issuedObj {
			validObj = true
		}
	}

	if !(validOp && validObj) {
		klog.Errorf("Can't execute the command that you issued.\nCommand: %++v", c)
	}

	switch issuedObj {
	case validObjs["temp"]:
		if issuedOp == validOps["read"] {
			returnVal := rand.Intn(5) + 23
			return Result{Message: fmt.Sprintf("%d %s", returnVal, "Celsius")}
		} else { //validOps["write"]
			// Mock Write.
			return Result{Message: "Thermostat temperature set."}
		}
	case validObjs["mag"]:
		if issuedOp == validOps["read"] {
			returnVal := rand.Intn(2)
			if returnVal == 0 {
				return Result{Message: "Window is closed."}
			} else {
				return Result{Message: "Window is opened."}
			}
		} else { //validOps["write"]
			// Mock Write.
			return Result{Message: "Triggering action to close window."}
		}
	case validObjs["cam"]:
		if issuedOp == validOps["read"] {
			returnVal := rand.Intn(2)
			if returnVal == 0 {
				return Result{Resource: []string{AllCalmImage}, Message: "Camera has not detected intruders."}
			} else {
				return Result{Resource: []string{ThiefImage}, Message: "Camera has detected intruders!"}
			}
		} else { //validOps["write"]
			return Result{Message: "Can't write to camera."}
		}
	default: //Execution will never get here.
		return Result{Message: "If this message is returned, contact system administrator."}
	}
}
