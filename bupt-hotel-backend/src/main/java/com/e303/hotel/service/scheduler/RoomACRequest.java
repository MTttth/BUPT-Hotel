package com.e303.hotel.service.scheduler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomACRequest {
    private int roomId;
    private String targetSpeed;
    private int serviceTime; // 正在被服务的时间（秒）
    private long requestTime; // 请求时间戳
    private int waitTime; // 等待时间倒计时
}
