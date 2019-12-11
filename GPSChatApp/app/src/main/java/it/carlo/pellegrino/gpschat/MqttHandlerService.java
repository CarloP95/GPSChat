package it.carlo.pellegrino.gpschat;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MainActivityMessageEvent;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MqttMessagePayloadEvent;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.WrapperEventForMessageHandler;
import it.carlo.pellegrino.gpschat.messageHandlers.MessageHandlerContainer;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;
import it.carlo.pellegrino.gpschat.topicUtils.TopicFilterBuilder;

/* TODO: Add to the documentation that the MQTTBroker has an explicit violation of the Standard MQTT 3.1.1 at 3.3.2.1 Topic Name. */
public class MqttHandlerService extends Service implements MqttCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private IMqttToken clientConnectedToken;
    private String clientId;
    private String url;
    private String topicFilter;
    private final int qos = 0;

    private SharedPreferences preferences;

    private final TopicFilterBuilder topicBuilder  = new TopicFilterBuilder(null);
    private MessageHandlerContainer messageBrokerWithUi = null;
    private IMqttToken         subscribeToken      = null;

    private EventBus processEventBus               = EventBus.getDefault();

    private final IBinder mBinder                  = new MqttHandlerBinder();

    @Override
    public void onCreate() {

        processEventBus.register(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        Log.v("GPSCHAT", "MqttHandlerService has been created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.clientId = MqttClient.generateClientId();
        this.options  = new MqttConnectOptions();
        this.url      = preferences.getString(getResources().getString(R.string.key_mqtt_url),
                intent.getExtras().getString(MainActivity.URL_KEY));

        this.options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        this.options.setCleanSession(true);

        String username_sessionId = intent.getExtras().getString(MainActivity.SES_KEY);
        String password_token     = intent.getExtras().getString(MainActivity.TKN_KEY);
        options.setUserName(username_sessionId);
        options.setPassword(password_token.toCharArray());

        this.client = new MqttAndroidClient(this, url, clientId);

        try {
            IMqttToken connectToken = client.connect(options);
            connectToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    onConnectionSuccess(asyncActionToken);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    onConnectionFailure(asyncActionToken, exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        this.messageBrokerWithUi = processEventBus.getStickyEvent(WrapperEventForMessageHandler.class).getMessageHandler();
        processEventBus.removeStickyEvent(WrapperEventForMessageHandler.class);

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    private void onConnectionSuccess(IMqttToken asyncActionToken) {
        Log.v("GPSCHAT", "Connected with MQTT broker.");
        clientConnectedToken = asyncActionToken;
        client.setCallback(this);

        MainActivityMessageEvent msg = processEventBus.getStickyEvent(MainActivityMessageEvent.class);
        onMainActivityMessageReceived(msg);
        processEventBus.removeStickyEvent(MainActivityMessageEvent.class);
    }

    private void onConnectionFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.e("GPSCHAT", exception.getLocalizedMessage());
        Log.e("GPSCHAT", asyncActionToken.getException().getMessage());
        //processEventBus.post(new MqttMessageEvent("!Connected with the MQTT broker."));
    }

    private void subscribeToTopicFilter() {

        if (clientConnectedToken != null) {

            try {
                subscribeToken = client.subscribe(topicFilter, qos);
                subscribeToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.v("GPSCHAT", "Subscribed correctly with the topicFilter: " + topicFilter);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.v("GPSCHAT", "Something is wrong with the subscription that has topic: " + topicFilter);
                    }
                });
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMainActivityMessageReceived(MainActivityMessageEvent message) {
        Log.v("GPSCHAT", "Received message from Main Activity ");
        String currentTopicFilter = updateTopicBuilderFromSharedPreferences().location(message.getLocation()).build();

        if (!currentTopicFilter.equals(this.topicFilter)) {
            if (subscribeToken != null && clientConnectedToken != null) {
                try {
                    client.unsubscribe(this.topicFilter);
                } catch (MqttException e) {
                    Log.e("GPSCHAT", "Tried to unsubscribe to previous topic. Error is: " + e.getMessage());
                }
            }
            this.topicFilter = currentTopicFilter;
            subscribeToTopicFilter();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onToPublishMessageReceived(MqttMessagePayloadEvent evt) {
        Log.v("GPSCHAT", "Received Message from UI to publish.");
        ObjectMapper mapper = new ObjectMapper();

        try {

            String jsonPayload = mapper.writeValueAsString(evt.getPayloadMessage());
            Log.v("GPSCHAT", "Sending Payload: " + jsonPayload);
            byte[] payload = jsonPayload.getBytes();
            client.publish(topicFilter, payload, qos, false);

        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e("GPSCHAT", cause.getLocalizedMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        MqttShoutMessage unMarshalledMqttMessage = new MqttShoutMessage(new String(message.getPayload()));
        messageBrokerWithUi.pushMessage(unMarshalledMqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void onDestroy() {
        try {
            client.unsubscribe(this.topicFilter);
            client.disconnect();
        } catch (MqttException e) {
            Log.e("GPSCHAT", "Service is closing resources but an error occurred: " + e.getMessage());
        }
        processEventBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        onMainActivityMessageReceived(new MainActivityMessageEvent()); // TopicBuilder will not be updated
        // since the default policy is to ignore null values.
    }

    public class MqttHandlerBinder extends Binder {
        public MqttHandlerService getServiceInstance() {
            return MqttHandlerService.this;
        }
    }

    private TopicFilterBuilder updateTopicBuilderFromSharedPreferences() {
        int radius    = preferences.getInt(getResources().getString(R.string.key_shout_radius), 1000),
             timestamp = preferences.getInt(getResources().getString(R.string.key_timestamp), 60);

        return topicBuilder
                .radius(String.valueOf(radius))
                .shape(preferences.getString(getResources().getString(R.string.key_shape), ""))
                .timestamp(String.valueOf(timestamp))
                .unit("m"); // Will implement in Future.
    }
}
