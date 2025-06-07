package com.e303.hotel.dto;

import lombok.Data;

@Data

public class PowerOnRequest {
    private Integer roomId;
    private float targetTemp;
    private String targetSpeed;
}
