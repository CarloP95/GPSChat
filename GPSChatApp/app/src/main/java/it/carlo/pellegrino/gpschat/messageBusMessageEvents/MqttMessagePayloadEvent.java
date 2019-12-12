package it.carlo.pellegrino.gpschat.messageBusMessageEvents;

import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttBaseMessage;

public class MqttMessagePayloadEvent {

    private final MqttBaseMessage messageToDisplay;

    public MqttMessagePayloadEvent(MqttBaseMessage toDisplay) {
        messageToDisplay = toDisplay;
    }

    public MqttBaseMessage getPayloadMessage() {
        return this.messageToDisplay;
    }
}
