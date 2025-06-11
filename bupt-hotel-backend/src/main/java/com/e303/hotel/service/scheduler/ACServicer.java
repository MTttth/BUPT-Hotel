package com.e303.hotel.service.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.e303.hotel.bean.Bill;
import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import com.e303.hotel.service.BillService;
import com.e303.hotel.service.RoomService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

//服务对象
@Component
public class ACServicer {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10); //服务队列，同时最多处理 10 个房间
    @Resource
    private RoomService roomService;
    @Resource
    BillService billService;
    @Resource
    private ServicePool servicePool;
    private Map<Integer, ScheduledFuture<?>> startTaskMap = new ConcurrentHashMap<>();
    private Map<Integer, ScheduledFuture<?>> backInitTaskTempMap = new ConcurrentHashMap<>();
    @Lazy
    @Resource
    private ACScheduler acScheduler;

    /**
     * @param roomId
     * @param mode   :制热还是制冷，0表示制热，1表示制冷
     */
    public void startServiceControlTask(Integer roomId, int mode) {
        cancelOldTask(roomId);
        Room tempRoom = roomService.getById(roomId);
        if (tempRoom == null || tempRoom.getRoomStatus() == 0) return;
        tempRoom.setCurrentSpeed(tempRoom.getTargetSpeed());
        roomService.updateById(tempRoom);
        //生成账单（初始化）
        Integer billId = billService.initBill(roomId);
        //float[] totalDegree = {0.0f};
        RoomACRequest roomACRequest = servicePool.getActiveServices().get(roomId);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            Room room = roomService.getById(roomId);
            if (room == null || room.getRoomStatus() == 0) return;
            float currentTemp = room.getCurrentTemp();
            float targetTemp = room.getTargetTemp();

            Speed speed = room.getCurrentSpeed();
            float delta = switch (speed) {  //每次变化的温度
                case HIGH -> 0.6f;
                case MID  -> 0.5f;
                case SLOW -> 0.4f;
                case STOP -> 0f;
                default   -> 0f;
            };
            float degreePer = switch (speed) {  //每次变化使用的度数
                case HIGH -> 1.0f;
                case MID  -> 0.5f;
                case SLOW -> 0.33f;
                case STOP -> 0f;
                default   -> 0f;
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
            roomACRequest.setTotalDegree(roomACRequest.getTotalDegree() + degreePer);
            //totalDegree[0]+=degreePer;
            room.setElectricalUsage(room.getElectricalUsage()+degreePer);
            room.setFee(room.getFee()+degreePer*Room.feePerDegree);
            roomService.updateById(room);
            System.out.println("房间 " + roomId + " 温度已更新为：" + room.getCurrentTemp());
            if (finished) {
                System.out.println("房间 " + roomId + " 达到目标温度，停止调节。");
                room.setCurrentSpeed(Speed.STOP);
                room.setStatus(0);
                roomService.updateById(room);
                //roomACRequest.setTotalDegree(totalDegree[0]);
                //结束线程调度
                ScheduledFuture<?> f = startTaskMap.get(roomId);
                if (f != null) {
                    f.cancel(true);
                    startTaskMap.remove(roomId);
                }
                backInitTempControlTask(roomId);
                acScheduler.releaseRoom(roomId);
                //totalDegree[0]=0.0f;
            }

        }, 5, 5, TimeUnit.SECONDS);
        //}, 1, 1, TimeUnit.MINUTES);


        startTaskMap.put(roomId, future);
    }

    public void backInitTempControlTask(Integer roomId) {
        cancelOldTask(roomId);
        RoomACRequest roomACRequest = servicePool.getActiveServices().get(roomId);
        float totalDegree = roomACRequest.getTotalDegree();
        billService.finishBill(roomId, totalDegree*Room.feePerDegree);
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
            //}, 1, 1, TimeUnit.MINUTES);
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
            System.out.println("房间" + roomId + "的回到初始温度任务被取消");

        }
    }

    /**
     * 返回这个房间是要制热还是要制冷，0制热，1制冷
     * @param roomId
     * @return
     */
    public int judgeMode(int roomId) {
        Room room = roomService.getById(roomId);
        return room.getCurrentTemp()<room.getTargetTemp() ? 0 : 1;
    }
}
