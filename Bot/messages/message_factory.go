package messages

import (
	"fmt"
	"github.com/GPSChat/Bot/commands"
	"github.com/google/uuid"
	"k8s.io/klog"
	"time"
)

type MessageFactory struct {
	Name      string
	Latitude  float64
	Longitude float64
}

func NewMessageFactory(name string, lat, lon float64) *MessageFactory {
	return &MessageFactory{Name: name, Latitude: lat, Longitude: lon}
}

func (mf *MessageFactory) NewShoutMessage(result commands.Result) *GPSChatShoutMessage {
	id, err := uuid.NewRandom()
	if err != nil {
		klog.Errorf("Something's wrong in generating UUID.\nError: %s", err)
		id =  uuid.MustParse("2")
	}
	return &GPSChatShoutMessage{
		GPSChatBaseMessage: GPSChatBaseMessage{
			Nickname:  mf.Name,
			Message:   result.Message,
			ID:        id.String(),
			Resources: result.Resource,
			Type:      ShoutMsg,
			Revision:  0,
			Timestamp: fmt.Sprintf("%s", time.Now()),
		},
		Location: LatLon{Latitude: mf.Latitude, Longitude: mf.Longitude},
	}
}
