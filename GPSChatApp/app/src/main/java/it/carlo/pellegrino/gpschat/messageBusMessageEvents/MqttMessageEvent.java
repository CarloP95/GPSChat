package it.carlo.pellegrino.gpschat.messageBusMessageEvents;

import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttBaseMessage;

public class MqttMessageEvent {

    private final MqttBaseMessage messageToDisplay;

    public MqttMessageEvent(MqttBaseMessage toDisplay) {
        messageToDisplay = toDisplay;
    }

    public MqttBaseMessage getPayloadMessage() {
        return this.messageToDisplay;
    }
}
