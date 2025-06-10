package com.e303.hotel.service.impl;

import com.e303.hotel.bean.Result;
import com.e303.hotel.dto.ReportRequest;
import com.e303.hotel.mapper.BillMapper;
import com.e303.hotel.service.ReportService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Resource
    private BillMapper billMapper;

    @Override
    public Result report(ReportRequest reportRequest) {
        LocalDateTime startTime = reportRequest.getStartTime();
        LocalDateTime endTime = reportRequest.getEndTime();

        // 直接从bill表获取统计数据
        List<Map<String, Object>> reportList = billMapper.getRoomReport(startTime, endTime);

        // 如果没有找到任何房间数据，返回错误信息
        if (reportList.isEmpty()) {
            return Result.error("400", "在指定时间段内未找到任何房间使用记录");
        }

        // 计算总使用时间和总费用
        float totalUsageTime = 0;
        float totalFee = 0;
        List<Map<String, Object>> roomSummaries = new ArrayList<>();

        for (Map<String, Object> row : reportList) {
            Integer roomId = (Integer) row.get("room_id");
            Long totalSeconds = row.get("total_seconds") == null ? 0L : ((Number) row.get("total_seconds")).longValue();
            Double roomFee = row.get("total_fee") == null ? 0.0 : ((Number) row.get("total_fee")).doubleValue();

            float usageHours = totalSeconds / 3600.0f;
            totalUsageTime += usageHours;
            totalFee += roomFee;

            Map<String, Object> roomSummary = new LinkedHashMap<>();
            roomSummary.put("room_id", roomId);
            roomSummary.put("usage_time", formatDuration(usageHours));
            roomSummary.put("total_fee", formatFee(roomFee));
            roomSummaries.add(roomSummary);
        }

        // 创建报表数据，使用LinkedHashMap保持插入顺序
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("total_rooms", reportList.size());
        report.put("total_usage_time", formatDuration(totalUsageTime));
        report.put("total_fee", formatFee(totalFee));
        report.put("room_summaries", roomSummaries);

        // 创建返回结果
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report", report);

        return Result.success(data);
    }

    private String formatDuration(float hours) {
        return String.format("%.0fh", hours);
    }

    private double formatFee(double fee) {
        return Math.round(fee * 100.0) / 100.0;
    }
}
