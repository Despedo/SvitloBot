package com.svitlobot.dto;

import lombok.Data;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

@Data
public class DaySchedule {
    public MonthDay date;
    public List<HalfHourStatus> hours = new ArrayList<>(); // 24 елементи (по годині)
}
