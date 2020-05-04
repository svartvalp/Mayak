package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
import com.kasyan313.Mayak.MessageInstance;
import com.kasyan313.Mayak.Models.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.jboss.jandex.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

@Repository
public class MessageService implements IMessageService {
    @Autowired
    SessionFactory sessionFactoryBean;

    private Session session() {
        return sessionFactoryBean.getCurrentSession();
    }

    @Override
    public List<UserInfo> getAllDialogUserInfos(int userId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Predicate fromUserId = criteriaBuilder.equal(root.get("from"), userId);
            Predicate toUserId = criteriaBuilder.equal(root.get("to"), userId);
            Object[] userIds = session.createQuery(query.select(root)
                    .where(criteriaBuilder.or(fromUserId, toUserId))
                    .orderBy(criteriaBuilder.desc(root.get("timestamp"))))
                    .stream()
                    .mapToInt(message -> {
                        return message.getFrom() == userId ? message.getTo() : message.getFrom();
                    }).distinct().boxed().toArray();
            Query<UserInfo> userInfoQuery = session.createQuery("from UserInfo where userId in (:userIds)");
            userInfoQuery.setParameterList("userIds", userIds);
            List<UserInfo> userInfoList = userInfoQuery.getResultList();
            session.getTransaction().commit();
           return userInfoList;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public MessageInstance getMessageInstanceById(int messageId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Message message = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                    .getSingleResult();
            session.getTransaction().commit();
            List<Image> images = getImagesByMessageId(messageId);
            Text text = getTextByMessageId(messageId);
            return  new MessageInstance(message, text, images);
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public Image getImageById(int imageId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Image> query =criteriaBuilder.createQuery(Image.class);
        Root<Image> root = query.from(Image.class);
        try {
            Image image = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("imageId"), imageId)))
                    .getSingleResult();
            session.getTransaction().commit();
            return image;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public void deleteMessage(int messageId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Message message = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                    .getSingleResult();
            session.getTransaction().commit();
            List<Image> images = getImagesByMessageId(messageId);
            Text text = getTextByMessageId(messageId);
            session.delete(message);
            session.delete(text);
            for(Image image : images) {
                session.delete(image);
            }
            session.getTransaction().commit();
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public MessageInstance createMessage(MessageInstance instance) {
        Session session = session();
        session.beginTransaction();
        Message message = instance.getMessageInfo();
        message.setTimestamp(new Timestamp(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC")).toInstant().toEpochMilli()));
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
        session.getTransaction().commit();
        return instance;
    }

    @Override
    public int uploadImage(int imageId, byte[] source) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Image> query =criteriaBuilder.createQuery(Image.class);
        Root<Image> root = query.from(Image.class);
        try {
            Image image = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("imageId"), imageId)))
                    .getSingleResult();
            image.setSource(source);
            session.merge(image);
            session.getTransaction().commit();
            return image.getImageId();
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public List<Image> getImagesByMessageId(int messageId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Image> query =criteriaBuilder.createQuery(Image.class);
        Root<Image> root = query.from(Image.class);
        List<Image> images = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                .getResultList();
        session.getTransaction().commit();
        return images;
    }

    @Override
    public Text getTextByMessageId(int messageId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Text> query =criteriaBuilder.createQuery(Text.class);
        Root<Text> root = query.from(Text.class);
        try {
            Text text = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                    .getSingleResult();
            session.getTransaction().commit();
            return text;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public Text getTextById(int textId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Text> query =criteriaBuilder.createQuery(Text.class);
        Root<Text> root = query.from(Text.class);
        try {
            Text text = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("textId"), textId)))
                    .getSingleResult();
            session.getTransaction().commit();
            return text;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public List<MessageInstance> getMessagesAfterTimestamp(Timestamp timestamp, int mainUserId, int anotherUserId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Predicate fromMainId = criteriaBuilder.equal(root.get("from"), mainUserId);
            Predicate toAnotherId = criteriaBuilder.equal(root.get("to"), anotherUserId);
            Predicate toMainId = criteriaBuilder.equal(root.get("to"), mainUserId);
            Predicate fromAnotherId = criteriaBuilder.equal(root.get("from"), anotherUserId);
            Predicate fromMainToAnother = criteriaBuilder.and(fromMainId,toAnotherId);
            Predicate fromAnotherToMain = criteriaBuilder.and(fromAnotherId, toMainId);
            Predicate afterTimestamp = criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), timestamp);
            Predicate orPredicate = criteriaBuilder.or(fromAnotherToMain, fromMainToAnother);
            Predicate fullPredicate = criteriaBuilder.and(afterTimestamp, orPredicate);
            List<Message> messages = session.createQuery(query.select(root)
                    .where(fullPredicate).orderBy(criteriaBuilder.desc(root.get("timestamp")))).list();
            List<MessageInstance> instances = new LinkedList<>();
            session.getTransaction().commit();
            for(Message message : messages) {
                instances.add(getMessageInstanceById(message.getMessageId()));
            }
            return instances;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public List<MessageInstance> getMessagesPackageBeforeTimestamp(Timestamp timestamp, int mainUserId,
                                                                   int anotherUserId, int limit, int offset) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Predicate fromMainId = criteriaBuilder.equal(root.get("from"), mainUserId);
            Predicate toAnotherId = criteriaBuilder.equal(root.get("to"), anotherUserId);
            Predicate toMainId = criteriaBuilder.equal(root.get("to"), mainUserId);
            Predicate fromAnotherId = criteriaBuilder.equal(root.get("from"), anotherUserId);
            Predicate fromMainToAnother = criteriaBuilder.and(fromMainId,toAnotherId);
            Predicate fromAnotherToMain = criteriaBuilder.and(fromAnotherId, toMainId);
            Predicate afterTimestamp = criteriaBuilder.lessThan(root.get("timestamp"), timestamp);
            Predicate orPredicate = criteriaBuilder.or(fromAnotherToMain, fromMainToAnother);
            Predicate fullPredicate = criteriaBuilder.and(afterTimestamp, orPredicate);
            List<Message> messages = session.createQuery(query.select(root)
                    .where(fullPredicate).orderBy(criteriaBuilder.desc(root.get("timestamp")))).setMaxResults(limit).setFirstResult(offset).list();
            List<MessageInstance> instances = new LinkedList<>();
            session.getTransaction().commit();
            for(Message message : messages) {
                instances.add(getMessageInstanceById(message.getMessageId()));
            }
            return instances;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }


    @Override
    public void setChecked(int fromUserId, int toUserId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            List<Message> messages = session.createQuery(query.select(root)
                    .where(criteriaBuilder.equal(root.get("from"), fromUserId),
                            criteriaBuilder.equal(root.get("to"), toUserId)))
                    .list();
            for(Message message : messages) {
                message.setChecked(true);
                session.merge(message);
            }
            session.getTransaction().commit();
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public List<MessageInstance> getMessagesBetweenTimestamp(Timestamp firstTimestamp,
                                                             Timestamp lastTimestamp,
                                                             int mainUserId, int anotherUserId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Predicate fromMainId = criteriaBuilder.equal(root.get("from"), mainUserId);
            Predicate toAnotherId = criteriaBuilder.equal(root.get("to"), anotherUserId);
            Predicate toMainId = criteriaBuilder.equal(root.get("to"), mainUserId);
            Predicate fromAnotherId = criteriaBuilder.equal(root.get("from"), anotherUserId);
            Predicate fromMainToAnother = criteriaBuilder.and(fromMainId,toAnotherId);
            Predicate fromAnotherToMain = criteriaBuilder.and(fromAnotherId, toMainId);
            Predicate afterTimestamp = criteriaBuilder.lessThan(root.get("timestamp"), lastTimestamp);
            Predicate beforeTimestamp = criteriaBuilder.greaterThan(root.get("timestamp"), firstTimestamp);
            Predicate betweenTimestamps = criteriaBuilder.and(afterTimestamp, beforeTimestamp);
            Predicate orPredicate = criteriaBuilder.or(fromAnotherToMain, fromMainToAnother);
            Predicate fullPredicate = criteriaBuilder.and(betweenTimestamps, orPredicate);
            List<Message> messages = session.createQuery(query.select(root)
                    .where(fullPredicate).orderBy(criteriaBuilder.desc(root.get("timestamp")))).list();
            List<MessageInstance> instances = new LinkedList<>();
            session.getTransaction().commit();
            for(Message message : messages) {
                instances.add(getMessageInstanceById(message.getMessageId()));
            }
            return instances;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
    }
}
