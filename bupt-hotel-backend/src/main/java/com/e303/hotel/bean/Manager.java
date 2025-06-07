package com.e303.hotel.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Manager.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manager {
    @TableId(value = "id", type = IdType.AUTO) // 👈 这行很关键！
    private Integer id;
    private String username;
    private String password;

    // Getters and setters...
}