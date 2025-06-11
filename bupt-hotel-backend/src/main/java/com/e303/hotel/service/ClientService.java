package com.e303.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.e303.hotel.bean.Bill;
import com.e303.hotel.bean.Client;

public interface ClientService extends IService<Client> {
    public void saveClient(String clientId);
}
