package com.svitlobot.dto;

import lombok.Data;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

@Data
public class DaySchedule {
    private MonthDay date;
    private List<HalfHourStatus> hours = new ArrayList<>();
}
