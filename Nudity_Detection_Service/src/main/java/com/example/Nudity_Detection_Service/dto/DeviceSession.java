package com.example.Nudity_Detection_Service.dto;

public class DeviceSession {

    private String deviceId;
    private String authToken;

    public DeviceSession() {}

    public DeviceSession(String deviceId, String authToken) {
        this.deviceId = deviceId;
        this.authToken = authToken;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
}
