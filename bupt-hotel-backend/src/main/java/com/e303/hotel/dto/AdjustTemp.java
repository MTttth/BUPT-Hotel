package com.e303.hotel.dto;

import lombok.Data;

@Data
public class AdjustTemp {
    private Integer roomId;
    private float targetTemp;
}
