package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Exceptions.UserNotFoundException;
import com.kasyan313.Mayak.Models.UserInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Repository
public class UserInfoService implements IUserInfoService {

    SessionFactory sessionFactoryBean;

    @Autowired
    public UserInfoService(SessionFactory sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    private Session session() {
        return sessionFactoryBean.getCurrentSession();
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void createUserInfo(UserInfo userInfo) {
        Session session = session();
        try {
            session.save(userInfo);
            session.flush();
        } catch (Exception e) {
            throw new UserAlreadyExistsException("nickname is already used");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserInfo getInfoByUserId(int userId) {
        Session session = session();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<UserInfo> query =criteriaBuilder.createQuery(UserInfo.class);
        Root<UserInfo> root = query.from(UserInfo.class);
        try {
            UserInfo userInfo =  session.createQuery(query.select(root).where(criteriaBuilder.equal(root.get("userId"), userId))).getSingleResult();
            session.flush();
            return userInfo;
        }catch (NoResultException exc) {
            throw new ResourceNotFoundException();
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateUserInfo(UserInfo userInfo) {
        Session session = session();
        try {
            session.merge(userInfo);
            session.flush();
        }catch (Exception e) {
            throw new UserAlreadyExistsException("nickname is already used");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserInfo findUserInfoByNickName(String nickname) {
        Session session = session();
        try {
            UserInfo userInfo = session.createQuery("from UserInfo where nickName = :nickname", UserInfo.class)
                    .setParameter("nickname", nickname)
                    .getSingleResult();
            return userInfo;
        } catch (Exception e) {
            throw new UserNotFoundException();
        }
    }
}
