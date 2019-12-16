package it.carlo.pellegrino.gpschat;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

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
    private int FINE_LOC_PERMISSION   = PackageManager.PERMISSION_DENIED;
    private int COARSE_LOC_PERMISSION = PackageManager.PERMISSION_DENIED;


    private String mChosenRadius;
    private String mChosenUnit;
    private Location mCurrentLocation = null;
    private Circle mCurrentPublishCircle = null;

    private SharedPreferences mPreferences;
    private String mNickname;

    private static int mZoom = 150;
    private boolean mBackPressedOneTime = false;
    private static List<String> mOpenInfoWindows = null;


    private EventBus mProcessEventBus = EventBus.getDefault();
    private Context mCurrentContext = null;
    private MessageHandlerContainer mMessageHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader(this);
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

        mPreferences = getSharedPreferences(LoginActivity.pref_string, Context.MODE_PRIVATE);
        mNickname = mPreferences.getString(LoginActivity.pref_string_nickname, "");
        // Implement on Change Listener for preferences
        mChosenRadius = mPreferences.getString(getResources().getString(R.string.key_shout_radius), "1000");
        mChosenUnit = "m"; //Will implement in future
        Log.i("GPSCHAT", "Chosen Radius is: " + mChosenRadius);
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
                Intent i = new Intent(mCurrentContext, SettingsActivity.class);
                startActivity(i);
            }
        });
        addCallbackForBackPressed();
        mCurrentContext = getApplicationContext();
    }

    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Rome, Italy.
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
        launchServiceIntent.putExtra(URL_KEY, "tcp://ec2-52-90-157-176.compute-1.amazonaws.com:1883");
        launchServiceIntent.putExtra(TKN_KEY, mPreferences.getString(LoginActivity.pref_string_token, ""));
        launchServiceIntent.putExtra(SES_KEY, mPreferences.getString(LoginActivity.pref_string_sessionId, ""));
        startService(launchServiceIntent);

        mProcessEventBus.register(this);
        mProcessEventBus.postSticky(new MainActivityMessageEvent()
                .setLocation(mCurrentLocation)
        );
        mMessageHandler  = new MessageHandlerContainer(mMap);
        mProcessEventBus.postSticky(new WrapperEventForMessageHandler(mMessageHandler));
        mMessageHandler.pushMessage(new MqttShoutMessage(new MqttBaseMessage.Builder()
                .id(1)
                .message("Hello, this is my current position")
                .nickname(mNickname)
                .resources("https://upload.wikimedia.org/wikipedia/it/e/ee/Logo_Vodafone_new.png")
                .revision(0L)
                .type(MqttBaseMessage.TYPE_SHOUT)
                .timestamp(new Date().toString())
                .build(),
                new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())
                )
        );
        mMessageHandler.pushMessage(new MqttReplyMessage(new MqttBaseMessage.Builder()
                .id(2)
                .message("Hello " + mNickname + ", my name is John.")
                .nickname("John")
                .resources("")
                .revision(0L)
                .type(MqttBaseMessage.TYPE_REPLY)
                .timestamp(new Date().toString())
                .build(),
                1L
                )
        );
        mMessageHandler.pushMessage(new MqttReplyMessage(new MqttBaseMessage.Builder()
                        .id(3)
                        .message("Hello John! Welcome to GPSChat")
                        .nickname(mNickname)
                        .resources("")
                        .revision(0L)
                        .type(MqttBaseMessage.TYPE_REPLY)
                        .timestamp(new Date().toString())
                        .build(),
                        1L
                )
        );
        mMessageHandler.pushMessage(new MqttReplyMessage(new MqttBaseMessage.Builder()
                        .id(4)
                        .message("Hello Everyone! I am Paul.")
                        .nickname("Paul")
                        .resources("")
                        .revision(0L)
                        .type(MqttBaseMessage.TYPE_REPLY)
                        .timestamp(new Date().toString())
                        .build(),
                        1L
                )
        );
        mMessageHandler.pushMessage(new MqttReplyMessage(new MqttBaseMessage.Builder()
                        .id(5)
                        .message("And i'm Mario")
                        .nickname("Mario")
                        .resources("")
                        .revision(0L)
                        .type(MqttBaseMessage.TYPE_REPLY)
                        .timestamp(new Date().toString())
                        .build(),
                        1L
                )
        );
        mMessageHandler.pushMessage(new MqttReplyMessage(new MqttBaseMessage.Builder()
                        .id(6)
                        .message("I was just wondering if spamming with text does the text " +
                                "wrap in the next line, or if it produces some weird graphic effect. " +
                                "Let's test it with this looooooong message. Hi All! I'm the producer of this app.")
                        .nickname(mNickname)
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
        mCurrentLocation = getLastLocationFromProviders();
        if (mCurrentLocation == null) {
            mCurrentLocation = new Location("");
            mCurrentLocation.setLongitude(12.48870849609375);
            mCurrentLocation.setLatitude(41.88592102814744);
        }

        if (mCurrentLocation != null) {

            LatLng currentLtLn = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            // TODO: Send a message to display your current position with your avatar
            final MarkerOptions mo = new MarkerOptions()
                    .position(currentLtLn)
                    .title("Current Position")
                    .snippet("This is your current position");

            mMap.addMarker(mo);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLtLn, 10));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLtLn));

            if (mCurrentPublishCircle != null)
                mCurrentPublishCircle.remove();

            mCurrentPublishCircle = MqttPayloadMessageAdaptatorForMarker.drawCircle(mMap, currentLtLn, UnitConverter.convertInMeters(mChosenRadius, mChosenUnit));
        }
    }

    private void publishMessage () {

        MqttBaseMessage.Builder builder = new MqttShoutMessage.Builder();
        MqttBaseMessage baseToSend = builder
                .message(mPublishMessage.getText().toString())
                .nickname(mNickname)
                .resources("") //Will set resources in future
                .id(UUID.randomUUID().getLeastSignificantBits()) // Temporary implementation
                .timestamp(new Date().toString())
                .build();

        MqttShoutMessage toSend = new MqttShoutMessage(baseToSend, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

        mProcessEventBus.post(new MqttMessagePayloadEvent(toSend));
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

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popup = inflater.inflate(R.layout.shout_responses_layout, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT,
                height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;

        PopupWindow window = new PopupWindow(popup, width, height, focusable);
        window.setElevation(5.0f);
        int color = 0x424242;
        int transparency = 0xEB000000;
        window.setBackgroundDrawable(new ColorDrawable(transparency + color));

        final MqttBaseMessage msg = mMessageHandler.getMessageFromMarker(marker);

        TextView nicknameTV = popup.findViewById(R.id.nickname_shout_text_view),
                dateTV = popup.findViewById(R.id.date_shout_text_view),
                messageTV = popup.findViewById(R.id.shout_message_text_view);

        if (msg != null) {
            String msgNickname = msg.getNickname();
            nicknameTV.setText(msgNickname.equals(mNickname) ? "You" : msgNickname);
            dateTV.setText(msg.getTimestamp());
            messageTV.setText(msg.getMessage());

            View replyButton = popup.findViewById(R.id.replyButton);
            replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView writtenText = popup.findViewById(R.id.shout_toReplyText);
                    MqttReplyMessage reply = new MqttReplyMessage(new MqttBaseMessage.Builder()
                            .message(writtenText.getText().toString())
                            .id(UUID.randomUUID().getLeastSignificantBits())
                            .type(MqttBaseMessage.TYPE_REPLY)
                            .revision(0L)
                            .nickname(mNickname)
                            .timestamp(new Date().toString())
                            .resources("")
                            .build(),
                            msg.getId()
                    );
                    mProcessEventBus.post(new MqttMessagePayloadEvent(reply));
                    writtenText.setText("");
                }
            });
        }

        LinearLayout displayMessagesLayout = popup.findViewById(R.id.displayMessagesLayout);
        // Dynamically Display replyMessages
        List<View> toDisplayLayouts = new LinkedList<>();
        if (msg != null) {

            for (MqttReplyMessage replyMessage : ((MqttShoutMessage) msg).getReplies()) {

                int complexWidth = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        250,
                        getResources().getDisplayMetrics()
                );

                int complexMargin = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10,
                        getResources().getDisplayMetrics()
                );

                boolean itsYourMessage = replyMessage.getNickname().equals(mNickname);

                TextView forEach_dateTV = new TextView(this);
                height = LinearLayout.LayoutParams.WRAP_CONTENT;
                forEach_dateTV.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                forEach_dateTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                forEach_dateTV.setText(replyMessage.getTimestamp());

                LinearLayout llParent = new LinearLayout(this),
                        llChild = new LinearLayout(this);
                llParent.setOrientation(LinearLayout.VERTICAL);
                llParent.setGravity(itsYourMessage ? Gravity.END : Gravity.START);

                LinearLayout.LayoutParams llParentLayoutParams = new LinearLayout.LayoutParams(width, height);
                if (itsYourMessage) llParentLayoutParams.setMarginEnd(complexMargin * 2);
                llParent.setLayoutParams(llParentLayoutParams);

                if (!itsYourMessage) {

                    int dimensions = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            40,
                            getResources().getDisplayMetrics()
                    );

                    CircularImageView portrait = new CircularImageView(this);
                    LinearLayout.LayoutParams portraitLayoutParams = new LinearLayout.LayoutParams(dimensions, dimensions);
                    portraitLayoutParams.setMarginStart(complexMargin*2);
                    portrait.setLayoutParams(portraitLayoutParams);
                    String urlOfPortrait = msg.getResources().get(0);
                    if (urlOfPortrait.equals("")) {
                        portrait.setBackgroundResource(R.drawable.userninjasolid); //Default User Photo
                    } else {
                        // Implement set background with correct photo
                        portrait.setBackgroundResource(R.drawable.userninjasolid); //Default User Photo
                    }
                    llParent.addView(portrait);
                }

                llChild.setOrientation(LinearLayout.VERTICAL);
                llChild.setBackgroundResource(itsYourMessage ? R.drawable.skyblue : R.drawable.azureishwhite);

                LinearLayout.LayoutParams llChildLayoutParams = new LinearLayout.LayoutParams(complexWidth, height);
                llChildLayoutParams.setMargins(complexMargin, 0,0, complexMargin);
                llChild.setLayoutParams(llChildLayoutParams);
                llChild.setPadding(complexMargin, complexMargin, complexMargin, complexMargin);

                TextView forEach_Nickname = new TextView(this);
                TextView forEach_Message = new TextView(this);
                forEach_Nickname.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                forEach_Nickname.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                forEach_Nickname.setTypeface(forEach_Nickname.getTypeface(), Typeface.BOLD);
                forEach_Nickname.setText(itsYourMessage ? "You" : replyMessage.getNickname());
                forEach_Nickname.setTextColor(Color.BLACK);
                forEach_Message.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                //forEach_Message.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                //forEach_Message.setTypeface(forEach_Message.getTypeface(), Typeface.BOLD);
                forEach_Message.setText(replyMessage.getMessage());
                forEach_Message.setTextColor(Color.BLACK);
                llChild.addView(forEach_Nickname);
                llChild.addView(forEach_Message);
                llParent.addView(llChild);

                toDisplayLayouts.add(forEach_dateTV);
                toDisplayLayouts.add(llParent);

            }
        }

        for (View ll: toDisplayLayouts)
            displayMessagesLayout.addView(ll);

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
                if (!mBackPressedOneTime)
                    mBackPressedOneTime = !mBackPressedOneTime;
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, mZoom));

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
