package com.e303.hotel.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    @TableId(value = "id", type = IdType.AUTO) // 👈 这行很关键！
    private Integer id;
    // 可扩展：用户名、联系方式等

    // Getters and setters...
}
