package main

import (
	"github.com/GPSChat/BotManager/controller"
	"k8s.io/klog"
)

func main() {
	klog.Infof("Started BOT Manager. Contact to 0.0.0.0:9450 to endpoint /api/botmanager/v1")
	klog.Infof("9450 is the default port for bot Manager. You can override it by using BOT_MANAGER_PORT environment variable")

	controller := controller.NewController()
	controller.Start()

	klog.Infof("Closing BOT Manager...")
}
