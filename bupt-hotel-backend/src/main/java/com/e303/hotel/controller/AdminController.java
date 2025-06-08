package com.e303.hotel.controller;

import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.dto.RoomDTO;
import com.e303.hotel.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller

public class AdminController {
    @Resource
    private RoomService roomService;

    @ResponseBody
    @PostMapping(value = "/get_room_detail_list")
    public Result getRoomDetailList(){
        return roomService.getAllRoomDetialList();
    }
}
