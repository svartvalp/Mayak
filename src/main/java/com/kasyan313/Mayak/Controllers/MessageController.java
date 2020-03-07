package com.kasyan313.Mayak.Controllers;

import com.kasyan313.Mayak.MessageInstance;
import com.kasyan313.Mayak.Models.Image;
import com.kasyan313.Mayak.Models.Text;
import com.kasyan313.Mayak.Services.IMessageService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
public class MessageController {
    @Autowired
    SessionFactory sessionFactoryBean;
    @Autowired
    IMessageService messageService;

    @GetMapping(value = "/message/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public MessageInstance getMessageInstance(@PathVariable("id") int id) {
        return messageService.getMessageInstanceById(id);
    }

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageInstance createMessage(@RequestBody MessageInstance instance) {
        return messageService.createMessage(instance);
    }

    @GetMapping(value = "/image/{id}")
    @ResponseStatus(HttpStatus.OK)
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
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody int uploadImage(@PathVariable("id") int id, @RequestBody byte[] source) {
        return messageService.uploadImage(id, source);
    }
    @GetMapping(value = "/message/user/{mainUserId}/{anotherUserId}/messages/timestamp/after/{timestamp}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<MessageInstance> getMessagesAfterTimestamp(@PathVariable("timestamp")long milis,
                                                           @PathVariable("mainUserId") int mainUserId,
                                                           @PathVariable("anotherUserId") int anotherUserId) {
        Timestamp timestamp = new Timestamp(milis);
        return messageService.getMessagesAfterTimestamp(timestamp, mainUserId, anotherUserId);
    }
    @DeleteMapping(value = "/message/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteMessage(@PathVariable("id") int id) {
        messageService.deleteMessage(id);
    }

    @GetMapping(value = "/text/message_id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Text getTextByMessageId(@PathVariable("id") int id) {
        return messageService.getTextByMessageId(id);
    }

    @GetMapping(value = "/text/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Text getTextById(@PathVariable("id") int id) {
        return messageService.getTextById(id);
    }
    @GetMapping(value = "/message/user/{mainUserId}/{anotherUserId}/messages")
    @ResponseStatus(HttpStatus.OK)
    public List<MessageInstance> getMessagesBetweenTimestamp(@PathVariable("mainUserId") int mainUserId,
                                       @PathVariable("anotherUserId") int anotherUserId,
                                       @RequestParam(name = "firstTimestamp") long firstTimestampI,
                                       @RequestParam(name = "lastTimestamp") long lastTimestampI) {
        Timestamp firstTimestamp = new Timestamp(firstTimestampI);
        Timestamp lastTimestamp = new Timestamp(lastTimestampI);
        return messageService.getMessagesBetweenTimestamp(firstTimestamp, lastTimestamp, mainUserId, anotherUserId);
    }
    @GetMapping(value = "/message/user/{mainUserId}/{anotherUserId}/messages/timestamp/before/{timestamp}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<MessageInstance> getMessagesBeforeTimestamp(@PathVariable("mainUserId") int mainUserId,
                                                             @PathVariable("anotherUserId") int anotherUserId,
                                                            @PathVariable("timestamp") long timestampI,
                                                            @RequestParam(name = "limit") int limit,
                                                            @RequestParam(name = "offset") int offset
                                                             ) {
      Timestamp timestamp = new Timestamp(timestampI);
      if(limit == 0)
          limit = 20;
      return messageService.getMessagesPackageBeforeTimestamp(timestamp, mainUserId, anotherUserId, limit, offset);
    }
    @PutMapping(value = "/message/user/{mainUserId}/{anotherUserId}/setChecked")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setChecked(@PathVariable("mainUserId") int mainUserId,
                           @PathVariable("anotherUserId") int anotherUserId) {
        messageService.setChecked(mainUserId, anotherUserId);
    }
    @GetMapping(value = "/user/{userId}/dialogs")
    @ResponseStatus(HttpStatus.OK)
    public Map<Integer, Integer> getDialogs(@PathVariable("userId") int userId) {
        return messageService.getAllDialogs(userId);
    }
}
