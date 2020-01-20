package utils

import "fmt"

const (
	defaultToAssignPort = 9460
)

type PortManager struct {
	assignedPorts    []int
	nextPortToAssign int
}

func NewPortManager() *PortManager {
	return &PortManager{
		assignedPorts:    []int{},
		nextPortToAssign: defaultToAssignPort,
	}
}

// Assign Ports in incremental way
// TODO: clean not used ports
func (pm *PortManager) GetPortToAssign() string {
	toReturnPort := pm.nextPortToAssign
	toReturnPortString := fmt.Sprintf("%d", toReturnPort)

	pm.assignedPorts = append(pm.assignedPorts, toReturnPort)
	pm.nextPortToAssign++

	return toReturnPortString
}