package com.e303.hotel.controller;

import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.dto.CheckInRequest;
import com.e303.hotel.dto.CheckOutRequest;
import com.e303.hotel.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
public class ReceptionController {

    @Resource
    private RoomService roomService;

    @ResponseBody
    @PostMapping(value = "/check_in")
    public Result checkIn(@RequestBody CheckInRequest checkInRequest, HttpSession session) {
        Integer roomId = checkInRequest.getRoomId();
        // 尝试从 Session 获取 client_id
        String clientId = (String) session.getAttribute("client_id");
        // 如果没有，就生成一个新的 UUID，并存入 session
        if (clientId == null) {
            clientId = UUID.randomUUID().toString();
            session.setAttribute("client_id", clientId);
        }
        checkInRequest.setClientId(clientId);
        Result result = roomService.checkInRoom(checkInRequest);
        if(result.getCode()=="400"){
            session.removeAttribute("client_id");
        }
        return result;
    }

    @ResponseBody
    @PostMapping(value = "/check_out")
    public Result checkOut(@RequestBody CheckOutRequest checkOutRequest, HttpSession session) {
        session.removeAttribute("client_id");
        return roomService.checkOutRoom(checkOutRequest);
    }

    @ResponseBody
    @GetMapping(value = "/get_emptyroom")
    public Result getEmptyRoom() {
        return roomService.getEmptyRoom();
    }
   

}
