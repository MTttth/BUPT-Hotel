package com.e303.hotel.dto;

import com.e303.hotel.bean.enums.Speed;
import lombok.Data;

@Data
public class AdjustWindRequest {
    private int roomId;
    private Speed targetSpeed;
}
