package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.MessageInstance;
import com.kasyan313.Mayak.Models.Image;
import com.kasyan313.Mayak.Models.Text;
import com.kasyan313.Mayak.Models.UserInfo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface IMessageService {
    public List<UserInfo> getAllDialogUserInfos(int userId);
    public MessageInstance getMessageInstanceById(int messageId);
    public Image getImageById(int imageId);
    public void deleteMessage(int messageId);
    public MessageInstance createMessage(MessageInstance instance);
    public int uploadImage(int imageId, byte[] source);
    public List<Image> getImagesByMessageId(int messageId);
    public Text getTextByMessageId(int messageId);
    public Text getTextById(int textId);
    public List<MessageInstance> getMessagesAfterTimestamp(Timestamp timestamp, int mainUserId, int anotherUserId);
    public List<MessageInstance> getMessagesPackageBeforeTimestamp(Timestamp timestamp, int mainUserId, int anotherUserId, int limit, int offset);
    public void setChecked(int fromUserId, int byUserId);
    public List<MessageInstance> getMessagesBetweenTimestamp(Timestamp firstTimestamp, Timestamp lastTimestamp,int mainUserId, int anotherUserId);
}
