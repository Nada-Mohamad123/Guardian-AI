package com.example.child_safety_service.dto;

public class DetectionDto {

    private int class_id;

    private String class_name;

    private double confidence;

    private DetectionBox box;

    public DetectionDto() {
    }

    public int getClass_id() {
        return class_id;
    }

    public void setClass_id(int class_id) {
        this.class_id = class_id;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public DetectionBox getBox() {
        return box;
    }

    public void setBox(DetectionBox box) {
        this.box = box;
    }
}