package com.e303.hotel.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.e303.hotel.bean.enums.Speed;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("room")
public class Room {
    @TableId(value = "room_id", type = IdType.AUTO) // 👈 这行很关键！
    //@JsonProperty("room_id")
    private Integer roomId;
    private String password;
    private Integer roomStatus; // 0: 空闲, 1: 不空闲
    private Integer status;     // 0: 关闭, 1: 开启
    private float currentTemp;
    private Speed currentSpeed; //"high" "mid" "slow" "stop"
    private float targetTemp;
    private Speed targetSpeed;  // "high" "mid" "slow" "stop"
    private float electricalUsage;
    private float fee;
    //private float initTemp = 25.0f;

    public float getInitialTemp(){
        return 25.0f;
    }


}
