package com.e303.hotel.controller;

import com.e303.hotel.dto.ReportRequest;
import com.e303.hotel.service.impl.ReportServiceImpl;
import lombok.experimental.PackagePrivate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import com.e303.hotel.service.ReportService;
import com.e303.hotel.bean.Result;
import javax.annotation.Resource;

@Controller
public class ManagerController {
    @Resource
    private ReportService reportService;

    @ResponseBody
    @PostMapping(value = "/report")
    public Result report(@RequestBody ReportRequest reportRequest) {
        return reportService.report(reportRequest);
    }
}
