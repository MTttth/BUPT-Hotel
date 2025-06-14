package com.e303.hotel.service.scheduler;

import com.e303.hotel.bean.enums.Speed;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Component
public class ServicePool {
    public static final int MAX_SERVICE_ROOMS = 3;
    private final int maxServiceRooms = MAX_SERVICE_ROOMS; //最大服务房间数
    private final Map<Integer, RoomACRequest> activeServices = new ConcurrentHashMap<>();  //服务队列

    //返回当前服务队列中风速最低同时服务时长最高的那个房间对应的roomId
    public Optional<Map.Entry<Integer, RoomACRequest>> getMinSpeedLongestService() {
        return activeServices.entrySet().stream()
                .min(Comparator
                        .comparingInt((Map.Entry<Integer, RoomACRequest> entry) -> entry.getValue().getTargetSpeed().getPriority())
                        .thenComparingInt(entry -> -entry.getValue().getServiceTime())); // 服务时间最长（倒序）
    }


}
