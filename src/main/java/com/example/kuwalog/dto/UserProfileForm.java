package com.example.kuwalog.dto;

import jakarta.validation.constraints.Size;

public class UserProfileForm {

    @Size(max = 500)
    private String bio;

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
