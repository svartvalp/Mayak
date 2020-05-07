package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Models.ProfileImage;
import com.kasyan313.Mayak.Models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class UserService implements IUserService {

    private SessionFactory sessionFactoryBean;

    @Autowired
    public UserService(SessionFactory sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    private Session session() {
        return sessionFactoryBean.getCurrentSession();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public int createUser(String email, String password) {
        if (checkIfExists(email)) {
            throw new UserAlreadyExistsException();
        }
        Session session = session();
        User newUser = new User(email, password);
        session.save(newUser);
        return newUser.getUserId();
    }

    @Transactional
    @Override
    public User findUserById(int id) {
        Session session = session();
        Query<User> query = session.createQuery("from User where userId = :userId", User.class);
        query.setParameter("userId", id);
        return query.getSingleResult();

    }

    @Transactional
    @Override
    public int getId(String email, String password) {
        Session session = session();
        Query<User> query = session.createQuery("from User where email = :email and " +
                "password = :password", User.class);
        query.setParameter("email", email);
        query.setParameter("password", password);
        User user = query.getSingleResult();
        session.flush();
        return user.getUserId();
    }

    @Transactional
    @Override
    public boolean checkIfExists(String email) {
        Session session = session();
        Query<User> query = session.createQuery("from User where email = :email", User.class);
        query.setParameter("email", email);
        List<User> users = query.list();
        return !users.isEmpty();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updateEmail(String oldEmail, String newEmail) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        User user = session.createQuery(query.select(root)
                .where(criteriaBuilder.equal(root.get("email"), oldEmail))).getSingleResult();
        user.setEmail(newEmail);
        session.merge(user);
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updatePassword(String email, String oldPassword, String newPassword) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        User user = session.createQuery(query.select(root)
                .where(criteriaBuilder.equal(root.get("email"), email), criteriaBuilder.equal(root.get("password"), oldPassword))).getSingleResult();
        user.setPassword(newPassword);
        session.merge(user);
        return true;
    }

    @Transactional
    @Override
    public boolean deleteUser(int id) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        User user = session.createQuery(query.select(root)
                .where(criteriaBuilder.equal(root.get("userId"), id))).getSingleResult();
        session.remove(user);
        return true;
    }

    @Transactional
    @Override
    public String getPassword(String email) {
        Session session = session();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        User user = session.createQuery(query.select(root)
                .where(builder.equal(root.get("email"), email))).getSingleResult();
        return user.getPassword();
    }

    @Transactional
    @Override
    public void uploadProfileImage(byte[] source, int userId) {
        Session session = session();
        ProfileImage profileImage = new ProfileImage(userId, source);
        session.saveOrUpdate(profileImage);
    }

    @Transactional
    @Override
    public byte[] getProfileImage(int userId) {
        Session session = session();
        Query<ProfileImage> query = session.createQuery("FROM ProfileImage where userId = :userId", ProfileImage.class);
        query.setParameter("userId", userId);
        ProfileImage image = query.getSingleResult();
        return image.getSource();
    }
}
