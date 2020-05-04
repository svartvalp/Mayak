package com.kasyan313.Mayak.Models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "profile_image")
public class ProfileImage {
    @Id
    @Column(name = "user_id")
    @NotNull
    private int userId;
    @Column(name = "source")
    private byte[] source;

    public ProfileImage() {
    }

    public ProfileImage(int userId, byte[] source) {
        this.userId = userId;
        this.source = source;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }
}
