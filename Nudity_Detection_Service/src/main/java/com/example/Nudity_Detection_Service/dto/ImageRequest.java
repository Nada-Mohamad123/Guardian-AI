package com.example.Nudity_Detection_Service.dto;

public class ImageRequest {

    private String imageBase64;
    private String userId;

    //  Getter
    public String getImageBase64() {
        return imageBase64;
    }

    //  Setter
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
