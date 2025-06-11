package com.e303.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.e303.hotel.bean.Client;
import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import com.e303.hotel.dto.*;
import com.e303.hotel.mapper.RoomMapper;
import com.e303.hotel.service.ClientService;
import com.e303.hotel.service.RoomService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {
    @Resource
    private ClientService clientService;



    @Override
    public Result checkInRoom(CheckInRequest checkInRequest) {
        Integer roomId = checkInRequest.getRoomId();
        String clientId = checkInRequest.getClientId();
        Room room = getById(roomId);
        if (room == null) {
            return Result.error("400", "房间不存在");
        }else if(room.getRoomStatus()==1){
            return Result.error("400","房间已入住");
        }
        room.setClientId(clientId);
        this.initRoom(room);
        clientService.saveClient(clientId);
        return Result.success("用户"+clientId+"入住成功,房间号为:"+roomId);
    }

    @Override
    public Result getAllRoomDetialList() {
        List<Room> roomList = this.list();
        Result success = Result.success("查询成功");
        List<RoomDTO> rooms = roomList.stream()
                .map(RoomDTO::new) // 转 DTO，隐藏 password
                .collect(Collectors.toList());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rooms", rooms);
        success.setData(dataMap);
        return success;
    }
    public Room initRoom(Room room) {
        room.setRoomStatus(1);
        room.setElectricalUsage(0);
        room.setFee(0);
        room.setTargetSpeed(Speed.STOP);
        room.setCurrentSpeed(Speed.STOP);
        updateById(room);
        return room;
    }


}
