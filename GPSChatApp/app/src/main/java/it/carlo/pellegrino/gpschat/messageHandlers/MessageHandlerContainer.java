package it.carlo.pellegrino.gpschat.messageHandlers;

import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.carlo.pellegrino.gpschat.mapUtils.MqttPayloadMessageAdaptatorForMarker;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MqttMessageEvent;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttBaseMessage;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttReplyMessage;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;

public class MessageHandlerContainer {

    private EventBus processEventBus = EventBus.getDefault();
    private HashMap<Long, MqttBaseMessage> mMQTTPayloadMessages;
    private HashMap<Long, Marker> mMessagesMarkers;
    private HashMap<String, MqttBaseMessage> mMarkerMQTTMessages;
    private GoogleMap uiMap;

    private int previousHashCode = -1;

    public MessageHandlerContainer(GoogleMap uiMap) {
        mMQTTPayloadMessages = new HashMap<>();
        mMarkerMQTTMessages = new HashMap<>();
        mMessagesMarkers = new HashMap<>();
        this.uiMap = uiMap;
    }

    public MqttBaseMessage getMessageFromMarker(Marker m) {
        return mMarkerMQTTMessages.get(m.getId());
    }

    public MessageHandlerContainer pushMessage(MqttBaseMessage msg) {

        MqttBaseMessage alreadyPresentMessage = mMQTTPayloadMessages.get(msg.getId());
        switch (msg.getType()) {
            case MqttBaseMessage.TYPE_SHOUT:

                if (alreadyPresentMessage == null) {
                    // Display in map
                    Marker m = handleNotifyUI(false, msg);
                    // Add in repositories
                    mMQTTPayloadMessages.put(msg.getId(), msg);
                    mMarkerMQTTMessages.put(m.getId(), msg);
                    mMessagesMarkers.put(msg.getId(), m);
                } else {
                    Log.i("GPSCHAT", "A message that is already present, returned as TYPE SHOUT. From Network Message: " + msg.toString() + "\nOld Message: " + alreadyPresentMessage.toString());
                }

                break;

            case MqttBaseMessage.TYPE_UPDATE:

                if (alreadyPresentMessage != null) {
                    // Delete from Eepositories
                    Marker m = mMessagesMarkers.remove(alreadyPresentMessage.getId());
                    MessageHandlerComponent.removeMarker(m);
                    mMarkerMQTTMessages.remove(m.getId());
                    mMQTTPayloadMessages.remove(alreadyPresentMessage.getId());

                    m = handleNotifyUI(false, msg);
                    // Update Repositories
                    mMQTTPayloadMessages.put(msg.getId(), msg);
                    mMarkerMQTTMessages.put(m.getId(), msg);
                    mMessagesMarkers.put(msg.getId(), m);

                } else {
                    Log.i("GPSCHAT", "A message that is not present arrived as TYPE UPDATE. From Network Message: " + msg.toString());
                }
                break;
            case MqttBaseMessage.TYPE_REPLY:
                // Check presence of Shout in repositories
                MqttShoutMessage shoutMessage = (MqttShoutMessage)mMQTTPayloadMessages.get (((MqttReplyMessage)msg).getResponseTo());
                if (shoutMessage != null) {
                    // Add to Replies
                    List<MqttReplyMessage> replies = shoutMessage.getReplies();
                    replies.add((MqttReplyMessage)msg);

                } else {
                    Log.i("GPSCHAT", "A Reply to a message that is not present arrived as TYPE REPLY. From Network Message: " + msg.toString());
                }
                break;
            case MqttBaseMessage.TYPE_DELETE:

                if (alreadyPresentMessage != null) { // If it was a SHOUT message, will be here // TODO: Handle DELETE of Type UPDATE messages.
                    // Remove from Map
                    Marker m = mMessagesMarkers.remove(msg.getId());
                    MessageHandlerComponent.removeMarker(m);
                    mMarkerMQTTMessages.remove(m.getId());
                    mMQTTPayloadMessages.remove(msg.getId());
                    // Remove from repositories
                } else {
                    Log.i("GPSCHAT", "A message that is not present arrived as TYPE DELETE. From Network Message: " + msg.toString());
                }

                break;
            default:
                Log.e ("GPSCHAT", "A message that has not the correct TYPE arrived: " + msg.toString());
        }

        return this;
    }

    private Marker handleNotifyUI(boolean force, MqttBaseMessage msg) {
        Marker addedMarker = null;
        int currentHashCode = this.mMQTTPayloadMessages.hashCode();

        if (force || currentHashCode != previousHashCode) {
            notifyUI(msg);
            addedMarker = MessageHandlerComponent.addMarker(this.uiMap, (MqttShoutMessage) msg);
        }

        this.previousHashCode = currentHashCode;

        return addedMarker;
    }

    private void notifyUI(MqttBaseMessage msg) {
        processEventBus.post(new MqttMessageEvent(msg));
    }

    private static class MessageHandlerComponent {

        private static Marker addMarker(GoogleMap map, MqttShoutMessage msg) {
            return MqttPayloadMessageAdaptatorForMarker.adapt(map, msg);
        }

        private static void removeMarker(Marker m) {
            m.hideInfoWindow();
            m.remove();
        }

    }
}
