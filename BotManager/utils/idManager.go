package utils

import (
	"github.com/google/uuid"
)

type IDManager struct {
}

func NewIDManager() *IDManager {
	return &IDManager{}
}

func (im *IDManager) GetUUID() (string, error) {
	toReturn, err := uuid.NewRandom()

	if err != nil {
		mock, _ := uuid.Parse("0") //This will never return Error.
		return mock.String(), err
	}

	return toReturn.String(), nil
}
