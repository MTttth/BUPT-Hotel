package com.e303.hotel.service.scheduler;

import com.e303.hotel.bean.Room;
import com.e303.hotel.bean.enums.Speed;
import com.e303.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//调度对象
@Component
public class ACScheduler {
    @Resource
    private ServicePool servicePool;
    @Resource
    private WaitQueue waitQueue;
    @Resource
    private ACServicer acServicer;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    @Autowired
    private RoomService roomService;

    public synchronized void handleRequest(RoomACRequest request, Boolean forceTry) {
        //服务队列
        Map<Integer, RoomACRequest> activeServices = servicePool.getActiveServices();
        // 如果服务池未满
        if (activeServices.size() < ServicePool.MAX_SERVICE_ROOMS) {
            activeServices.put(request.getRoomId(), request);
            System.out.println("房间" + request.getRoomId() +",风速:"+request.getTargetSpeed()+" 被放入服务队列");
            int mode = acServicer.judgeMode(request.getRoomId());
            this.updateRoomSpeed(request);
            acServicer.startServiceControlTask(request.getRoomId(), mode);
            return;
        }
        Map.Entry<Integer, RoomACRequest> minSpeedLongestService = servicePool.getMinSpeedLongestService().orElse(null);
        Speed targetSpeed = request.getTargetSpeed();
        if (targetSpeed.getPriority() > minSpeedLongestService.getValue().getTargetSpeed().getPriority()) {//如果请求的风速大于服务的最低风速
            //找到最低风速同时服务时长最高的那个请求，把他从服务队列中去除，初始化等待时长，放到wait队列中。
            Integer minSpeedLongestServiceRoomId = minSpeedLongestService.getKey();
            acServicer.backInitTempControlTask(minSpeedLongestServiceRoomId);//取消送风服务
            activeServices.remove(minSpeedLongestServiceRoomId);
            System.out.println("房间" + minSpeedLongestServiceRoomId +",风速:"+minSpeedLongestService.getValue().getTargetSpeed()+ "被移出服务队列");
            this.stopRoomSpeed(minSpeedLongestServiceRoomId);
            waitQueue.add(minSpeedLongestService.getValue(), 60);
            System.out.println("房间" + minSpeedLongestServiceRoomId  +",风速:"+minSpeedLongestService.getValue().getTargetSpeed()+ "被放入等待队列,需等待" + minSpeedLongestService.getValue().getWaitTime() + "秒");

            activeServices.put(request.getRoomId(), request);
            System.out.println("房间" + request.getRoomId() +",风速:"+request.getTargetSpeed()+ "被放入服务队列");
            this.updateRoomSpeed(request);
            acServicer.startServiceControlTask(request.getRoomId(), acServicer.judgeMode(request.getRoomId()));//开启送风服务

        } else if (targetSpeed.getPriority() == minSpeedLongestService.getValue().getTargetSpeed().getPriority()) {
            if (forceTry) {
                // 抢占服务时长最长的那个（风速相等）
                Integer minSpeedLongestServiceRoomId = minSpeedLongestService.getKey();
                acServicer.backInitTempControlTask(minSpeedLongestServiceRoomId);//取消送风服务
                RoomACRequest kicked = activeServices.remove(minSpeedLongestServiceRoomId);
                waitQueue.add(kicked, 60);
                System.out.println("房间" + minSpeedLongestServiceRoomId +",风速:"+minSpeedLongestService.getValue().getTargetSpeed()+ "被移出服务队列");
                this.stopRoomSpeed(minSpeedLongestServiceRoomId);

                activeServices.put(request.getRoomId(), request);
                System.out.println("房间" + request.getRoomId() +",风速:"+request.getTargetSpeed()+ "被放入服务队列");
                this.updateRoomSpeed(request);
                acServicer.startServiceControlTask(request.getRoomId(), acServicer.judgeMode(request.getRoomId()));//开启送风服务

            } else {
                waitQueue.add(request, 60);
                System.out.println("房间" + request.getRoomId() +",风速:"+request.getTargetSpeed()+ "被放入等待队列,需等待" + request.getWaitTime() + "秒");
            }
        } else { //如果请求的风速小于最低风速
            //request.setPriority(request.getPriority() + 1);
            waitQueue.add(request, 60);
            System.out.println("房间" + request.getRoomId() +",风速:"+request.getTargetSpeed()+ "被放入等待队列,需等待" + request.getWaitTime() + "秒");
        }

    }

    // 模拟服务完成、释放资源
    public synchronized void releaseRoom(Integer roomId) {
        //服务队列
        Map<Integer, RoomACRequest> activeServices = servicePool.getActiveServices();
        RoomACRequest roomACRequest = activeServices.get(roomId);
        Speed targetSpeed = roomACRequest.getTargetSpeed();
        if (activeServices.remove(roomId) != null) {
            System.out.println("房间 " + roomId +",风速:"+targetSpeed+ " 服务结束，释放服务资源");
            RoomACRequest next = waitQueue.poll();
            if (next != null) {
                //activeServices.put(next.getRoomId(), next);
                handleRequest(next,false);
                System.out.println("等待队列中的房间 " + next.getRoomId() +",风速:"+next.getTargetSpeed()+ " 获得服务");
            }
        }
    }

    public void startWaitCountdownLoop() {
        List<RoomACRequest> toPromote = new ArrayList<>();
        System.out.println("======waitTime倒计时监控器启动======");
        scheduler.scheduleAtFixedRate(() -> {
            List<RoomACRequest> waitQueueAll = waitQueue.getAll();
            if (waitQueueAll.size() > 0) {
                for (RoomACRequest request : waitQueueAll) {
                    int timeLeft = request.getWaitTime();
                    if (timeLeft > 0) {
                        request.setWaitTime(timeLeft - 1);
                    }
                    if (request.getWaitTime() <= 0) {
                        toPromote.add(request);
                    }
                }

                // 从等待队列中移除，转入服务队列
                for (RoomACRequest promote : toPromote) {
                    waitQueue.remove(promote);
                    promote.setPriority(promote.getPriority() + 1);
                    System.out.println("房间 " + promote.getRoomId() +",风速:"+promote.getTargetSpeed()+ " 倒计时结束，再次发送服务请求");
                    handleRequest(promote, true); // 再次发送服务请求
                }
                toPromote.clear();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void startServiceTimeCounterLoop() {
        System.out.println("======服务时长计时器启动======");
        scheduler.scheduleAtFixedRate(() -> {
            for (RoomACRequest request : servicePool.getActiveServices().values()) {
                int time = request.getServiceTime();
                request.setServiceTime(time + 1);
                //System.out.println("房间 " + request.getRoomId() + " 的服务时长为：" + (time + 1) + " 秒");
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    public void updateRoomSpeed(RoomACRequest request) {
        int roomId = request.getRoomId();
        Speed targetSpeed = request.getTargetSpeed();
        Room room = roomService.getById(roomId);
        room.setCurrentSpeed(targetSpeed);
        roomService.updateById(room);
    }
    public void stopRoomSpeed(Integer roomId) {
        Room room = roomService.getById(roomId);
        room.setCurrentSpeed(Speed.STOP);
        roomService.updateById(room);
    }
}
