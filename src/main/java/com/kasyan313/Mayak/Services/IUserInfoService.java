package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Models.UserInfo;

import java.util.Calendar;

public interface IUserInfoService {
    public void createUserInfo(UserInfo userInfo);
    public UserInfo getInfoByUserId(int userId);
    public void updateUserInfo(UserInfo userInfo);
}
