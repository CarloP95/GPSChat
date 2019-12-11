package it.carlo.pellegrino.gpschat.messageBusMessageEvents;

import it.carlo.pellegrino.gpschat.messageHandlers.MessageHandlerContainer;

public class WrapperEventForMessageHandler {

    private MessageHandlerContainer messageHandler;

    public WrapperEventForMessageHandler(MessageHandlerContainer h) {
        messageHandler = h;
    }

    public MessageHandlerContainer getMessageHandler() {
        return messageHandler;
    }

}
