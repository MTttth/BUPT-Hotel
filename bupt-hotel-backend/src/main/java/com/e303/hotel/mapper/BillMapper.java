package com.e303.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e303.hotel.bean.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {
    @Select("SELECT room_id, SUM(TIMESTAMPDIFF(SECOND, start_time, end_time)) AS total_seconds, SUM(fee) AS total_fee FROM bill WHERE start_time >= #{startTime} AND end_time <= #{endTime} GROUP BY room_id")
    List<Map<String, Object>> getRoomReport(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
