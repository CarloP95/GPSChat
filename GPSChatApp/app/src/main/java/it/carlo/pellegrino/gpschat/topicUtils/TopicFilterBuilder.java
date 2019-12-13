package it.carlo.pellegrino.gpschat.topicUtils;

import android.location.Location;
import android.util.Log;

public class TopicFilterBuilder {

    private String unit;
    private String shape;
    private String radius;
    private String timestamp;
    private Location location;
    private String baseTopicFilter = "topics/loc/";

    private static final String topicSeparator = "&";
    private static final String timestampKey   = "t";
    private static final String locationKey    = "ll";
    private static final String radiusKey      = "r";
    private static final String shapeKey       = "s";
    private static final String unitKey        = "u";
    private static final String equalKey       = "=";

    public TopicFilterBuilder(String baseTopicFilter) {
        this.baseTopicFilter = !baseTopicFilter.equals("") ?
                                            baseTopicFilter:
                                            this.baseTopicFilter;
    }

    /*
    * Copy constructor
    * */
    public TopicFilterBuilder(TopicFilterBuilder copy) {
        this.unit = copy.unit;
        this.shape = copy.shape;
        this.radius = copy.radius;
        this.location = copy.location;
        this.timestamp = copy.timestamp;
        this.baseTopicFilter = copy.baseTopicFilter;
    }

    public TopicFilterBuilder unit(String unit) {
        this.unit = unit;
        return this;
    }

    public TopicFilterBuilder shape(String shape) {
        this.shape = shape;
        return this;
    }

    public TopicFilterBuilder radius(String radius) {
        this.radius = radius;
        return this;
    }

    public TopicFilterBuilder timestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public TopicFilterBuilder location(Location location) {
        if (location != null)
            this.location = location;

        return this;
    }

    public String build() {
        try {
            return baseTopicFilter +
                    locationKey + equalKey + location.getLatitude() + "," + location.getLongitude() + topicSeparator +
                    shapeKey + equalKey + shape + topicSeparator +
                    radiusKey + equalKey + radius + topicSeparator +
                    unitKey + equalKey + unit + topicSeparator +
                    timestampKey + equalKey + timestamp;
        } catch (NullPointerException ex) {
            Log.e("GPSCHAT", "You are invoking Build with Location field not set. This thrown Null Pointer Exception. Maybe something is wrong with your connection with MQTT Broker.");
            return "";
        }
    }

}
