package it.carlo.pellegrino.gpschat.messageBusMessageEvents;

import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;

public class MqttMessageEvent {

    private final MqttShoutMessage messageToDisplay;

    public MqttMessageEvent(MqttShoutMessage toDisplay) {
        messageToDisplay = toDisplay;
    }

    public MqttShoutMessage getPayloadMessage() {
        return this.messageToDisplay;
    }
}
