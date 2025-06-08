package com.e303.hotel.service.impl;

import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import com.e303.hotel.dto.*;
import com.e303.hotel.service.CentralACService;
import com.e303.hotel.service.RoomService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class CentralACServiceImpl implements CentralACService {
    @Resource
    private RoomService roomService;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10); //服务队列，同时最多处理 10 个房间
    private Map<Integer, ScheduledFuture<?>> startTaskMap = new ConcurrentHashMap<>();
    private Map<Integer, ScheduledFuture<?>> backInitTaskTempMap = new ConcurrentHashMap<>();

    @Override
    public Result powerOn(PowerOnRequest powerOnRequest) {
        Room room = roomService.getById(powerOnRequest.getRoomId());
        if (room == null) {
            return Result.error("400", "房间不存在");
        }
        //else if (room.getStatus() != 0) {
        //    return Result.error("400", "该房间空调已开启");
        //} else if (room.getRoomStatus() != 1) {
        //    return Result.error("400", "该房间未登记入住，请先去前台登记入住该房间");
        //}
        // 设置目标值和当前风速
        float currentTemp = room.getCurrentTemp();
        room.setStatus(1);  // 开启状态
        room.setTargetTemp(powerOnRequest.getTargetTemp());
        room.setTargetSpeed(powerOnRequest.getTargetSpeed());
        room.setCurrentSpeed(powerOnRequest.getTargetSpeed());  // 当前风速立即等于目标风速
        roomService.updateById(room);

        // 启动模拟温度变化任务
        startTempControlTask(room.getRoomId(), currentTemp <= powerOnRequest.getTargetTemp() ? 0 : 1);
        return Result.success("空调已开启");
    }

    @Override
    public Result powerOff(PowerOffRequest powerOffRequest) {
        Room room = roomService.getById(powerOffRequest.getRoomId());
        if (room == null) {
            return Result.error("400", "房间不存在");
        }else if(room.getStatus()==0){
            return Result.error("400","该房间空调已关闭");
        }
        // 设置目标值和当前风速
        room.setStatus(0);
        room.setCurrentSpeed(Speed.STOP);
        roomService.updateById(room);
        // 启动模拟温度变化任务
        backInitTempControlTask(room.getRoomId());
        return Result.success("空调已关闭");
    }

    @Override
    public Result adjustTemperature(AdjustTempRequest adjustTempRequest) {
        Integer roomId = adjustTempRequest.getRoomId();
        float targetTemp = adjustTempRequest.getTargetTemp();
        Room room = roomService.getById(roomId);
        if(room==null){
            return Result.error("400","房间不存在");
        }
        //else if(room.getStatus()==0){
        //    return Result.error("400","房间空调未开启，请先开启空调");
        //}else if(room.getRoomStatus()==0){
        //    return Result.error("400","房间未登记入住，请先登记入住");
        //}
        room.setTargetTemp(targetTemp);
        roomService.updateById(room);
        return Result.success("目标温度成功修改为:"+targetTemp);
    }

    @Override
    public Result adjustWind(AdjustWindRequest adjustWindRequest) {
        Integer roomId = adjustWindRequest.getRoomId();
        Speed targetSpeed = adjustWindRequest.getTargetSpeed();
        Room room = roomService.getById(roomId);
        if(room==null){
            return Result.error("400","房间不存在");
        }
        //else if(room.getStatus()==0){
        //    return Result.error("400","房间空调未开启，请先开启空调");
        //}else if(room.getRoomStatus()==0){
        //    return Result.error("400","房间未登记入住，请先登记入住");
        //}
        room.setTargetSpeed(targetSpeed);
        room.setCurrentSpeed(targetSpeed);
        roomService.updateById(room);
        return Result.success("目标风速成功修改为:"+targetSpeed);
    }



    /**
     * @param roomId
     * @param mode   :制热还是制冷，0表示制热，1表示制冷
     */
    private void startTempControlTask(Integer roomId, int mode) {
        cancelOldTask(roomId);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            Room room = roomService.getById(roomId);
            if (room == null || room.getRoomStatus() == 0) return;

            float currentTemp = room.getCurrentTemp();
            float targetTemp = room.getTargetTemp();
            Speed speed = room.getCurrentSpeed();
            float delta = switch (speed) {
                case HIGH -> 0.6f;
                case MID -> 0.5f;
                case SLOW -> 0.4f;
                case STOP -> 0f;
                default -> 0f;
            };

            if (delta == 0f) return;

            boolean finished = false;
            if (mode == 0) { // 制热
                room.setCurrentTemp(currentTemp + delta);
                if (room.getCurrentTemp() >= targetTemp + 1) {
                    finished = true;
                }
            } else { // 制冷
                room.setCurrentTemp(currentTemp - delta);
                if (room.getCurrentTemp() <= targetTemp - 1) {
                    finished = true;
                }
            }
            roomService.updateById(room);
            System.out.println("房间 " + roomId + " 温度已更新为：" + room.getCurrentTemp());
            if (finished) {
                System.out.println("房间 " + roomId + " 达到目标温度，停止调节。");
                room.setCurrentSpeed(Speed.STOP);
                room.setStatus(0);
                roomService.updateById(room);

                //结束线程调度
                ScheduledFuture<?> f = startTaskMap.get(roomId);
                if (f != null) {
                    f.cancel(true);
                    startTaskMap.remove(roomId);
                }
                backInitTempControlTask(roomId);
            }

        }, 5, 5, TimeUnit.SECONDS);
        //}, 0, 10, TimeUnit.SECONDS);


        startTaskMap.put(roomId, future);
    }

    private void backInitTempControlTask(Integer roomId) {
        cancelOldTask(roomId);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            Room room = roomService.getById(roomId);
            if (room == null) return;
            // 初始温度设为环境温度（写死也行）
            float initialTemp = room.getInitialTemp();
            float currentTemp = room.getCurrentTemp();
            Boolean finished = false;
            // 向初始温度靠近，每分钟变化 0.5℃
            float delta = 0.5f;
            if (Math.abs(currentTemp - initialTemp) < delta) {
                room.setCurrentTemp(initialTemp);
            } else if (currentTemp < initialTemp) {
                room.setCurrentTemp(currentTemp + delta);
            } else {
                room.setCurrentTemp(currentTemp - delta);
            }
            roomService.updateById(room);
            if (room.getCurrentTemp() == initialTemp) {
                finished = true;
            }
            System.out.println("房间 " + roomId + "（关机状态）温度回归为：" + room.getCurrentTemp());
            if (finished) {
                System.out.println("房间 " + roomId + " 已经回到初始温度");
                roomService.updateById(room);
                //结束线程调度
                ScheduledFuture<?> f = backInitTaskTempMap.get(roomId);
                if (f != null) {
                    f.cancel(true);
                    backInitTaskTempMap.remove(roomId);
                }
            }
            //}, 0, 1, TimeUnit.MINUTES);
        }, 5, 5, TimeUnit.SECONDS);

        backInitTaskTempMap.put(roomId, future);
    }

    public void cancelOldTask(int roomId) {
        // 如果已有任务，先取消旧的（避免一个房间多个任务）
        if (startTaskMap.containsKey(roomId)) {
            ScheduledFuture<?> oldTask = startTaskMap.get(roomId);
            oldTask.cancel(true);
            startTaskMap.remove(roomId);
            System.out.println("房间" + roomId + "的送风任务被取消");
        }
        //结束变化到初始化温度的任务
        if (backInitTaskTempMap.containsKey(roomId)) {
            ScheduledFuture<?> oldTask = backInitTaskTempMap.get(roomId);
            oldTask.cancel(true);
            backInitTaskTempMap.remove(roomId);
            System.out.println("房间" + roomId + "的停止任务被取消");

        }
    }
}
