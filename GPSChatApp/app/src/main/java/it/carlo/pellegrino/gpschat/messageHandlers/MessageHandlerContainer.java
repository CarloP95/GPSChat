package it.carlo.pellegrino.gpschat.messageHandlers;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import it.carlo.pellegrino.gpschat.imageUtils.ImageUtils;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MqttMessageEvent;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttBaseMessage;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttReplyMessage;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;

public class MessageHandlerContainer {

    private EventBus processEventBus = EventBus.getDefault();
    private HashMap<Long, MqttBaseMessage> mMQTTPayloadMessages;
    private HashMap<Long, Marker> mMessagesMarkers;
    private HashMap<String, MqttBaseMessage> mMarkerMQTTMessages;
    private HashMap<Long, MqttBaseMessage> mRecvMsgWithoutShout;
    private HashMap<Long, Integer> mRecvMsgWithoutShoutTTL;
    private GoogleMap mUiMap;
    private Handler mPeriodicHandlerForUpdates;

    private static int TTL = 5;
    private static int INTERVAL_PERIODIC_HANDLER_UPDATES_S = 10;

    private int previousHashCode = -1;

    public MessageHandlerContainer(GoogleMap mUiMap) {
        mMQTTPayloadMessages = new HashMap<>();
        mMarkerMQTTMessages = new HashMap<>();
        mMessagesMarkers = new HashMap<>();
        mRecvMsgWithoutShout = new HashMap<>();
        mRecvMsgWithoutShoutTTL = new HashMap<>();
        this.mUiMap = mUiMap;

        // Handler used to cache replies that has not a Shout counterpart
        this.mPeriodicHandlerForUpdates = new Handler();
        // TODO: Must cast if type is different than MQTTReplyMessage
        Runnable checkForNewShouts = new Runnable() {
            @Override
            public void run() {
                Log.v("GPSCHAT","Starting routine to clean or update the UI with reply messages");
                // Check for updates & Decrease TTL for those replies that has no shout counterpart
                mRecvMsgWithoutShout.forEach( (key, value) -> {
                    MqttReplyMessage reply = (MqttReplyMessage)value;
                    if (mMQTTPayloadMessages.containsKey(reply.getResponseTo())) {
                        Log.v("GPSCHAT", "Msg with id " + value.getId() + " in response to " + (reply.getResponseTo()) + " now has a shout.. Sending to display it.");
                        pushMessage(value);
                    } else {
                        mRecvMsgWithoutShoutTTL.put(key, mRecvMsgWithoutShoutTTL.get(key) - 1 );

                        // Delete replies that has 0 TTL
                        if (mRecvMsgWithoutShoutTTL.get(key) <= 0) {
                            Log.v("GPSCHAT", "Msg with ID " + value.getId() + " is being removed..");
                            mRecvMsgWithoutShoutTTL.remove(key);
                            mRecvMsgWithoutShout.remove(key);
                        }
                    }
                });

                mPeriodicHandlerForUpdates.postDelayed(this,INTERVAL_PERIODIC_HANDLER_UPDATES_S *1000);
            }
        };

        this.mPeriodicHandlerForUpdates.post(checkForNewShouts);
    }

    public MqttBaseMessage getMessageFromMarker (Marker m) {
        return mMarkerMQTTMessages.get(m.getId());
    }

    public MqttBaseMessage getMessageFromID (Long id) {
        return mMQTTPayloadMessages.get(id);
    }

    public MessageHandlerContainer pushMessage(MqttBaseMessage msg) {

        MqttBaseMessage alreadyPresentMessage = mMQTTPayloadMessages.get(msg.getId());
        switch (msg.getType()) {
            case MqttBaseMessage.TYPE_SHOUT:

                if (alreadyPresentMessage == null) {
                    // Display in map
                    Marker m = handleNotifyUI(false, msg);
                } else {
                    Log.i("GPSCHAT", "A message that is already present, returned as TYPE SHOUT. From Network Message: " + msg.toString() + "\nOld Message: " + alreadyPresentMessage.toString());
                }

                break;

            case MqttBaseMessage.TYPE_UPDATE:

                if (alreadyPresentMessage != null) {
                    // Delete from Repositories
                    Marker m = mMessagesMarkers.remove(alreadyPresentMessage.getId());
                    MessageHandlerComponent.removeMarker(m);
                    mMarkerMQTTMessages.remove(m.getId());
                    mMQTTPayloadMessages.remove(alreadyPresentMessage.getId());

                    m = handleNotifyUI(false, msg);

                } else {
                    Log.i("GPSCHAT", "A message that is not present arrived as TYPE UPDATE. From Network Message: " + msg.toString());
                    mRecvMsgWithoutShout.put(msg.getId(), msg);
                    mRecvMsgWithoutShoutTTL.put(msg.getId(), TTL);
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
                    mRecvMsgWithoutShout.put(msg.getId(), msg);
                    mRecvMsgWithoutShoutTTL.put(msg.getId(), TTL);
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

        int currentHashCode = this.mMQTTPayloadMessages.hashCode();

        if (force || currentHashCode != previousHashCode) {
            notifyUI(msg);

            UpdateMapTask t = new UpdateMapTask(mUiMap, (MqttShoutMessage)msg);
            t.execute((Void) null);
        }

        this.previousHashCode = currentHashCode;

        return null;
    }

    private void notifyUI(MqttBaseMessage msg) {
        processEventBus.post(new MqttMessageEvent(msg));
    }

    private static class MessageHandlerComponent {


        private static void removeMarker(Marker m) {
            m.hideInfoWindow();
            m.remove();
        }

    }

    private class UpdateMapTask extends AsyncTask<Void, Void, MarkerOptions> {

        private GoogleMap mMap;
        private MqttShoutMessage mMsg;

        public UpdateMapTask(GoogleMap map, MqttShoutMessage msg) {
            mMap = map;
            mMsg = msg;
        }

        @Override
        protected MarkerOptions doInBackground(Void... params) {

            String avatarUrl = mMsg.getResources().get(0);

            MarkerOptions mo = new MarkerOptions()
                    .title(mMsg.getNickname())
                    .snippet(mMsg.getMessage())
                    .position(mMsg.getLocation())
                    .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmap(avatarUrl)));

            return mo;
        }

        @Override
        protected void onPostExecute(final MarkerOptions success) {
            Marker m = mMap.addMarker(success);

            mMQTTPayloadMessages.put(mMsg.getId(), mMsg);
            mMarkerMQTTMessages.put(m.getId(), mMsg);
            mMessagesMarkers.put(mMsg.getId(), m);
        }

        @Override
        protected void onCancelled() {
            Log.e("GPSCHAT", "Cancelled");
        }
    }
}
