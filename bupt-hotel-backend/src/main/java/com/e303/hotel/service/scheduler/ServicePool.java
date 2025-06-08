package com.e303.hotel.service.scheduler;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServicePool {
    //private final int maxServiceRooms = 3; //最大服务房间数
    //private final Map<Integer, RoomACRequest> activeServices = new ConcurrentHashMap<>();  //服务队列
    //private final WaitQueue waitQueue = new WaitQueue();   //等待队列
    //public synchronized void handleRequest(RoomACRequest request) {
    //    // 如果服务池未满
    //    if (activeServices.size() < maxServiceRooms) {
    //        activeServices.put(request.getRoomId(), request);
    //        System.out.println("房间 " + request.getRoomId() + " 开始服务");
    //        return;
    //    }
    //
    //    // 判断优先级调度
    //    Optional<Map.Entry<String, RoomACRequest>> lowerPriority = activeServices.entrySet().stream()
    //            .filter(entry -> entry.getValue().getTargetSpeed().getPriority() < request.getTargetSpeed().getPriority())
    //            .min(Comparator.comparingInt(e -> e.getValue().getTargetSpeed().getPriority()));
    //
    //    if (lowerPriority.isPresent()) {
    //        String removedRoom = lowerPriority.get().getKey();
    //        RoomACRequest removedRequest = activeServices.remove(removedRoom);
    //        waitQueue.add(removedRequest, 5); // 进入等待队列，s秒假设为5
    //        activeServices.put(request.getRoomId(), request);
    //        System.out.println("房间 " + removedRoom + " 被换下，" + request.getRoomId() + " 替换进入服务");
    //        return;
    //    }
    //
    //    // 否则进入时间片策略
    //    waitQueue.add(request, 5);
    //    System.out.println("房间 " + request.getRoomId() + " 加入等待队列");
    //}
    //// 模拟服务完成、释放资源
    //public synchronized void releaseRoom(String roomId) {
    //    if (activeServices.remove(roomId) != null) {
    //        System.out.println("房间 " + roomId + " 服务结束，释放服务资源");
    //
    //        RoomACRequest next = waitQueue.poll();
    //        if (next != null) {
    //            activeServices.put(next.getRoomId(), next);
    //            System.out.println("等待队列中的房间 " + next.getRoomId() + " 获得服务");
    //        }
    //    }
    //}
    //
    //public Map<String, RoomACRequest> getActiveServices() {
    //    return activeServices;
    //}
    //
    //public WaitQueue getWaitQueue() {
    //    return waitQueue;
    //}
}
