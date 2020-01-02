package it.carlo.pellegrino.gpschat;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import it.carlo.pellegrino.gpschat.mqttPayloadMessages.MqttBaseMessage;

public class ChatInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private MqttBaseMessage mMsgToDisplay;
    private LayoutInflater mInflater;
    private Context mContext;
    private String mNickname;

    private boolean not_first_time_showing_info_window;

    public ChatInfoWindowAdapter(Context c, MqttBaseMessage msg, String nickname) {
        this.mContext = c;
        this.mMsgToDisplay = msg;
        this.mNickname = nickname;
        this.not_first_time_showing_info_window = false;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View visualize = mInflater.inflate(R.layout.info_window_msg, null);

        int color = 0x424242;
        int transparency = 0xEB000000;

        visualize.setBackground(new ColorDrawable(transparency + color));

        TextView nicknameTV = visualize.findViewById(R.id.imgTextView),
                dateTV = visualize.findViewById(R.id.imgDateView);
        ImageView imgView   = visualize.findViewById(R.id.imgImgView);

        if (mMsgToDisplay != null) {
            String msgNickname = mMsgToDisplay.getNickname();
            nicknameTV.setText(msgNickname.equals(mNickname) ? "You" : msgNickname);
            dateTV.setText(mMsgToDisplay.getTimestamp());
            // Get(1) MUST NOT return null, this Adapter is used only when more than one resource is present.
            String urlOfImgToDisplay = mMsgToDisplay.getResources().get(1);
            if (urlOfImgToDisplay.equals("")) {
                // Default user photo is already in the layout
                //shouterAvatar.setBackgroundResource(R.drawable.userninjasolid); //Default User Photo
            } else {
                if (not_first_time_showing_info_window) {
                    Log.v("GPSCHAT", "Download Avatar and then display it.");
                    Picasso.get().load(urlOfImgToDisplay).resize(50, 50).into(imgView);
                } else {
                    not_first_time_showing_info_window = true;
                    Picasso.get().load(urlOfImgToDisplay).resize(50, 50).into(imgView, new CustomInfoWindowUpdater(marker));
                }
            }

        }

        return visualize;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private class CustomInfoWindowUpdater implements Callback {
        private Marker mMarkerToUpdate;

        private CustomInfoWindowUpdater(Marker m) {
            this.mMarkerToUpdate = m;
        }

        @Override
        public void onSuccess() {
            Log.v("GPSCHAT","Called on success");
            if (mMarkerToUpdate.isInfoWindowShown()) {
                mMarkerToUpdate.hideInfoWindow();
                mMarkerToUpdate.showInfoWindow();
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.e("GPSCHAT",ex.getLocalizedMessage());
        }
    }
}
