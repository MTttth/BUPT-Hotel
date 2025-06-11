package com.e303.hotel.service.scheduler;

import com.e303.hotel.bean.enums.Speed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomACRequest {
    private int roomId;
    private Speed targetSpeed;
    private int serviceTime = 0; // 正在被服务的时间（秒）
    //private long requestTime; // 请求时间戳
    private int waitTime; // 等待时间倒计时
    private float totalDegree = 0.0f;
    private int priority = 0;//优先级，用于相同speed时对比

    public RoomACRequest(int roomId, Speed targetSpeed) {
        this.roomId = roomId;
        this.targetSpeed = targetSpeed;
    }
}
