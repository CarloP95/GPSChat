package it.carlo.pellegrino.gpschat.mqttPayloadMessages;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class that is used to handle in a simple way the unmarshalling of JSON objects coded into MqttMessage Payload.
 * Each object will be composed in the following way:
 * {
 *      "type": 0,
 *      "id" : --some Long value--,
 *      "nickname": --String--,
 *      "message": --String--,
 *      "resources": [
 *          "https://img1.com", "https://img2.com" ...
 *      ],
 *      "location": {
 *           "latitude": --some Double value--,
 *           "longitude": --some Double value--
 *      },
 *      "timestamp": "2 May 1995 - 16:30:0000",
 *      "revision": 0
 * }
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "id",
        "nickname",
        "message",
        "resources",
        "location",
        "timestamp",
        "numReplies",
        "replies",
        "revision"
})
public class MqttShoutMessage extends MqttBaseMessage {

    private static String LOCATION_STRING     = "location";
    private static String LATITUDE_STRING     = "latitude";
    private static String LONGITUDE_STRING    = "longitude";
    private static String NUMREPLIES_STRING   = "numReplies";
    private static String REPLIES_STRING      = "replies";

    @JsonProperty("location")
    private LatLng location;
    @JsonProperty("numReplies")
    private Long numReplies;
    @JsonProperty("replies")
    private List<MqttReplyMessage> replies;

    public MqttShoutMessage(String payload) {
        super(payload);
        try {
            JSONObject jsonPayload        = new JSONObject(payload);
            JSONObject loc                = jsonPayload.getJSONObject(LOCATION_STRING);
            JSONArray replies             = jsonPayload.getJSONArray(REPLIES_STRING);

            this.location   = new LatLng(loc.getDouble(LATITUDE_STRING), loc.getDouble(LONGITUDE_STRING));
            this.numReplies = jsonPayload.getLong(NUMREPLIES_STRING);
            this.replies    = new ArrayList<>();

            for (int idx = 0; idx < replies.length(); ++idx)
                this.replies.add((MqttReplyMessage)replies.get(idx));
        }
        catch (JSONException ex) {
            Log.e("GPSCHAT", "Received string that is not a valid JSON : " + payload);
        }
    }

    @JsonProperty("location")
    public LatLng getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(LatLng location) {
        this.location = location;
    }

    @JsonProperty("numReplies")
    public Long getNumReplies() {
        return numReplies;
    }

    @JsonProperty("numReplies")
    public void setNumReplies(Long numReplies) {
        this.numReplies = numReplies;
    }

    @JsonProperty("replies")
    public List<MqttReplyMessage> getReplies() {
        return replies;
    }

    @JsonProperty("replies")
    public void setReplies(List<MqttReplyMessage> replies) {
        this.replies = replies;
    }

    @NonNull
    @Override
    public String toString() {
        return "Nickname: " + this.getNickname() + "\n" +
                "Message: " + this.getMessage() + "\n" +
                "Id: " + this.getId() + "\n" +
                "Type: " + this.getType() + "\n" +
                "Revision: " + this.getRevision() + "\n" +
                "Timestamp: " + this.getTimestamp() + "\n" +
                "Location: " + this.getLocation().toString() + "\n" +
                "#Replies: " + this.getNumReplies() + "\n" +
                "Replies: " + this.getReplies().toString() + "\n" +
                "Resources: " + this.getResources().toString() + "\n";
    }

    /* Copy constructor to gain parameters from base message (previously built by Builder) */
    public MqttShoutMessage(MqttBaseMessage base, LatLng location) {

        super(base.type, base.revision, base.timestamp, base.message, base.nickname, base.id, base.resources);

        this.location   = location;
        this.numReplies = 0L;
        this.replies    = new ArrayList<>();
    }

}
