package com.e303.hotel.dto;

import lombok.Data;

@Data
public class AdjustTempRequest {
    private Integer roomId;
    private float targetTemp;
}
