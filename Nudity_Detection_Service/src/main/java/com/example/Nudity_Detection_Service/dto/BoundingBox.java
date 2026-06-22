package com.example.Nudity_Detection_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBox {
    private int x;       // left
    private int y;       // top
    private int width;
    private int height;
}
