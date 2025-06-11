package com.e303.hotel.dto;

import lombok.Data;

@Data
public class CheckInRequest {
    private String clientId;
    private Integer roomId;
}
