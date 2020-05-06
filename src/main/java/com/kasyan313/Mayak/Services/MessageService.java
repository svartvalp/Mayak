package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
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
import javax.persistence.NoResultException;
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

    SessionFactory sessionFactoryBean;

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
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
            Predicate fromUserId = criteriaBuilder.equal(root.get("from"), userId);
            Predicate toUserId = criteriaBuilder.equal(root.get("to"), userId);
            Object[] userIds = session.createQuery(query.select(root)
                    .where(criteriaBuilder.or(fromUserId, toUserId))
                    .orderBy(criteriaBuilder.desc(root.get("timestamp"))))
                    .stream()
                    .mapToInt(message -> message.getFrom() == userId ? message.getTo() : message.getFrom()).distinct().boxed().toArray();
            if(userIds.length == 0) {
                return new LinkedList<>();
            }
            Query<UserInfo> userInfoQuery = session.createQuery("from UserInfo where userId in (:userIds)");
            userInfoQuery.setParameterList("userIds", userIds);
            List<UserInfo> userInfoList = userInfoQuery.getResultList();
           return userInfoList;
    }

    @Transactional
    @Override
    public MessageInstance getMessageInstanceById(int messageId) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query = criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Message message = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                    .getSingleResult();
            List<Image> images = getImagesByMessageId(messageId);
            Text text = getTextByMessageId(messageId);
            session.flush();
            return new MessageInstance(message, text, images);
        } catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional
    @Override
    public Image getImageById(int imageId) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Image> query =criteriaBuilder.createQuery(Image.class);
        Root<Image> root = query.from(Image.class);
        try {
            Image image = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("imageId"), imageId)))
                    .getSingleResult();
            session.flush();
            return image;
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void deleteMessage(int messageId) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Message> query =criteriaBuilder.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        try {
            Message message = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                    .getSingleResult();
            List<Image> images = getImagesByMessageId(messageId);
            Text text = getTextByMessageId(messageId);
            session.delete(message);
            session.delete(text);
            for(Image image : images) {
                session.delete(image);
            }
            session.flush();
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public MessageInstance createMessage(MessageInstance instance) {
        Session session = session();
        Message message = instance.getMessageInfo();
        message.setTimestamp(new Timestamp(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("+3")).toInstant().toEpochMilli()));
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
        try {
            Image image = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("imageId"), imageId)))
                    .getSingleResult();
            image.setSource(source);
            session.merge(image);
            session.flush();
            return image.getImageId();
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional
    @Override
    public List<Image> getImagesByMessageId(int messageId) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Image> query =criteriaBuilder.createQuery(Image.class);
        Root<Image> root = query.from(Image.class);
        List<Image> images = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                .getResultList();
        return images;
    }

    @Transactional
    @Override
    public Text getTextByMessageId(int messageId) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Text> query =criteriaBuilder.createQuery(Text.class);
        Root<Text> root = query.from(Text.class);
        try {
            Text text = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("messageId"), messageId)))
                    .getSingleResult();
            session.flush();
            return text;
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional
    @Override
    public Text getTextById(int textId) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Text> query =criteriaBuilder.createQuery(Text.class);
        Root<Text> root = query.from(Text.class);
        try {
            Text text = session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("textId"), textId)))
                    .getSingleResult();
            session.flush();
            return text;
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }
    @Transactional
    @Override
    public List<MessageInstance> getMessagesAfterTimestamp(Timestamp timestamp, int mainUserId, int anotherUserId) {
        Session session = session();
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
            for(Message message : messages) {
                instances.add(getMessageInstanceById(message.getMessageId()));
            }
            session.flush();
            return instances;
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional
    @Override
    public List<MessageInstance> getMessagesPackageBeforeTimestamp(Timestamp timestamp, int mainUserId,
                                                                   int anotherUserId, int limit, int offset) {
        Session session = session();
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
            for(Message message : messages) {
                instances.add(getMessageInstanceById(message.getMessageId()));
            }
            session.flush();
            return instances;
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void setChecked(int fromUserId, int toUserId) {
        Session session = session();
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
            session.flush();
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional
    @Override
    public List<MessageInstance> getMessagesBetweenTimestamp(Timestamp firstTimestamp,
                                                             Timestamp lastTimestamp,
                                                             int mainUserId, int anotherUserId) {
        Session session = session();
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
            for(Message message : messages) {
                instances.add(getMessageInstanceById(message.getMessageId()));
            }
            session.flush();
            return instances;
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }
}
