package com.e303.hotel.controller;

import com.e303.hotel.bean.Result;
import com.e303.hotel.service.CentralACService;
import com.e303.hotel.service.scheduler.RoomACRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class CentralACController {

    @Resource
    private CentralACService centralACService;

    @PostMapping("/send_request")
    public Result sendRequest(@RequestBody RoomACRequest roomACRequest) {
        return centralACService.sendRequest(roomACRequest);
    }

    @PostMapping(value = "release_room")
    public Result releaseRoom(@RequestBody RoomACRequest roomACRequest) {
        return centralACService.releaseRoom(roomACRequest);
    }

}
