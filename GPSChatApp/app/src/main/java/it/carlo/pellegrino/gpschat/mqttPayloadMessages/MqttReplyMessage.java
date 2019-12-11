package it.carlo.pellegrino.gpschat.mqttPayloadMessages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "id",
        "nickname",
        "message",
        "resources",
        "timestamp",
        "#replies",
        "replies",
        "revision"
})

public class MqttReplyMessage extends MqttBaseMessage {

    private String NUMREPLIES_STRING = "#replies";
    private String REPLIES_STRING    = "replies";
    private String RESPONSETO_STRING = "responseTo";

    @JsonProperty("#replies")
    private Long numReplies;
    @JsonProperty("replies")
    private List<MqttReplyMessage> replies;
    @JsonProperty("responseTo")
    private Long responseTo;

    public MqttReplyMessage(String payload) {
        super(payload);

        try {
            JSONObject jsonPayload = new JSONObject(payload);
            JSONArray replies      = jsonPayload.getJSONArray(REPLIES_STRING);

            this.numReplies        = jsonPayload.getLong(NUMREPLIES_STRING);
            this.responseTo        = jsonPayload.getLong(RESPONSETO_STRING);
            this.replies           = new ArrayList<>();

            for (int idx = 0; idx < replies.length(); ++idx)
                this.replies.add((MqttReplyMessage)replies.get(idx));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JsonProperty("responseTo")
    public Long getResponseTo() {
        return responseTo;
    }

    @JsonProperty("responseTo")
    public void setResponseTo(Long responseTo) {
        this.responseTo = responseTo;
    }

    @JsonProperty("#replies")
    public Long getNumReplies() {
        return numReplies;
    }

    @JsonProperty("#replies")
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
                "ResponseTo: " + this.getResponseTo() + "\n" +
                "#Replies: " + this.getNumReplies() + "\n" +
                "Replies: " + this.getReplies().toString() + "\n" +
                "Resources: " + this.getResources().toString() + "\n";
    }
}
