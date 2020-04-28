package com.kasyan313.Mayak.Services;

import com.kasyan313.Mayak.Models.User;

public interface IUserService {
    //returns a userId
    public int createUser(String email, String password);
    //returns a user or null
    public User findUserById(int id);
    //returns userId
    public int getId(String email, String password);
    public boolean checkIfExists(String email);
    public boolean updateEmail(String oldEmail, String newEmail);
    public boolean updatePassword(String email, String oldPassword, String newPassword);
    public boolean deleteUser(int id);
    public String getPassword(String email);
    public void uploadProfileImage(byte[] source, int userId);
    public byte[] getProfileImage(int userId);
 }
