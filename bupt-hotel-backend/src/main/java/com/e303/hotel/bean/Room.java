package com.e303.hotel.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("room")
public class Room {
    @TableId(value = "room_id", type = IdType.AUTO) // 👈 这行很关键！
    private Integer roomId;
    private String password;
    private Integer roomStatus; // 0: 空闲, 1: 不空闲
    private Integer status;     // 0: 关闭, 1: 开启
    private float currentTemp;
    private String currentSpeed; // HIGH, MEDIUM, LOW, OFF
    private float targetTemp;
    private String targetSpeed;  // HIGH, MEDIUM, LOW, OFF
    private float electricalUsage;
    private float fee;


}
