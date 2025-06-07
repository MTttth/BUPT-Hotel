package com.e303.hotel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClientLoginRequest {
    private Integer roomId;
    private String password;
}