package com.kasyan313.Mayak.Controllers;

import com.kasyan313.Mayak.Models.UserInfo;
import com.kasyan313.Mayak.Services.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserInfoController {

    @Autowired
    IUserInfoService userInfoService;

    @GetMapping(value = "/user/info/{id}")
    public UserInfo getUserInfoById(@PathVariable("id") int userId) {
        return userInfoService.getInfoByUserId(userId);
    }

    @PostMapping(value = "/user/info", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createUserInfo(@RequestBody UserInfo userInfo) {
        userInfoService.createUserInfo(userInfo);
    }

    @PutMapping(value = "/user/info/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateUserInfo(@RequestBody UserInfo userInfo, @PathVariable String id) {
        userInfoService.updateUserInfo(userInfo);
    }

    @GetMapping(value = "/user/info/nickname/{nickname}")
    public UserInfo getUserInfoByNickName(@PathVariable("nickname") String nickname) {
        return userInfoService.findUserInfoByNickName(nickname);
    }
}
