package com.svitlobot.service;

import com.svitlobot.PowerState;
import com.svitlobot.dto.DaySchedule;
import com.svitlobot.dto.HalfHourStatus;
import com.svitlobot.dto.TimeSegment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MessageFormatService {

    public String prepareFullMessage(DaySchedule daySchedule) {
        int currentYear = LocalDate.now().getYear();
        LocalDate fullDate = daySchedule.getDate().atYear(currentYear);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale.forLanguageTag("uk"));
        String formattedDate = fullDate.format(dateFormatter);

        StringBuilder message = new StringBuilder();
        message.append("Графік відключення на ").append(formattedDate).append("\n");

        for (int hour = 0; hour < 24; hour++) {
            HalfHourStatus hourStatus = daySchedule.getHours().get(hour);
            String formattedHour = String.format("%02d:00", hour);
            message.append(formattedHour).append(" ").append(formatPowerState(hourStatus.getLeftHalf())).append("\n");
            if (hourStatus.getLeftHalf() != hourStatus.getRightHalf()) {
                String halfHour = String.format("%02d:30", hour);
                message.append(halfHour).append(" ").append(formatPowerState(hourStatus.getRightHalf())).append("\n");
            }
        }

        return message.toString();
    }

    public String prepareShortMessage(DaySchedule daySchedule) {
        MonthDay date = daySchedule.getDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale.forLanguageTag("uk"));
        String formattedDate = date.format(dateFormatter);

        StringBuilder message = new StringBuilder();
        message.append("📅 ").append(formattedDate).append("\n\n");

        List<TimeSegment> segments = findTimeSegments(daySchedule);

        boolean hasOutages = segments.stream().anyMatch(s ->
                s.getState() == PowerState.CONFIRMED_DISCONNECTION ||
                        s.getState() == PowerState.POSSIBLE_DISCONNECTION);

        if (!hasOutages) {
            message.append("✅ Весь день без відключень");
            return message.toString();
        }

        List<TimeSegment> confirmedOutages = segments.stream()
                .filter(s -> s.getState() == PowerState.CONFIRMED_DISCONNECTION)
                .toList();

        if (!confirmedOutages.isEmpty()) {
            message.append("❌ Світла немає:\n");
            for (TimeSegment segment : confirmedOutages) {
                message.append("   ").append(segment.getStartTime()).append("-").append(segment.getEndTime()).append("\n");
            }
            message.append("\n");
        }

        List<TimeSegment> possibleOutages = segments.stream()
                .filter(s -> s.getState() == PowerState.POSSIBLE_DISCONNECTION)
                .toList();

        if (!possibleOutages.isEmpty()) {
            message.append("⚠️ Можливе відключення:\n");
            for (TimeSegment segment : possibleOutages) {
                message.append("   ").append(segment.getStartTime()).append("-").append(segment.getEndTime()).append("\n");
            }
        }

        return message.toString();
    }

    private List<TimeSegment> findTimeSegments(DaySchedule daySchedule) {
        List<TimeSegment> segments = new ArrayList<>();
        TimeSegment currentSegment = null;

        for (int hour = 0; hour < 24; hour++) {
            HalfHourStatus hourStatus = daySchedule.getHours().get(hour);
            if (currentSegment == null || currentSegment.getState() != hourStatus.getLeftHalf()) {
                if (currentSegment != null) {
                    currentSegment.setEndTime(String.format("%02d:00", hour));
                }
                currentSegment = TimeSegment.builder()
                        .startTime(String.format("%02d:00", hour))
                        .state(hourStatus.getLeftHalf())
                        .build();
                segments.add(currentSegment);
            }

            if (hourStatus.getLeftHalf() != hourStatus.getRightHalf()) {
                currentSegment.setEndTime(String.format("%02d:30", hour));
                currentSegment = TimeSegment.builder()
                        .startTime(String.format("%02d:30", hour))
                        .state(hourStatus.getRightHalf())
                        .build();
                segments.add(currentSegment);
            } else if (hour == 23 && currentSegment != null) {
                currentSegment.setEndTime("00:00");
            }
        }

        return segments;
    }

    private static String formatPowerState(PowerState state) {
        switch (state) {
            case NO_DISCONNECTION:
                return "✅Світло є";
            case CONFIRMED_DISCONNECTION:
                return "❌Світла немає";
            case POSSIBLE_DISCONNECTION:
                return "⚠️ Можливе відключення";
            default:
                return "❓Невідомий статус";
        }
    }

}