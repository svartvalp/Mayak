package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Models.ProfileImage;
import com.kasyan313.Mayak.Models.User;
import com.kasyan313.Mayak.Exceptions.UserNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Queue;

@Repository
public class UserService implements IUserService {
    @Autowired
    SessionFactory sessionFactoryBean;

    private Session session() {
        return sessionFactoryBean.getCurrentSession();
    }
    @Override
    public int createUser(String email, String password) {
        if(checkIfExists(email)) {
            throw new UserAlreadyExistsException();
        }
        Session session = session();
        session.beginTransaction();
        User newUser = new User(email, password);
        session.save(newUser);
        session.getTransaction().commit();
        return newUser.getUserId();
    }

    @Override
    public User findUserById(int id) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query =criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        try {
            User user =  session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("userId"), id))).getSingleResult();
            session.getTransaction().commit();
            return user;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new UserNotFoundException();
        }
    }

    @Override
    public int getId(String email, String password) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query =criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        try {
            User user =  session.createQuery(query.select(root)
                    .where(criteriaBuilder.equal(root.get("email"), email), criteriaBuilder.equal(root.get("password"), password)))
                    .getSingleResult();
            session.getTransaction().commit();
            return user.getUserId();
        }catch (NoResultException ex) {
            session.getTransaction().rollback();
            throw new UserNotFoundException();
        }
    }

    @Override
    public boolean checkIfExists(String email) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query =criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        List<User> users = session.createQuery(query.select(root)
                .where(criteriaBuilder.equal(root.get("email"), email))).list();
        session.getTransaction().commit();
        if(!users.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean updateEmail(String oldEmail, String newEmail) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query =criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        try {
            User user = session.createQuery(query.select(root)
                    .where(criteriaBuilder.equal(root.get("email"), oldEmail))).getSingleResult();
            user.setEmail(newEmail);
            session.merge(user);
            session.getTransaction().commit();
            return true;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new UserNotFoundException();
        }
    }

    @Override
    public boolean updatePassword(String email, String oldPassword, String newPassword) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query =criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        try {
            User user = session.createQuery(query.select(root)
                    .where(criteriaBuilder.equal(root.get("email"), email), criteriaBuilder.equal(root.get("password"), oldPassword))).getSingleResult();
            user.setPassword(newPassword);
            session.merge(user);
            session.getTransaction().commit();
            return true;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new UserNotFoundException();
        }
    }

    @Override
    public boolean deleteUser(int id) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query =criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        try {
            User user = session.createQuery(query.select(root)
                    .where(criteriaBuilder.equal(root.get("userId"), id))).getSingleResult();
            session.remove(user);
            session.getTransaction().commit();
            return true;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new UserNotFoundException();
        }
    }

    @Override
    public String getPassword(String email) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        try {
            User user = session.createQuery(query.select(root)
                    .where(builder.equal(root.get("email"), email))).getSingleResult();
            session.getTransaction().commit();
            return user.getPassword();
        }catch (NoResultException exc){
            session.getTransaction().rollback();
            throw new UserNotFoundException();
        }
    }

    @Override
    public void uploadProfileImage(byte[] source, int userId) {
        Session session = session();
        session.beginTransaction();
        ProfileImage profileImage = new ProfileImage(userId, source);
        try {
            session.saveOrUpdate(profileImage);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
        }
    }

    @Override
    public byte[] getProfileImage(int userId) {
        Session session = session();
        session.beginTransaction();
        try {
            Query<ProfileImage> query = session.createQuery("FROM ProfileImage where userId = :userId",ProfileImage.class);
            query.setParameter("userId", userId);
            ProfileImage image = query.getSingleResult();
            return  image.getSource();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw  new UserNotFoundException();
        }
    }
}
