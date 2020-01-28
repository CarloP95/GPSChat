package utils

import (
	"fmt"
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
		return fmt.Sprintf("%d", mock.ID()), err
	}

	return fmt.Sprintf("%d", toReturn.ID()), nil
}
