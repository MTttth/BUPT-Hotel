package com.e303.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.e303.hotel.bean.Bill;
import com.e303.hotel.bean.Room;

public interface BillService extends IService<Bill> {
    public Integer initBill(Integer roomId);
}
