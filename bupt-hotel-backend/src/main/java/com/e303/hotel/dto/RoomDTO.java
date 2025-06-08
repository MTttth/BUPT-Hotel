package com.e303.hotel.dto;

import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {
    private Integer roomId;
    private Integer roomStatus; // 0: 空闲, 1: 不空闲
    private Integer status;     // 0: 关闭, 1: 开启
    private float currentTemp;
    private Speed currentSpeed; //"high" "mid" "slow" "stop"
    private float targetTemp;
    private Speed targetSpeed;  // "high" "mid" "slow" "stop"
    private float electricalUsage;
    private float fee;
    public RoomDTO(Room room) {
        this.roomId = room.getRoomId();
        this.roomStatus = room.getRoomStatus();
        this.status = room.getStatus();
        this.currentTemp = room.getCurrentTemp();
        this.currentSpeed = room.getCurrentSpeed();
        this.targetTemp = room.getTargetTemp();
        this.targetSpeed = room.getTargetSpeed();
        this.electricalUsage = room.getElectricalUsage();
        this.fee = room.getFee();

    }
}
