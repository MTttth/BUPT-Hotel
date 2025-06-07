package com.e303.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.dto.*;

import java.util.List;

public interface RoomService extends IService<Room> {
    public Result powerOn(PowerOnRequest powerOnRequest);
    public Result powerOff(PowerOffRequest powerOffRequest);
    public Result adjustTemperature(AdjustTempRequest adjustTempRequest);
    public Result adjustWind(AdjustWindRequest adjustWindRequest);
    public Result clientLogin(ClientLoginRequest clientLoginRequest);
    public Result getAllRoomDetaisList();
}
