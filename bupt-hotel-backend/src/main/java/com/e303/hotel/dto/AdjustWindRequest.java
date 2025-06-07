package com.e303.hotel.dto;

import lombok.Data;

@Data
public class AdjustWindRequest {
    private int roomId;
    private String targetSpeed;
}
