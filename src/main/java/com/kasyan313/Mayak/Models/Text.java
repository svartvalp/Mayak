package com.kasyan313.Mayak.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "text")
@JsonRootName(value = "text")
public class Text {

    @Column(name = "text_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int textId;
    @Column(name = "text")
    @JsonProperty("text")
    String text;
    @Column(name = "message_id")
    @NotNull
    private int messageId;
    public Text() {
    }

    public Text(String text) {
        this.text = text;
    }

    public int getTextId() {
        return textId;
    }

    public void setTextId(int textId) {
        this.textId = textId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
