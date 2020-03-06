package com.kasyan313.Mayak.Controllers;

import com.kasyan313.Mayak.MessageInstance;
import com.kasyan313.Mayak.Models.Image;
import com.kasyan313.Mayak.Services.IMessageService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class MessageController {
    @Autowired
    SessionFactory sessionFactoryBean;
    @Autowired
    IMessageService messageService;

    @GetMapping(value = "/message/{id}")
    public MessageInstance getMessageInstance(@PathVariable("id") int id) {
        return messageService.getMessageInstanceById(id);
    }
    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MessageInstance createMessage(@RequestBody MessageInstance instance) {
        return messageService.createMessage(instance);
    }
    @GetMapping(value = "/image/{id}")
    public ResponseEntity<byte[]> loadImage(@PathVariable("id") int id) {
        Image image = messageService.getImageById(id);
        HttpHeaders httpHeaders = new HttpHeaders();
        if(image.getType() == null) {
            httpHeaders.set("Content-Type", "image/jpeg");
        } else {
            httpHeaders.set("Content-Type", image.getType());
        }
        return ResponseEntity.ok().headers(httpHeaders).body(image.getSource());
    }
    @PostMapping(value = "/image/{id}")
    public @ResponseBody int uploadImage(@PathVariable("id") int id, @RequestBody byte[] source) {
        return messageService.uploadImage(id, source);
    }
    @GetMapping(value = "/message/timestamp/{timestamp}")
    public List<MessageInstance> getMessagesAfterTimestamp(@PathVariable("timestamp")long milis,
                                                           @RequestParam(name = "mainUserId") int mainUserId,
                                                           @RequestParam(name = "anotherUserId") int anotherUserId) {
        Timestamp timestamp = new Timestamp(milis);
        return messageService.getMessagesAfterTimestamp(timestamp, mainUserId, anotherUserId);
    }
}
