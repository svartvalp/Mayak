package com.kasyan313.Mayak.Models;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

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
    private int from;
    @Column(name = "to_id")
    @JsonProperty("to")
    @NotNull
    private int to;
    @Column(name = "message_timestamp")
    @JsonProperty("timestamp")
    private Timestamp timestamp;
    @Column(name = "checked")
    private boolean checked;

    public Message(int from, int to, Timestamp timestamp) {
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
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

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Message() {

    }
}
