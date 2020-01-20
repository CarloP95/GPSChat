package bot

import (
	"errors"
	"io"
	"io/ioutil"
	"k8s.io/klog"
	"net/http"
)

type BotManager struct {
	IDToURLMap	map[string]string
}

func NewBotManager() *BotManager {
	return &BotManager{map[string]string{}}
}

func (bm *BotManager) UpdateID4BotURL(botID, url string) {
	bm.IDToURLMap[botID] = url
}

func (bm *BotManager) SendCommandToBot(botID string, body io.Reader) ([]byte, error) {

	botUrl := bm.IDToURLMap[botID]
	if len(botUrl) == 0 {
		klog.Errorf("Did not found BotID with id %s. Can't send message.", botID)
		return nil, errors.New("Can't find the requested Bot. Bot ID is not valid. Check what you sent to BotManager")
	}

	resp, err := http.Post(botUrl, "application/json", body)
	if err != nil {
		klog.Errorf("Command has been sent to Bot but an error returned.\nError: %s", err)
		return nil, err
	}

	bytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		klog.Errorf("Command has been sent to Bot but an error happened when reading the body.\nError: %s", err)
		return nil, err
	}

	return bytes, nil
}