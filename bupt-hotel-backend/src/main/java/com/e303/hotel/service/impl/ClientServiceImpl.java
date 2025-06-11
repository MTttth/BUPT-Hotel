package com.e303.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.e303.hotel.bean.Client;
import com.e303.hotel.mapper.ClientMapper;
import com.e303.hotel.service.ClientService;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, Client> implements ClientService {
    @Override
    public void saveClient(String clientId) {
        Client byId = this.getById(clientId);
        if(byId != null) {
            Client client = new Client();
            client.setId(clientId);
            this.save(client);
        }
    }
}
