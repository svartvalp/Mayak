package com.kasyan313.Mayak.Services;
import com.kasyan313.Mayak.MessageInstance;
import com.kasyan313.Mayak.Models.Image;
import com.kasyan313.Mayak.Models.Message;
import com.kasyan313.Mayak.Models.Text;
import com.kasyan313.Mayak.Models.UserInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

@Repository
public class MessageService implements IMessageService {

    private SessionFactory sessionFactoryBean;

    @Autowired
    public MessageService(SessionFactory sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    private Session session() {
        return sessionFactoryBean.getCurrentSession();
    }

    @Transactional
    @Override
    public List<UserInfo> getAllDialogUserInfos(int userId) {
        Session session = session();
        Object[] userIds = session.createQuery("from Message where fromId = :userId or toId = :userId" +
                " order by messageTimestamp desc", Message.class)
                .setParameter("userId", userId)
                .stream()
                .mapToInt(message -> message.getFromId() == userId ? message.getToId() : message.getFromId())
                .distinct().boxed().toArray();
            if(userIds.length == 0) {
                return new LinkedList<>();
            }
            Query<UserInfo> userInfoQuery = session.createQuery("from UserInfo where userId in (:userIds)", UserInfo.class);
            userInfoQuery.setParameterList("userIds", userIds);
        return userInfoQuery.getResultList();
    }

    @Transactional
    @Override
    public MessageInstance getMessageInstanceById(int messageId) {
        Session session = session();
        Query<Message> query = session.createQuery("from Message where messageId = :messageId", Message.class);
        Message message = query.setParameter("messageId", messageId).getSingleResult();
            List<Image> images = getImagesByMessageId(messageId);
            Text text = getTextByMessageId(messageId);
            return new MessageInstance(message, text, images);
    }

    @Transactional
    @Override
    public Image getImageById(int imageId) {
        Session session = session();
        var query = session.createQuery("from Image where imageId = :imageId", Image.class);
        return query.setParameter("imageId", imageId).getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void deleteMessage(int messageId) {
        Session session = session();
        Message message = session
                .createQuery("from Message where messageId = :messageId", Message.class)
                .setParameter("messageId", messageId)
                .getSingleResult();
            List<Image> images = getImagesByMessageId(messageId);
            Text text = getTextByMessageId(messageId);
            session.delete(message);
            session.delete(text);
            for(Image image : images) {
                session.delete(image);
            }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public MessageInstance createMessage(MessageInstance instance) {
        Session session = session();
        Message message = instance.getMessageInfo();
        message.setMessageTimestamp(new Timestamp(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("+3")).toInstant().toEpochMilli()));
        session.save(message);
        int messageId = message.getMessageId();
        Text text = instance.getText();
        if(text != null) {
            text.setMessageId(messageId);
            session.save(text);
        }
        List<Image> images = instance.getImages();
        if(!images.isEmpty()) {
            for(Image image : images) {
                image.setMessageId(messageId);
                image.setSource(null);
                session.save(image);
            }
        }
        return instance;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public int uploadImage(int imageId, byte[] source) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Image> query =criteriaBuilder.createQuery(Image.class);
        Root<Image> root = query.from(Image.class);
            Image image = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("imageId"), imageId)))
                    .getSingleResult();
            image.setSource(source);
            session.merge(image);
            return image.getImageId();
    }

    @Transactional
    @Override
    public List<Image> getImagesByMessageId(int messageId) {
        Session session = session();
        var query = session.createQuery("from Image where messageId = :messageId", Image.class);
        return query.setParameter("messageId", messageId).getResultList();
    }

    @Transactional
    @Override
    public Text getTextByMessageId(int messageId) {
        Session session = session();
        var query = session.createQuery("from Text  where messageId = :messageId", Text.class);
        return query.setParameter("messageId", messageId).getSingleResult();
    }

    @Transactional
    @Override
    public Text getTextById(int textId) {
        Session session = session();
        return session.createQuery("from Text where textId = :textId", Text.class)
                .setParameter("textId", textId)
                .getSingleResult();
    }

    @Transactional
    @Override
    public List<MessageInstance> getMessagesAfterTimestamp(Timestamp timestamp, int mainUserId, int anotherUserId) {
        Session session = session();
        List<Message> messages = session
                .createQuery("from Message where messageTimestamp >= :timestamp and" +
                        " ((fromId = :mainUserId and toId = :anotherUserId)" +
                        " or (fromId = :anotherUserId and toId = :mainUserId)) order by messageTimestamp desc", Message.class)
                .setParameter("timestamp", timestamp)
                .setParameter("mainUserId", mainUserId)
                .setParameter("anotherUserId", anotherUserId)
                .getResultList();
            List<MessageInstance> instances = new LinkedList<>();
            for(Message message : messages) {
                instances.add(getMessageInstanceById(message.getMessageId()));
            }
            return instances;
    }

    @Transactional
    @Override
    public List<MessageInstance> getMessagesPackageBeforeTimestamp(Timestamp timestamp, int mainUserId,
                                                                   int anotherUserId, int limit, int offset) {
        Session session = session();

        List<Message> messages = session
                .createQuery("from Message where messageTimestamp <= :timestamp and" +
                " ((fromId = :mainUserId and toId = :anotherUserId)" +
                " or (fromId = :anotherUserId and toId = :mainUserId)) order by messageTimestamp desc", Message.class)
                .setParameter("timestamp", timestamp)
                .setParameter("mainUserId", mainUserId)
                .setParameter("anotherUserId", anotherUserId)
                .getResultList();
        List<MessageInstance> instances = new LinkedList<>();
        for (Message message : messages) {
            instances.add(getMessageInstanceById(message.getMessageId()));
        }
        return instances;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void setChecked(int fromUserId, int toUserId) {
        Session session = session();
        List<Message> messages = session.createQuery("from Message where (fromId = :fromId and toId = :toId) or" +
                " (fromId = :toId and toId = :fromId)", Message.class)
                .setParameter("fromId", fromUserId)
                .setParameter("toId", toUserId)
                .getResultList();
        for (Message message : messages) {
            message.setChecked(true);
            session.merge(message);
        }
    }

    @Transactional
    @Override
    public List<MessageInstance> getMessagesBetweenTimestamp(Timestamp firstTimestamp,
                                                             Timestamp lastTimestamp,
                                                             int mainUserId, int anotherUserId) {
        Session session = session();
        List<Message> messages = session
                .createQuery("from Message where (messageTimestamp <= :lastTimestamp and messageTimestamp >= :firstTimestamp) and" +
                "((fromId = :mainUserId and toId = :anotherUserId) or (fromId = :anotherUserId and toId = :mainUserId))", Message.class)
                .setParameter("lastTimestamp", lastTimestamp)
                .setParameter("firstTimestamp", firstTimestamp)
                .setParameter("mainUserId", mainUserId)
                .setParameter("anotherUserId", anotherUserId)
                .getResultList();
        List<MessageInstance> instances = new LinkedList<>();
        for (Message message : messages) {
            instances.add(getMessageInstanceById(message.getMessageId()));
        }
        return instances;
    }
}
