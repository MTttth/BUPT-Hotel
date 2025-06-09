package com.e303.hotel.service.scheduler;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

@Component
public class WaitQueue {
    private final PriorityQueue<RoomACRequest> queue;

    public WaitQueue() {
        queue = new PriorityQueue<>((r1, r2) -> {
            // 高优先级在前；同优先级时等待时间更长的在前
            int speedCmp = Integer.compare(r2.getTargetSpeed().getPriority(), r1.getTargetSpeed().getPriority());
            int prioCmp = Integer.compare(r2.getPriority(), r1.getPriority());
            if (speedCmp != 0) return speedCmp;
            if(prioCmp != 0) return prioCmp;
            return Long.compare(r1.getWaitTime(), r2.getWaitTime());
        });
    }

    public synchronized void add(RoomACRequest request, int waitSeconds) {
        request.setWaitTime(waitSeconds);
        queue.offer(request);
    }

    public synchronized RoomACRequest poll() {
        return queue.poll();
    }

    public synchronized List<RoomACRequest> getAll() {
        return new ArrayList<>(queue);
    }
    public synchronized void remove(RoomACRequest request) {
        queue.remove(request);
    }
}

