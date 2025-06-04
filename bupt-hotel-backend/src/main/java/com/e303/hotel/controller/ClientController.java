package com.e303.hotel.controller;

import com.e303.hotel.bean.Result;
import com.e303.hotel.dto.ClientLoginRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class ClientController {

    @ResponseBody
    @PostMapping(value = "/client_login")
    public Result clientJoin(@RequestBody ClientLoginRequest clientLoginRequest) {
        Integer roomId = clientLoginRequest.getRoomId();
        String password = clientLoginRequest.getPassword();
        System.out.println(roomId);

        if (Integer.valueOf(100).equals(roomId) && "123".equals(password)) {
            Result success = Result.success();
            success.setMsg("登陆成功");
            return success;
        }
        return Result.error("400", "登陆失败");
    }


}
