package com.e303.hotel.service.scheduler;

import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import com.e303.hotel.service.BillService;
import com.e303.hotel.service.RoomService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.*;

//服务对象
@Component
public class ACServicer {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10); //线程池，同时最多处理 10 个房间
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
    private static int flashTime = 10;

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
                case high -> 0.6f;
                case mid -> 0.5f;
                case slow -> 0.4f;
                case stop -> 0f;
                default -> 0f;
            };
            float degreePer = switch (speed) {  //每次变化使用的度数
                case high -> 1.0f;
                case mid -> 0.5f;
                case slow -> 0.33f;
                case stop -> 0f;
                default -> 0f;
            };

            if (delta == 0f) return;

            boolean finished = false;
            if (mode == 0) { // 制热
                room.setCurrentTemp(currentTemp + delta);
                if (room.getCurrentTemp() >= targetTemp ) {
                    room.setCurrentTemp(targetTemp);
                    finished = true;
                }
            } else { // 制冷
                room.setCurrentTemp(currentTemp - delta);
                if (room.getCurrentTemp() <= targetTemp ) {
                    room.setCurrentTemp(targetTemp);
                    finished = true;
                }
            }
            roomACRequest.setTotalDegree(roomACRequest.getTotalDegree() + degreePer);
            //totalDegree[0]+=degreePer;
            room.setElectricalUsage(room.getElectricalUsage() + degreePer);
            room.setFee(room.getFee() + degreePer * Room.feePerDegree);
            roomService.updateById(room);
            System.out.println("房间 " + roomId + ",风速:" + room.getCurrentSpeed() + " 温度已更新为：" + room.getCurrentTemp());
            if (finished) {
                System.out.println("房间 " + roomId + ",风速:" + room.getCurrentSpeed() + " 达到目标温度，停止调节。");
                room.setCurrentSpeed(Speed  .stop);
                roomService.updateById(room);
                //roomACRequest.setTotalDegree(totalDegree[0]);
                //结束线程调度
                ScheduledFuture<?> f = startTaskMap.get(roomId);
                //回归到目标温度+-1度
                backOneDegreeTask(roomId,mode);
                acScheduler.releaseRoom(roomId);
                if (f != null) {
                    startTaskMap.remove(roomId);
                    f.cancel(false);
                }

                //totalDegree[0]=0.0f;
            }

        }, ACServicer.flashTime, ACServicer.flashTime, TimeUnit.SECONDS);
        //}, 1, 1, TimeUnit.MINUTES);


        startTaskMap.put(roomId, future);
    }
    public void backOneDegreeTask(Integer roomId, int mode) {
        cancelOldBackInitTask(roomId); // 先取消旧任务
        Room tempRoom = roomService.getById(roomId);
        if (tempRoom == null || tempRoom.getRoomStatus() == 0) return;
        float startTemp = tempRoom.getCurrentTemp();
        final float defaultTemp = tempRoom.getInitialTemp();
        final float delta = 0.5f;
        //生成账单
        billService.finishBill(roomId,ACScheduler.allServices.get(roomId).getTotalDegree()*Room.feePerDegree);
        ACScheduler.allServices.get(roomId).setTotalDegree(0);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {

            Room room = roomService.getById(roomId);
            if (room == null) return;
            float currentTemp = room.getCurrentTemp();
            float newTemp = currentTemp;
            boolean finished = false;

            // 模式判断
            if (mode == 0 && currentTemp > defaultTemp) {
                newTemp = Math.max(currentTemp - delta, defaultTemp);
            } else if (mode == 1 && currentTemp < defaultTemp) {
                newTemp = Math.min(currentTemp + delta, defaultTemp);
            } else {
                // 不符合调整条件，直接结束
                finished = true;
            }

            room.setCurrentTemp(newTemp);
            roomService.updateById(room);

            System.out.println("房间 " + roomId + " 温度回归为：" + newTemp);
            if(Math.abs(newTemp - startTemp) >= 1.0f || Math.abs(newTemp - defaultTemp) < 0.01f ){
                finished = true;
            }
            // 判断是否变化了1度或到达默认温度
            if (finished) {
                System.out.println("房间 " + roomId + " 已完成背离温度1度任务，发送请求并停止调度");
                // 发送 handleRequest 请求
                RoomACRequest roomACRequest = new RoomACRequest(room.getRoomId(), room.getTargetSpeed());
                acScheduler.handleRequest(roomACRequest,false);
                // 停止任务
                ScheduledFuture<?> f = backInitTaskTempMap.get(roomId);
                if (f != null) {
                    f.cancel(false);
                }
                backInitTaskTempMap.remove(roomId);
            }
        }, ACServicer.flashTime, ACServicer.flashTime, TimeUnit.SECONDS); // 每2秒执行一次

        // 存储任务
        backInitTaskTempMap.put(roomId, future);
    }


    public void backInitTempControlTask(Integer roomId) {
        cancelOldBackInitTask(roomId);
        Room tempRoom = roomService.getById(roomId);
        if (tempRoom == null || tempRoom.getRoomStatus() == 0) return;
        tempRoom.setCurrentSpeed(Speed.stop);
        roomService.updateById(tempRoom);

        RoomACRequest roomACRequest = ACScheduler.allServices.get(roomId);
        float totalDegree = roomACRequest.getTotalDegree();
        billService.finishBill(roomId, totalDegree * Room.feePerDegree);
        roomACRequest.setTotalDegree(0);
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
                backInitTaskTempMap.remove(roomId);
                if (f != null) {
                    f.cancel(false);
                }
            }
            //}, 1, 1, TimeUnit.MINUTES);
        }, ACServicer.flashTime, ACServicer.flashTime, TimeUnit.SECONDS);

        backInitTaskTempMap.put(roomId, future);
    }

    public void cancelOldStartTask(int roomId) {
        // 如果已有任务，先取消旧的（避免一个房间多个任务）
        if (startTaskMap.containsKey(roomId)) {
            ScheduledFuture<?> oldTask = startTaskMap.get(roomId);
            oldTask.cancel(false);
            startTaskMap.remove(roomId);
            //System.out.println("房间" + roomId + "的送风任务被取消");
        }

    }

    public void cancelOldBackInitTask(int roomId) {

        //结束变化到初始化温度的任务
        if (backInitTaskTempMap.containsKey(roomId)) {
            ScheduledFuture<?> oldTask = backInitTaskTempMap.get(roomId);
            oldTask.cancel(false);
            backInitTaskTempMap.remove(roomId);
            //System.out.println("房间" + roomId + "的回到初始温度任务被取消");

        }
    }
    public void cancelOldTask(int roomId) {
        // 如果已有任务，先取消旧的（避免一个房间多个任务）
        if (startTaskMap.containsKey(roomId)) {
            ScheduledFuture<?> oldTask = startTaskMap.get(roomId);
            oldTask.cancel(false);
            startTaskMap.remove(roomId);
            //System.out.println("房间" + roomId + "的送风任务被取消");
        }
        //结束变化到初始化温度的任务
        Speed targetSpeed = roomService.getById(roomId).getTargetSpeed();
        if(targetSpeed != Speed.stop){
            if (backInitTaskTempMap.containsKey(roomId)) {
                ScheduledFuture<?> oldTask = backInitTaskTempMap.get(roomId);
                oldTask.cancel(false);
                backInitTaskTempMap.remove(roomId);
                //System.out.println("房间" + roomId + "的回到初始温度任务被取消");
            }
        }

    }
    /**
     * 返回这个房间是要制热还是要制冷，0制热，1制冷
     *
     * @param roomId
     * @return
     */
    public int judgeMode(int roomId) {
        Room room = roomService.getById(roomId);
        return room.getCurrentTemp() < room.getTargetTemp() ? 0 : 1;
    }
}
