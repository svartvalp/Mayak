package com.kasyan313.Mayak.Models;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "message")
@JsonRootName(value = "message_info")
public class Message {
    @Column(name = "message_id", unique = true, insertable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int messageId;
    @Column(name = "from_id")
    @JsonProperty("from")
    @NotNull
    private int fromId;
    @Column(name = "to_id")
    @JsonProperty("to")
    @NotNull
    private int toId;
    @Column(name = "message_timestamp")
    @JsonProperty("timestamp")
    private Timestamp messageTimestamp;
    @Column(name = "checked")
    private boolean checked;

    public Message(int fromId, int toId, Timestamp messageTimestamp) {
        this.fromId = fromId;
        this.toId = toId;
        this.messageTimestamp = messageTimestamp;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @JsonProperty("message_id")
    public int getMessageId() {
        return messageId;
    }

    @JsonIgnore
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int from) {
        this.fromId = from;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int to) {
        this.toId = to;
    }

    public Timestamp getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(Timestamp timestamp) {
        this.messageTimestamp = timestamp;
    }

    public Message() {

    }
}
