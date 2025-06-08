package com.e303.hotel.service.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class WaitQueue {
    //private final PriorityQueue<RoomACRequest> queue;
    //
    //public WaitQueue() {
    //    queue = new PriorityQueue<>((r1, r2) -> {
    //        // 高优先级在前；同优先级时等待时间更长的在前
    //        int prioCmp = Integer.compare(r2.getTargetSpeed(), r1.getTargetSpeed());
    //        if (prioCmp != 0) return prioCmp;
    //        return Long.compare(r1.getRequestTime(), r2.getRequestTime());
    //    });
    //}
    //
    //public synchronized void add(RoomACRequest request, int waitSeconds) {
    //    request.setWaitTime(waitSeconds);
    //    queue.offer(request);
    //}
    //
    //public synchronized RoomACRequest poll() {
    //    return queue.poll();
    //}
    //
    //public synchronized List<RoomACRequest> getAll() {
    //    return new ArrayList<>(queue);
    //}
}
