package com.e303.hotel.service;

import com.e303.hotel.bean.Result;
import com.e303.hotel.dto.AdjustTempRequest;
import com.e303.hotel.dto.AdjustWindRequest;
import com.e303.hotel.dto.PowerOffRequest;
import com.e303.hotel.dto.PowerOnRequest;

public interface CentralACService {
    public Result powerOn(PowerOnRequest powerOnRequest);
    public Result powerOff(PowerOffRequest powerOffRequest);
    public Result adjustTemperature(AdjustTempRequest adjustTempRequest);
    public Result adjustWind(AdjustWindRequest adjustWindRequest);
}
