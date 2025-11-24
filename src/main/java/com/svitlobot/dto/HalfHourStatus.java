package com.svitlobot.dto;

import com.svitlobot.PowerState;
import lombok.Data;

@Data
public class HalfHourStatus {
    public PowerState leftHalf;  // 00-29 хв
    public PowerState rightHalf; // 30-59 хв
}
