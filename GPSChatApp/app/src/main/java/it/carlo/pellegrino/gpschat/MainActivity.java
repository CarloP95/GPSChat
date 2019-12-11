package it.carlo.pellegrino.gpschat;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import it.carlo.pellegrino.gpschat.imageUtils.ImageUtils;
import it.carlo.pellegrino.gpschat.mapUtils.MqttPayloadMessageAdaptatorForMarker;
import it.carlo.pellegrino.gpschat.mapUtils.UnitConverter;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MainActivityMessageEvent;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MqttMessageEvent;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.MqttMessagePayloadEvent;
import it.carlo.pellegrino.gpschat.messageBusMessageEvents.WrapperEventForMessageHandler;
import it.carlo.pellegrino.gpschat.messageHandlers.MessageHandlerContainer;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttBaseMessage;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttReplyMessage;
import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager mGPSManager;

    private FloatingActionButton mPublishButton;
    private FloatingActionButton mSettingsButton;
    private TextView mPublishMessage;
    
    public static final String URL_KEY    = "geomqtt_url";
    public static final String TKN_KEY    = "token";
    public static final String SES_KEY    = "sessionId";


    private String chosenRadius;
    private String chosenUnit;
    private Location currentLocation      = null;
    private Circle currentPublishCircle   = null;

    private SharedPreferences preferences;
    private String            nickname;

    private int FINE_LOC_PERMISSION   = PackageManager.PERMISSION_DENIED;
    private int COARSE_LOC_PERMISSION = PackageManager.PERMISSION_DENIED;

    private static int zoom = 150;
    private boolean backPressedOneTime = false;
    private static List<String> mOpenInfoWindows = null;


    private EventBus processEventBus = EventBus.getDefault();
    private Context currentContext = null;
    private MessageHandlerContainer messageHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGPSManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener gpschatLatLonProvider = new LocListener();

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOC_PERMISSION);
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOC_PERMISSION);
        }

        preferences  = getSharedPreferences(LoginActivity.pref_string, Context.MODE_PRIVATE);
        nickname     = preferences.getString(LoginActivity.pref_string_nickname, "");
        // Implement on Change Listener for preferences
        chosenRadius = preferences.getString(getResources().getString(R.string.key_shout_radius), "");
        chosenUnit   = "m"; //Will implement in future
        Log.i("GPSCHAT", "Chosen Radius is: " + chosenRadius);
        mGPSManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, gpschatLatLonProvider);
        mOpenInfoWindows = new LinkedList<>();

        mPublishMessage = findViewById(R.id.textInputPublishMessage);
        mPublishButton  = findViewById(R.id.publish_button);
        mPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage();
                mPublishMessage.setText("");
            }
        });

        mSettingsButton = findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(currentContext, SettingsActivity.class);
                startActivity(i);
            }
        });
        addCallbackForBackPressed();
        currentContext = getApplicationContext();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        //mMap.setInfoWindowAdapter(new ChatInfoWindowAdapter(this));

        displayCurrentPositionAndSubscribeRadius();

        Intent launchServiceIntent = new Intent(this, MqttHandlerService.class);
        launchServiceIntent.putExtra(URL_KEY, "tcp://0.tcp.ngrok.io:10658");
        launchServiceIntent.putExtra(TKN_KEY, preferences.getString(LoginActivity.pref_string_token, ""));
        launchServiceIntent.putExtra(SES_KEY, preferences.getString(LoginActivity.pref_string_sessionId, ""));
        startService(launchServiceIntent);

        processEventBus.register(this);
        processEventBus.postSticky(new MainActivityMessageEvent()
                .setLocation(currentLocation)
        );
        messageHandler = new MessageHandlerContainer(mMap);
        processEventBus.postSticky(new WrapperEventForMessageHandler(messageHandler));
        messageHandler.pushMessage(new MqttShoutMessage(new MqttBaseMessage.Builder()
                .id(1)
                .message("Hello, this is my current position")
                .nickname(nickname)
                .resources("")
                .revision(0L)
                .type(MqttBaseMessage.TYPE_SHOUT)
                .timestamp(new Date().toString())
                .build(),
                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
                )
        );
        messageHandler.pushMessage(new MqttReplyMessage(new MqttBaseMessage.Builder()
                .id(2)
                .message("Hello " + nickname + ", my name is John.")
                .nickname("John")
                .resources("")
                .revision(0L)
                .type(MqttBaseMessage.TYPE_REPLY)
                .timestamp(new Date().toString())
                .build(),
                1L
                )
        );


    }

    private void displayCurrentPositionAndSubscribeRadius() {
        currentLocation = getLastLocationFromProviders();
        if (currentLocation == null) {
            currentLocation = new Location("");
            currentLocation.setLongitude(12.48870849609375);
            currentLocation.setLatitude(41.88592102814744);
        }

        if (currentLocation != null) {
            Bitmap image = ImageUtils.getTransparentImage("https://upload.wikimedia.org/wikipedia/it/e/ee/Logo_Vodafone_new.png");

            LatLng currentLtLn = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            Marker f = mMap.addMarker(new MarkerOptions()
                    .position(currentLtLn)
                    .title("Current Position")
                    .snippet("This is your current position")
                    .icon(BitmapDescriptorFactory.fromBitmap(image))
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLtLn, 10));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLtLn));

            if (currentPublishCircle != null)
                currentPublishCircle.remove();

            currentPublishCircle = MqttPayloadMessageAdaptatorForMarker.drawCircle(mMap, currentLtLn, UnitConverter.convertInMeters(chosenRadius, chosenUnit));
        }
    }

    private void publishMessage () {

        MqttBaseMessage.Builder builder = new MqttShoutMessage.Builder();
        MqttBaseMessage baseToSend = builder
                .message(mPublishMessage.getText().toString())
                .nickname(nickname)
                .resources("") //Will set resources in future
                .id(UUID.randomUUID().getLeastSignificantBits()) // Temporary implementation
                .build();

        MqttShoutMessage toSend = new MqttShoutMessage(baseToSend, new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

        processEventBus.post(new MqttMessagePayloadEvent(toSend));
    }

    private Location getLastLocationFromProviders() {
        Location currentPos = null;

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            currentPos = mGPSManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentPos == null)
                currentPos = mGPSManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return currentPos;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popup = inflater.inflate(R.layout.shout_responses_layout, null);
        int width  = LinearLayout.LayoutParams.MATCH_PARENT,
                height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;

        PopupWindow window = new PopupWindow(popup, width, height, focusable);
        window.setElevation(5.0f);
        int color = 0x8A817C;
        int transparency = 0xEB000000;
        window.setBackgroundDrawable(new ColorDrawable(transparency + color));

        MqttBaseMessage msg = messageHandler.getMessageFromMarker(marker);

        TextView nicknameTV = popup.findViewById(R.id.nickname_shout_text_view),
                dateTV = popup.findViewById(R.id.date_shout_text_view),
                messageTV = popup.findViewById(R.id.shout_message_text_view);

        if (msg != null) {
            String msgNickname = msg.getNickname();
            nicknameTV.setText(msgNickname.equals(nickname) ? "You" : msgNickname);
            dateTV.setText(msg.getTimestamp());
            messageTV.setText(msg.getMessage());
        }

        window.showAtLocation(this.getCurrentFocus(), Gravity.CENTER, 0, 0);

    }

    // TODO: Move to the component that will have the responsibility to display Markers
    @Override
    public boolean onMarkerClick(Marker marker) {

        if (!mOpenInfoWindows.contains(marker.getId())) {
            mOpenInfoWindows.add(marker.getId());
            marker.showInfoWindow();
        }
        else {
            mOpenInfoWindows.remove(mOpenInfoWindows.indexOf(marker.getId()));
            marker.hideInfoWindow();
        }

        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageReceived(MqttMessageEvent message) {
        Log.v("GPSCHAT", "Received message from MQTTService");
        Toast.makeText(this, message.getPayloadMessage().toString(), Toast.LENGTH_SHORT).show();

        //MqttPayloadMessageAdaptatorForMarker.adapt(mMap, message.getPayloadMessage());
        // Handled by MessageHandlerComponent
    }

    private void addCallbackForBackPressed () {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (!backPressedOneTime)
                    backPressedOneTime = !backPressedOneTime;
                else
                    finishAndRemoveTask();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private class LocListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            Marker first = mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .title("Current Position")
                    .snippet("This is your current position")
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoom));

            Toast.makeText(
                    getApplicationContext(),
                    "Location changed: Lat: " + location.getLatitude() + " Lng: "
                            + location.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + location.getLongitude();
            Log.v("GPSCHAT", longitude);
            String latitude = "Latitude: " + location.getLatitude();
            Log.v("GPSCHAT", latitude);

            /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                    + cityName;
            Log.v("GPSCHAT", s);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
