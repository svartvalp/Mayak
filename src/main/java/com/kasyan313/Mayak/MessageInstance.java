package com.kasyan313.Mayak;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.kasyan313.Mayak.Models.Image;
import com.kasyan313.Mayak.Models.Message;
import com.kasyan313.Mayak.Models.Text;

import java.util.List;
import java.util.Map;

@JsonRootName(value = "message")

public class MessageInstance {
    Message messageInfo;
    Text text;
    List<Image> images;

    public MessageInstance() {
    }

    public Message getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(Message messageInfo) {
        this.messageInfo = messageInfo;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public MessageInstance(Message messageInfo, Text text, List<Image> images) {
        this.messageInfo = messageInfo;
        this.text = text;
        this.images = images;
    }
}
