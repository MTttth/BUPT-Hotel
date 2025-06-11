package com.e303.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.e303.hotel.bean.Bill;
import com.e303.hotel.bean.Room;
import com.e303.hotel.mapper.BillMapper;
import com.e303.hotel.service.BillService;
import com.e303.hotel.service.RoomService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements BillService {
    @Resource
    private RoomService roomService;

    @Override
    public Integer initBill(Integer roomId) {
        Bill bill = new Bill();
        Room room = roomService.getById(roomId);
        String clientId = room.getClientId();
        bill.setRoomId(roomId);
        bill.setFee(0.0f);
        bill.setStartTime(LocalDateTime.now());
        //bill.setStartTime(new Date());

        bill.setClientId(clientId);
        this.save(bill); // 插入数据库
        return bill.getId(); // 获取自增的主键 ID
    }

    @Override
    public void finishBill(Integer roomId,float fee) {
        Room overRoom = roomService.getById(roomId);
        String clientId = overRoom.getClientId();
        QueryWrapper<Bill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId)
                .eq("client_id", clientId)
                .orderByDesc("start_time")
                .last("LIMIT 1");
        Bill bill = this.getOne(queryWrapper);
        bill.setEndTime(LocalDateTime.now());
        //bill.setEndTime(new Date());

        bill.setFee(fee);
        this.updateById(bill);
    }
}
