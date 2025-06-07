package com.e303.hotel.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reception {
    private Integer id;
    private String username;
    private String password;

    // Getters and setters...
}