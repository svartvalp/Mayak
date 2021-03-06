package com.kasyan313.Mayak.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Calendar;

@Entity
@Table(name = "user_info")
public class UserInfo {
    @Column(name = "user_id")
    @Id
    @JsonProperty("user_id")
    @NotNull
    private int userId;
    @Column(name = "first_name")
    @JsonProperty("first_name")
    @NotNull
    private String firstName;
    @Column(name = "last_name")
    @JsonProperty("last_name")
    @NotNull
    private String lastName;
    @Column(name = "nickname", unique = true)
    @JsonProperty("nickname")
    @NotNull
    private String nickName;
    @Column(name = "date_of_birth")
    @JsonProperty("date_of_birth")
    private Calendar dateOfBirth;
    @Column(name = "phone_number")
    @JsonProperty("phone_number")
    @Size(max = 12)
    private String phoneNumber;
    @Column(name = "creation_timestamp")
    @JsonProperty("creation_timestamp")
    @NotNull
    private Timestamp creationTimestamp;

    public UserInfo() {
    }

    public UserInfo(int userId, String firstName, String lastName, String nickName, Calendar dateOfBirth, String phoneNumber, Timestamp creationTimestamp) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.creationTimestamp = creationTimestamp;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Calendar getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Calendar dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Timestamp getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Timestamp creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
