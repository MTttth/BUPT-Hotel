package com.e303.hotel.controller;

import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.dto.ClientLoginRequest;
import com.e303.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class ClientController {

    @Autowired
    private RoomService roomService;

    @ResponseBody
    @PostMapping(value = "/client_login")
    public Result clientJoin(@RequestBody ClientLoginRequest clientLoginRequest) {
        Integer roomId = clientLoginRequest.getRoomId();
        String password = clientLoginRequest.getPassword();

        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error("400", "房间不存在");
        }
        // 建议使用加密密码时的匹配方式
        if (password != null && password.equals(room.getPassword())) {
            Result success = Result.success();
            success.setMsg("入住成功");
            return success;
        }
        return Result.error("400", "密码错误，入住失败");
    }


}
