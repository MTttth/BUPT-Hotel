package com.e303.hotel.controller;

import com.e303.hotel.bean.Result;
import com.e303.hotel.bean.Room;
import com.e303.hotel.dto.*;
import com.e303.hotel.service.CentralACService;
import com.e303.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Controller
public class ClientController {

    @Resource
    private RoomService roomService;
    @Resource
    private CentralACService centralACService;


    @ResponseBody
    @PostMapping("/power_on")
    public Result powerOn(@RequestBody PowerOnRequest powerOnRequest) {

        //String clientId = (String)session.getAttribute("client_id");
        //if(clientId == null) {
        //    return Result.error(400,"您还没有入住呢？调啥空调我请问了？？");
        //}
        //// 设置到请求对象中
        //powerOnRequest.setClientId(clientId);
        // 调用服务层
        return centralACService.powerOn(powerOnRequest);
    }

    @ResponseBody
    @PostMapping(value = "/power_off")
    public Result powerOff(@RequestBody PowerOffRequest powerOffRequest) {
        return centralACService.powerOff(powerOffRequest);
    }

    @ResponseBody
    @PostMapping(value = "/adjust_temperature")
    public Result adjustTemperature(@RequestBody AdjustTempRequest adjustTempRequest) {
        return centralACService.adjustTemperature(adjustTempRequest);
    }

    @ResponseBody
    @PostMapping(value = "/adjust_wind")
    public Result adjustWind(@RequestBody AdjustWindRequest adjustWindRequest) {
        return centralACService.adjustWind(adjustWindRequest);
    }

    @ResponseBody
    @PostMapping(value = "/status_heartbeat")
    public Result statusHeartbeat(@RequestBody Room reqRoom) {
        Integer roomId = reqRoom.getRoomId();
        //System.out.println(roomId);
        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error(400, "房间不存在");
        }
        Result success = Result.success();
        success.setMsg("成功获取房间状态");
        success.setData(room);
        return success;
    }



}
