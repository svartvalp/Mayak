package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Exceptions.UserNotFoundException;
import com.kasyan313.Mayak.Models.UserInfo;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.NoResultException;

@Repository
public class UserInfoService implements IUserInfoService {

    SessionFactory sessionFactoryBean;

    @Autowired
    public UserInfoService(SessionFactory sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    private Session session() {
        Session session = sessionFactoryBean.getCurrentSession();
        return session;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void createUserInfo(UserInfo userInfo) {
        Session session = session();
            session.save(userInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserInfo getInfoByUserId(int userId) {
        Session session = session();
        Query<UserInfo> query = session.createQuery("from UserInfo  where userId = :userId", UserInfo.class);
        query.setParameter("userId", userId);
            UserInfo userInfo =  query.getSingleResult();
            return userInfo;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateUserInfo(UserInfo userInfo) {
        Session session = session();
            session.merge(userInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserInfo findUserInfoByNickName(String nickname) {
        Session session = session();
            UserInfo userInfo = session.createQuery("from UserInfo where nickName = :nickname", UserInfo.class)
                    .setParameter("nickname", nickname)
                    .getSingleResult();
            return userInfo;
    }
}
