package messages

import "strings"

var (
	ValidOps4Bots     = map[string]string{"read": "read", "write": "write"}
	ValidObjs4Bots    = map[string]string{"temp": "temperature", "cam": "camera", "mag": "magwindow"}
	ValidOps4Manager  = map[string]string{"create": "create", "delete": "delete"}
	ValidObjs4Manager = map[string]string{"container": "bot"}
)

type Message struct {
	Op       string `json:"op"`
	Obj      string `json:"obj"`
	Value    string `json:"value"`
	ID       string `json:"id"`
	Interval int    `json:"interval"`
}

// Value of the previous struct will be converted to this type if is a command for the manager to handle.
type ValueToStartBot struct {
	PublishTopic string  `json:"publishTopic"`
	TopicFilter  string  `json:"topicFilter"`
	Name         string  `json:"name"`
	Latitude     float64 `json:"latitude"`
	Longitude    float64 `json:"longitude"`
	Interval     int     `json:"interval"`
	URL          string  `json:"url"`
	Commands     string  `json:"commands"`
}

func checkMap4Entry(entry string, m map[string]string) bool {
	for _, value := range m {
		if value == entry {
			return true
		}
	}
	return false
}

func (m *Message) ValidateMsg() (valid, validForBot, validForManager bool) {
	var validObj4Bot, validOp4Bot, validObj4Manager, validOp4Manager bool

	opToValidate, objToValidate := strings.ToLower(m.Op), strings.ToLower(m.Obj)

	validOp4Bot = checkMap4Entry(opToValidate, ValidOps4Bots)
	validObj4Bot = checkMap4Entry(objToValidate, ValidObjs4Bots)
	validOp4Manager = checkMap4Entry(opToValidate, ValidOps4Manager)
	validObj4Manager = checkMap4Entry(objToValidate, ValidObjs4Manager)

	validForBot = validObj4Bot && validOp4Bot
	validForManager = validOp4Manager && validObj4Manager

	valid = validForManager || validForBot

	return
}
