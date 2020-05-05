package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Exceptions.UserNotFoundException;
import com.kasyan313.Mayak.Models.User;
import com.kasyan313.Mayak.Models.UserInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Repository
public class UserInfoService implements IUserInfoService {
    @Autowired
    SessionFactory sessionFactoryBean;

    private Session session() {
        return sessionFactoryBean.openSession();
    }
    @Override
    public void createUserInfo(UserInfo userInfo) {
        Session session = session();
        session.beginTransaction();
        try {
            session.save(userInfo);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new UserAlreadyExistsException("nickname is already used");
        }
        finally {
            session.close();
        }
    }

    @Override
    public UserInfo getInfoByUserId(int userId) {
        Session session = session();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<UserInfo> query =criteriaBuilder.createQuery(UserInfo.class);
        Root<UserInfo> root = query.from(UserInfo.class);
        try {
            UserInfo userInfo =  session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("userId"), userId))).getSingleResult();
            session.getTransaction().commit();
            return userInfo;
        }catch (NoResultException exc) {
            session.getTransaction().rollback();
            throw new ResourceNotFoundException();
        }
        finally {
            session.close();
        }
    }

    @Override
    public void updateUserInfo(UserInfo userInfo) {
        Session session = session();
        try {
            session.beginTransaction();
            session.merge(userInfo);
            session.getTransaction().commit();
        }catch (Exception e) {
            session.getTransaction().rollback();
            throw new UserAlreadyExistsException("nickname is already used");
        }
        finally {
            session.close();
        }
    }

    @Override
    public UserInfo findUserInfoByNickName(String nickname) {
        Session session = session();
        session.beginTransaction();
        try {
            UserInfo userInfo = session.createQuery("from UserInfo where nickName = :nickname", UserInfo.class)
                    .setParameter("nickname", nickname)
                    .getSingleResult();
            session.getTransaction().commit();
            return userInfo;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new UserNotFoundException();
        }
        finally {
            session.close();
        }
    }
}
