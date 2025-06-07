package com.e303.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.dto.AdjustTemp;
import com.e303.hotel.dto.ClientLoginRequest;
import com.e303.hotel.dto.PowerOffRequest;
import com.e303.hotel.dto.PowerOnRequest;
import org.springframework.stereotype.Service;

public interface RoomService extends IService<Room> {
    public Result powerOn(PowerOnRequest powerOnRequest);
    public Result powerOff(PowerOffRequest powerOffRequest);
    public Result adjustTemperature(AdjustTemp adjustTemp);
    public Result clientLogin(ClientLoginRequest clientLoginRequest);
}
