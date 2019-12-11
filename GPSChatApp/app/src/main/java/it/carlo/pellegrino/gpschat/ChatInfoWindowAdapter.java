package it.carlo.pellegrino.gpschat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class ChatInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater inflater;
    Context context;

    public ChatInfoWindowAdapter(Context c) {
        this.context = c;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View visualize = inflater.inflate(R.layout.info_window_msg, null);
        return visualize;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
