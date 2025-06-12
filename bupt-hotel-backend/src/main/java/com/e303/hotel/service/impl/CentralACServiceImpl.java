package com.e303.hotel.service.impl;

import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import com.e303.hotel.dto.AdjustTempRequest;
import com.e303.hotel.dto.AdjustWindRequest;
import com.e303.hotel.dto.PowerOffRequest;
import com.e303.hotel.dto.PowerOnRequest;
import com.e303.hotel.service.BillService;
import com.e303.hotel.service.CentralACService;
import com.e303.hotel.service.RoomService;
import com.e303.hotel.service.scheduler.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class CentralACServiceImpl implements CentralACService {
    @Resource
    private RoomService roomService;
    @Resource
    private BillService billService;
    @Resource
    private ServicePool servicePool;
    @Resource
    private WaitQueue waitQueue;
    @Resource
    private ACScheduler acScheduler;
    @Resource
    private ACServicer acServicer;


    @PostConstruct
    public void init() {
        acScheduler.startWaitCountdownLoop();
        acScheduler.startServiceTimeCounterLoop();
    }

    @Override
    public Result sendRequest(RoomACRequest roomACRequest) {
        this.acScheduler.handleRequest(roomACRequest, false);
        return Result.success("ok");
    }

    @Override
    public Result releaseRoom(RoomACRequest roomACRequest) {
        this.acScheduler.releaseRoom(roomACRequest.getRoomId());
        return Result.success("释放成功");
    }

    @Override
    public Result powerOn(PowerOnRequest powerOnRequest) {
        Room room = roomService.getById(powerOnRequest.getRoomId());
        if (room == null) {
            return Result.error(400, "房间不存在");
        } else if (room.getStatus() != 0) {
            return Result.error(400, "该房间空调已开启");
        } else if (room.getRoomStatus() != 1) {
            return Result.error(400, "该房间未登记入住，请先去前台登记入住该房间");
        }
        // 设置目标值和当前风速
        room.setStatus(1);  // 开启状态
        room.setTargetTemp(powerOnRequest.getTargetTemp());
        room.setTargetSpeed(powerOnRequest.getTargetSpeed());
        room.setCurrentSpeed(Speed.STOP);  // 当前风速先置为stop
        roomService.updateById(room);
        RoomACRequest roomACRequest = new RoomACRequest(room.getRoomId(), room.getTargetSpeed());
        // 启动模拟温度变化任务
        acScheduler.handleRequest(roomACRequest, false);
        //acServicer.startServiceControlTask(room.getRoomId(), acServicer.judgeMode(room.getRoomId()));
        return Result.success("空调已开启");
    }

    @Override
    public Result powerOff(PowerOffRequest powerOffRequest) {
        Room room = roomService.getById(powerOffRequest.getRoomId());
        if (room == null) {
            return Result.error(400, "房间不存在");
        } else if (room.getStatus() == 0) {
            return Result.error(400, "该房间空调已关闭");
        } else if (room.getRoomStatus() == 0) {
            return Result.error(400, "该房间未入住");
        }
        // 设置目标值和当前风速
        room.setStatus(0);
        room.setCurrentSpeed(Speed.STOP);
        roomService.updateById(room);
        // 启动模拟温度变化任务
        acServicer.backInitTempControlTask(room.getRoomId());
        acScheduler.releaseRoom(room.getRoomId());
        return Result.success("空调已关闭");
    }

    @Override
    public Result adjustTemperature(AdjustTempRequest adjustTempRequest) {
        Integer roomId = adjustTempRequest.getRoomId();
        float targetTemp = adjustTempRequest.getTargetTemp();
        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error(400, "房间不存在");
        } else if (room.getRoomStatus() == 0) {
            return Result.error(400, "房间未登记入住，请先登记入住");
        } else if(room.getStatus() == 0) {
            return Result.error(400,"房间空调未开启,请先开启空调");
        }
        room.setTargetTemp(targetTemp);
        roomService.updateById(room);
        return Result.success("目标温度成功修改为:" + targetTemp);
    }

    @Override
    public Result adjustWind(AdjustWindRequest adjustWindRequest) {
        Integer roomId = adjustWindRequest.getRoomId();
        Speed targetSpeed = adjustWindRequest.getTargetSpeed();
        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error(400, "房间不存在");
        } else if (room.getRoomStatus() == 0) {
            return Result.error(400, "房间未登记入住，请先登记入住");
        } else if(room.getRoomStatus() == 0) {
            return Result.error(400,"房间空调未开启，请先开启空调");
        }
        room.setTargetSpeed(targetSpeed);
        roomService.updateById(room);
        RoomACRequest roomACRequest = new RoomACRequest(roomId, targetSpeed);
        this.sendRequest(roomACRequest);
        return Result.success("目标风速成功修改为:" + targetSpeed);
    }


}
