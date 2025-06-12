package com.e303.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.e303.hotel.bean.Bill;
import com.e303.hotel.bean.Room;

import java.util.List;

public interface BillService extends IService<Bill> {
    public Integer initBill(Integer roomId);
    public void finishBill(Integer roomId,float fee);
    public List<Bill> getBillsByRoomIdAndClientId(Integer roomId, String clientId);
}
