package it.carlo.pellegrino.gpschat.messageBusMessageEvents;

import android.location.Location;

public class MainActivityMessageEvent {

    private String radius;
    private String timestamp;
    private String shape;
    private String unit;
    private Location location;

    public MainActivityMessageEvent() {

    }

    public String getRadius() { return this.radius; }

    public String getTimestamp() { return this.timestamp; }

    public String getShape() { return this.shape; }

    public String getUnit() { return this.unit; }

    public Location getLocation() { return this.location; }

    public MainActivityMessageEvent setRadius(String radius) {
        this.radius = radius;
        return this;
    }

    public MainActivityMessageEvent setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MainActivityMessageEvent setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public MainActivityMessageEvent setShape(String shape) {
        this.shape = shape;
        return this;
    }

    public MainActivityMessageEvent setLocation(Location loc) {
        this.location = loc;
        return this;
    }
}
