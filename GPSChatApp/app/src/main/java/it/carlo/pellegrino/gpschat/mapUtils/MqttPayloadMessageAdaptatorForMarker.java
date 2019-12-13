package it.carlo.pellegrino.gpschat.mapUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttShoutMessage;

public class MqttPayloadMessageAdaptatorForMarker {

    private static int circleInternalColorAlpha = 0xA0000000;
    private static int circleInternalColorBack  = 0xC2C5BB; //Silver
    private static int circleBorderColorAlpha   = 0xA0000000;
    private static int circleBorderColorBack    = 0x3A445D; //Charcoal

    public static Marker adapt(GoogleMap map, MqttShoutMessage msg) {

        return map.addMarker(new MarkerOptions()
                .position(msg.getLocation())
                .title(msg.getNickname())
                .snippet(msg.getMessage())
        );
    }


    public static Circle drawCircle(GoogleMap map, LatLng currentLatLng, double radius) {
        return map.addCircle(new CircleOptions()
                .center(currentLatLng)
                .radius(radius)
                .strokeColor(circleBorderColorAlpha + circleBorderColorBack)
                .fillColor(circleInternalColorAlpha + circleInternalColorBack)
        );
    }

}
