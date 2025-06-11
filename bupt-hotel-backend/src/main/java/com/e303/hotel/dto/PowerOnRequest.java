package com.e303.hotel.dto;

import com.e303.hotel.bean.enums.Speed;
import lombok.Data;

@Data

public class PowerOnRequest {
    private String clientId;
    private Integer roomId;
    private float targetTemp;
    private Speed targetSpeed;
}
