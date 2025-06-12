package com.e303.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.e303.hotel.bean.Bill;
import com.e303.hotel.bean.Client;
import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import com.e303.hotel.dto.*;
import com.e303.hotel.mapper.RoomMapper;
import com.e303.hotel.service.BillService;
import com.e303.hotel.service.ClientService;
import com.e303.hotel.service.RoomService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.apache.catalina.manager.StatusTransformer.formatTime;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {
    @Resource
    private ClientService clientService;
    @Lazy
    @Resource
    private BillService billService;

    @Override
    public Result checkOutRoom(CheckOutRequest checkOutRequest) {
        //private Integer roomId;
        //private String clientId;
        //private Integer roomStatus; // 0: 空闲, 1: 不空闲
        //private Integer status;     // 0: 关闭, 1: 开启
        //private Speed currentSpeed; //"high" "mid" "slow" "stop"
        //private Speed targetSpeed;  // "high" "mid" "slow" "stop"
        //private float electricalUsage;
        //private float fee;
        Integer roomId = checkOutRequest.getRoomId();
        Room room = this.getById(roomId);
        String clientId = room.getClientId();
        float fee = room.getFee();
        List<Bill> bills = billService.getBillsByRoomIdAndClientId(roomId, clientId);
        List<Map<String, Object>> detailedList = bills.stream()
                .map(bill -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("start_time", bill.getStartTime());
                    item.put("end_time", bill.getEndTime());
                    item.put("fee", bill.getFee());
                    return item;
                })
                .collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("bill", fee);
        data.put("detailed_list", detailedList);
        //退房，更新房间的数据
        this.overRoom(roomId);
        Result success = Result.success("退房成功!");
        success.setData(data);
        return success;
    }

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
    public void overRoom(Integer roomId) {
        Room room = this.getById(roomId);
        room.setCurrentSpeed(Speed.STOP);
        room.setTargetSpeed(Speed.STOP);
        room.setElectricalUsage(0);
        room.setClientId("free");
        room.setFee(0.0f);
        room.setStatus(0);
        room.setRoomStatus(0);
        this.updateById(room);
    }

    @Override
    public Result getEmptyRoom() {
        QueryWrapper<Room> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_status", 0);
        List<Room> emptyRooms = this.list(queryWrapper);
        if (emptyRooms.isEmpty()) {
            return Result.error("400", "没有空闲房间");
        }
        List<Integer> emptyRoomIds = emptyRooms.stream().map(Room::getRoomId).collect(Collectors.toList());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("empty_room_list", emptyRoomIds);
        Result result = Result.success("获取成功");
        result.setData(dataMap);
        return result;
    }


}
