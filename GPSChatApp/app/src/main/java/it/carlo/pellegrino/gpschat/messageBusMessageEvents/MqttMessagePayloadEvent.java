package it.carlo.pellegrino.gpschat.messageBusMessageEvents;

import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;

public class MqttMessagePayloadEvent {

    private final MqttShoutMessage messageToDisplay;

    public MqttMessagePayloadEvent(MqttShoutMessage toDisplay) {
        messageToDisplay = toDisplay;
    }

    public MqttShoutMessage getPayloadMessage() {
        return this.messageToDisplay;
    }
}
