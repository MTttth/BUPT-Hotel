package com.e303;

import com.e303.hotel.SpringBootStarter;
import com.e303.hotel.bean.Room;
import com.e303.hotel.mapper.RoomMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = SpringBootStarter.class)
public class MapperTest {
    @Resource
    RoomMapper roomMapper ;
    @Test
    public void test() {
        Room room = roomMapper.selectById(1);
        System.out.println(room);
    }
}
