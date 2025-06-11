package com.e303.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.dto.*;

import java.util.List;

public interface RoomService extends IService<Room> {

    public Result getAllRoomDetialList();
    public Result checkInRoom(CheckInRequest checkInRequest);
}
