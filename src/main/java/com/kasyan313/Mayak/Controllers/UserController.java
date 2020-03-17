package com.kasyan313.Mayak.Controllers;


import com.kasyan313.Mayak.Models.User;
import com.kasyan313.Mayak.Services.IUserService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    SessionFactory sessionFactoryBean;

    @Autowired
    IUserService userService;

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String home() {
        return "Hello, world!";
    }

    @PostMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> createUser(@RequestBody User user)
    {
        Map<String, String> responseBody = new LinkedHashMap<>();
        responseBody.put("user_id", Integer.toString(userService.createUser(user.getEmail(), user.getPassword())));
        return responseBody;
    }

    @GetMapping(value = "/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User findUserById(@PathVariable("id") int id) {
        return userService.findUserById(id);
    }

    @DeleteMapping(value = "/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable("id") int id) {
        userService.deleteUser(id);
    }

    @PutMapping(value = "/user/email")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserEmail(@RequestParam("oldEmail") String oldEmail, @RequestParam("newEmail") String newEmail) {
        userService.updateEmail(oldEmail, newEmail);
    }

    @PutMapping(value = "/user/email/{email}/password")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserPassword(@PathVariable(name = "email") String email, @RequestParam("oldPassword") String oldPassword,
                                   @RequestParam("newPassword") String newPassword) {
        userService.updatePassword(email, oldPassword, newPassword);
    }

    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getUserId(@RequestParam String email, @RequestParam String password) {
        Map<String, String> responseBody = new LinkedHashMap<>();
        responseBody.put("user_id", Integer.toString(userService.getId(email, password)));
        return responseBody;
    }
    @PutMapping(value = "auth/forgot")
    public void sendUserPasswordInMail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = userService.getPassword(email);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setFrom("kasyan313@gmail.com");
        mailMessage.setSubject("Your password");
        mailMessage.setText("Yout password: " + password);
        javaMailSender.send(mailMessage);
    }
}
