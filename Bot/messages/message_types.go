package messages

type MessageType int

const (
	ShoutMsg  MessageType = 0x00
	UpdateMsg MessageType = 0x01
	ReplyMsg  MessageType = 0x02
	DeleteMsg MessageType = 0x03
)

type GPSChatBaseMessage struct {
	Nickname  string      `json:"nickname"`
	Message   string      `json:"message"`
	ID        string      `json:"id"`
	Type      MessageType `json:"type"`
	Resources []string    `json:"resources"`
	Revision  int         `json:"revision"`
	Timestamp string      `json:"timestamp"`
}

type GPSChatShoutMessage struct {
	GPSChatBaseMessage
	Location   LatLon                `json:"location"`
	Replies    []GPSChatReplyMessage `json:"replies"`
	NumReplies int                   `json:"numReplies"`
}

type GPSChatReplyMessage struct {
	GPSChatBaseMessage
	ResponseTo string                `json:"responseTo"`
	Replies    []GPSChatReplyMessage `json:"replies"`
	NumReplies int                   `json:"numReplies"`
}

type LatLon struct {
	Latitude  float64 `json:"lat"`
	Longitude float64 `json:"lon"`
}
