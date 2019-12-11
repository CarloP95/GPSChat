package it.carlo.pellegrino.gpschat.mqttPayloadMessages;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Base Class of messages.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "nickname",
        "message",
        "resources",
        "revision",
        "timestamp"
})
public class MqttBaseMessage {

    private static String NICKNAME_STRING     = "nickname";
    private static String MESSAGE_STRING      = "message";
    private static String ID_STRING           = "id";
    private static String TYPE_STRING         = "type";
    private static String REVISION_STRING     = "revision";
    private static String RESOURCES_STRING    = "resources";
    private static String TIMESTAMP_STRING    = "timestamp";

    public static final int TYPE_SHOUT  = 0x00;
    public static final int TYPE_UPDATE = 0x01;
    public static final int TYPE_REPLY  = 0x02;
    public static final int TYPE_DELETE = 0x03;

    @JsonProperty("type")
    protected int type;
    @JsonProperty("id")
    protected Long id;
    @JsonProperty("revision")
    protected Long revision;
    @JsonProperty("nickname")
    protected String nickname;
    @JsonProperty("message")
    protected String message;
    @JsonProperty("timestamp")
    protected String timestamp;
    @JsonProperty("resources")
    protected List<String> resources = null;
    @JsonIgnore
    protected Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public MqttBaseMessage(String payload) {
        try {
            JSONObject jsonPayload        = new JSONObject(payload);
            JSONArray  payloadResources   = jsonPayload.getJSONArray(RESOURCES_STRING);

            this.resources = new ArrayList<>();
            for (int idx = 0; idx < payloadResources.length(); ++idx)
                this.resources.add(payloadResources.get(idx).toString());
            this.timestamp = jsonPayload.getString(TIMESTAMP_STRING);
            this.message   = jsonPayload.getString(MESSAGE_STRING);
            this.nickname  = jsonPayload.getString(NICKNAME_STRING);
            this.id        = jsonPayload.getLong(ID_STRING);
            this.type      = jsonPayload.getInt(TYPE_STRING);
            this.revision  = jsonPayload.getLong(REVISION_STRING);

        }
        catch (JSONException ex) {
            Log.e("GPSCHAT", "Received string that is not a valid JSON : " + payload);
        }
    }

    /* Constructor that will be used from Builder */
    protected MqttBaseMessage(int type, Long revision, String timestamp, String message, String nickname, long id, List<String> resources) {

        this.timestamp = timestamp;
        this.resources = resources;
        this.revision = revision;
        this.message = message;
        this.nickname = nickname;
        this.type = type;
        this.id = id;
    }

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("nickname")
    public String getNickname() {
        return nickname;
    }

    @JsonProperty("nickname")
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @JsonProperty("revision")
    public Long getRevision() {
        return revision;
    }

    @JsonProperty("revision")
    public void setRevision(Long revision) {
        this.revision = revision;
    }

    @JsonProperty("type")
    public int getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(int type) {
        this.type = type;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp= timestamp;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("resources")
    public List<String> getResources() {
        return resources;
    }

    @JsonProperty("resources")
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @NonNull
    @Override
    public String toString() {
        return "Nickname: " + this.getNickname() + "\n" +
                "Message: " + this.getMessage() + "\n" +
                "Id: " + this.getId() + "\n" +
                "Type: " + this.getType() + "\n" +
                "Timestamp: " + this.getTimestamp() + "\n" +
                "Revision: " + this.getRevision() + "\n" +
                "Resources: " + this.getResources().toString() + "\n";
    }

    public static class Builder {

        protected long id;
        protected int type;
        protected long revision;
        protected String nickname;
        protected String message;
        protected String timestamp;
        protected List<String> resources;

        public Builder id (long id){
            this.id = id;
            return this;
        }

        public Builder nickname(String nickname){
            this.nickname = nickname;
            return this;
        }

        public Builder message (String message){
            this.message = message;
            return this;
        }

        public Builder revision (Long revision){
            this.revision = revision;
            return this;
        }

        public Builder type (int type) {
            this.type = type;
            return this;
        }

        public Builder timestamp (String timestamp) {
            this.timestamp= timestamp;
            return this;
        }

        public Builder resources (String... args){
            this.resources = Arrays.asList(args);
            return this;
        }

        public MqttBaseMessage build() {
            return new MqttBaseMessage(this.type, this.revision, this.timestamp, this.message, this.nickname, this.id, this.resources);
        }
    }
}
