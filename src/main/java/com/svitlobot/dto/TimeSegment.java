package com.svitlobot.dto;

import com.svitlobot.PowerState;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TimeSegment {
    String startTime;
    String endTime;
    PowerState state;
}
