package it.carlo.pellegrino.gpschat.messageHandlers;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import it.carlo.pellegrino.gpschat.mapUtils.MqttPayloadMessageAdaptatorForMarker;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MqttMessageEvent;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;

public class MessageHandlerContainer {

    private EventBus processEventBus = EventBus.getDefault();
    private HashMap<Long, MqttShoutMessage> mqttPayloadMessages;
    private GoogleMap uiMap;

    private int previousHashCode = -1;

    public MessageHandlerContainer(GoogleMap uiMap) {
        mqttPayloadMessages = new HashMap<>();
        this.uiMap = uiMap;
    }

    public MessageHandlerContainer pushMessage(MqttShoutMessage msg) {

        MqttShoutMessage alreadyPresentMessage = mqttPayloadMessages.get(msg.getId());

        if (alreadyPresentMessage != null)
            Log.v("GPSCHAT", "Received duplicate entry for messageId that should be unique. " +
                    "Received is: " + msg.toString() + "\nAlready present is: " + alreadyPresentMessage.toString());

        /* However, since it can be possible that a message has duplicates id, put the new message and forget the previous. */
        this.mqttPayloadMessages.put(msg.getId(), msg);

        handleNotifyUI(false, msg);

        return this;
    }

    public void handleNotifyUI(boolean force, MqttShoutMessage msg) {

        int currentHashCode = this.mqttPayloadMessages.hashCode();

        if (force || currentHashCode != previousHashCode) {
            notifyUI(msg);
            MessageHandlerComponent.updateUI(this.uiMap, msg);
        }

        this.previousHashCode = currentHashCode;
    }

    private void notifyUI(MqttShoutMessage msg) {
        processEventBus.post(new MqttMessageEvent(msg));
    }

    public static class MessageHandlerComponent {

        private static Marker updateUI(GoogleMap map, MqttShoutMessage msg) {
            return MqttPayloadMessageAdaptatorForMarker.adapt(map, msg);
        }

    }
}
