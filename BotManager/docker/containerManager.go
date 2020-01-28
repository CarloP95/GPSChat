package docker

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/GPSChat/BotManager/messages"
	"github.com/GPSChat/BotManager/utils"
	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/container"
	dockercli "github.com/docker/docker/client"
	"github.com/docker/go-connections/nat"
	"golang.org/x/net/context"
	"io"
	"k8s.io/klog"
	"os"
)

const (
	imageName          = "carlop95/gps-bot"
	latitudeString     = "latitude"
	longitudeString    = "longitude"
	idString           = "id"
	nameString         = "name"
	commandString      = "commands"
	intervalString     = "interval"
	publishTopicString = "topic" //PublishTopic
	topicFilterString  = "subscribeTopic"
	urlString          = "url"
	// Variables related to execution of container
	executableKey     = "BOT_MANAGER_EXE_NAME"
	botHttpPortEnvKey = "BOT_HTTP_PORT"
	// Default values if above environment variables are not set
	defaultExecutableName = "bot"
)

type ContainerManager struct {
	cli                       *dockercli.Client
	ctx                       context.Context
	uidManager                utils.IDManager
	portManager               *utils.PortManager
	botIDToContainerIDMapping map[string]string
	executableName            string
}

func NewContainerManager() (*ContainerManager, error) {
	ctx := context.Background()
	cli, err := dockercli.NewClientWithOpts(dockercli.FromEnv, dockercli.WithAPIVersionNegotiation())
	if err != nil {
		klog.Errorf("Error occurred while creating the CLI to control the Docker daemon to start bot containers.\nError: %s", err)
		return nil, err
	}

	return &ContainerManager{
		cli:                       cli,
		ctx:                       ctx,
		uidManager:                *utils.NewIDManager(),
		botIDToContainerIDMapping: make(map[string]string),
		portManager:               utils.NewPortManager(),
		executableName:            os.Getenv(executableKey),
	}, nil
}

func (cm *ContainerManager) StartBot(command messages.ValueToStartBot, botID string) (string, string, error) {
	reader, err := cm.cli.ImagePull(cm.ctx, imageName, types.ImagePullOptions{})
	if err != nil {
		klog.Infof("Error occurred while pulling image.\nError: %s", err)
		return "", "", err
	}

	_, err = io.Copy(os.Stdout, reader)
	if err != nil {
		klog.Errorf("Error while copying result of image pull to reader.\nError: %s", err)
		return "", "", err
	}

	if len(cm.executableName) == 0 {
		cm.executableName = defaultExecutableName
	}

	var commandKeyValue string
	if len(command.Commands) != 0 {
		commandKeyValue = fmt.Sprintf("-%s %s", commandString, command.Commands)
	} else {
		commandKeyValue = ""
	}

	cliCommand := []string{
		"./" + cm.executableName,

		"-" + latitudeString,
		fmt.Sprintf("%f", command.Latitude),
		"-" + longitudeString,
		fmt.Sprintf("%f", command.Longitude),

		"-" + nameString,
		command.Name,

		"-" + intervalString,
		fmt.Sprintf("%ds", command.Interval),

		"-" + idString,
		botID,

		commandKeyValue,

		"-" + publishTopicString,
		command.PublishTopic,

		"-" + topicFilterString,
		command.TopicFilter,

		"-" + urlString,
		command.URL,
	}

	klog.Infof("Command with which the container will be created is %++v", cliCommand)

	currentPort := cm.portManager.GetPortToAssign()
	hostBinding := nat.PortBinding{
		HostIP:   "0.0.0.0",
		HostPort: currentPort,
	}
	containerPort, err := nat.NewPort("tcp", currentPort)
	if err != nil {
		panic("Unable to get the port")
	}

	portBinding := nat.PortMap{containerPort: []nat.PortBinding{hostBinding}}

	hostConfig := container.HostConfig{
		AutoRemove:   true,
		PortBindings: portBinding,
	}
	//Create Container passing CL Arguments
	resp, err := cm.cli.ContainerCreate(cm.ctx, &container.Config{
		Image: imageName,
		Cmd:   cliCommand,
		// TODO: Must pass BOT_USERNAME and BOT_PWD to configure the bot to connect to MQTT Broker
		Env: []string{botHttpPortEnvKey + "=" + currentPort},
	}, &hostConfig, nil, "")

	if err != nil {
		klog.Errorf("Error while creating the new Container.\nError: %s", err)
		return "", "", err
	}

	cm.botIDToContainerIDMapping[botID] = resp.ID
	klog.Infof("Container %s has been created.", resp.ID)

	err = cm.cli.ContainerStart(cm.ctx, resp.ID, types.ContainerStartOptions{})
	if err != nil {
		klog.Errorf("Error in starting container.\nError: %s", err)
		return "", "", err
	}

	klog.Infof("Container %s has been Started.", resp.ID)

	return resp.ID, fmt.Sprintf(":%s", currentPort), nil
}

func (cm *ContainerManager) DeleteBot(command messages.Message) error {
	containerID := cm.botIDToContainerIDMapping[command.ID]

	if len(containerID) == 0 {
		klog.Errorf("No containerID associated with this bot.\nbot: %s", command.ID)
		return nil
	}

	err := cm.cli.ContainerKill(cm.ctx, containerID, "KILL")
	if err != nil {
		klog.Errorf("Error in deleting a bot.\nError: %s", err)
		return err
	}

	// TODO: Delete exited containers.
	//err := cm.cli.ContainersPrune(cm.ctx, filters.Args{})

	return nil
}

func (cm *ContainerManager) HandleMessage(m messages.Message) (string, string, error) {
	switch m.Op {
	case messages.ValidOps4Manager["create"]:
		if m.Obj == messages.ValidObjs4Manager["container"] {
			// Parse Value into a more complex object
			var containerArgs messages.ValueToStartBot
			err := json.Unmarshal([]byte(m.Value), &containerArgs)
			if err != nil {
				klog.Errorf("An error occurred because the value field was not well formatted.\nValue: %s\nError:", m.Value, err)
				return "", "", err
			}
			// Start the container
			botID, err := cm.uidManager.GetUUID()
			if err != nil {
				klog.Errorf("Error while getting UUID for bot.\nError: %s", err)
			}

			containerID, url, err := cm.StartBot(containerArgs, botID)
			if err != nil {
				klog.Errorf("An error occurred while trying to create the container.\nError: %s", err)
				return "", "", err
			} else {
				cm.botIDToContainerIDMapping[botID] = containerID
			}
			return botID, url, nil
		} else {
			klog.Errorf("This should be never printed. OBJ is not a valid OBJ Message: %s", m.Obj)
			return "", "", errors.New("OBJ passed is different from bot. This is not supported for now. Do Debug")
		}
	case messages.ValidOps4Manager["delete"]:
		if m.Obj == messages.ValidObjs4Manager["container"] {
			// Remove the container
			err := cm.DeleteBot(m)
			if err != nil {
				klog.Errorf("Error in removing the bot.\nError: %s", err)
				return "", "", err
			}

			return "", "", nil

		} else {
			klog.Errorf("This should be never printed. OBJ is not a valid OBJ Message: %s", m.Obj)
			return "", "", errors.New("OBJ passed is different from bot. This is not supported for now. Do Debug")
		}
	default:
		klog.Errorf("This should be never printed. OP is not a valid OP Message: %s", m.Op)
		return "", "", errors.New("Entered in Default Case for Switch with OP. This should not happened. Do debug")
	}
}
