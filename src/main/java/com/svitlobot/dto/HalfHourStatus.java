package com.svitlobot.dto;

import com.svitlobot.PowerState;
import lombok.Data;

@Data
public class HalfHourStatus {
    private PowerState leftHalf;
    private PowerState rightHalf;
}
