package com.svitlobot;

import com.svitlobot.dto.DaySchedule;
import com.svitlobot.dto.HalfHourStatus;
import com.svitlobot.dto.TimeSegment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PowerScheduleMessageFormatter {

    public String prepareFullMessage(DaySchedule daySchedule) {
        // Get the current year to create a full date
        int currentYear = LocalDate.now().getYear();
        LocalDate fullDate = daySchedule.date.atYear(currentYear);

        // Format the date as day.month (e.g., 20.11)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale.forLanguageTag("uk"));
        String formattedDate = fullDate.format(dateFormatter);

        StringBuilder message = new StringBuilder();
        message.append("Графік відключення на ").append(formattedDate).append("\n");

        // Process each hour and its status
        for (int hour = 0; hour < 24; hour++) {
            HalfHourStatus hourStatus = daySchedule.hours.get(hour);

            // Format the hour (00:00, 01:00, etc.)
            String formattedHour = String.format("%02d:00", hour);
            message.append(formattedHour).append(" ").append(formatPowerState(hourStatus.leftHalf)).append("\n");

            // If left and right halves differ, add a half-hour entry
            if (hourStatus.leftHalf != hourStatus.rightHalf) {
                String halfHour = String.format("%02d:30", hour);
                message.append(halfHour).append(" ").append(formatPowerState(hourStatus.rightHalf)).append("\n");
            }
        }

        return message.toString();
    }

    /**
     * Prepares a shortened version of the schedule message showing only timeframes of outages
     */
    public String prepareShortMessage(DaySchedule daySchedule) {
        // Get the current year to create a full date
        int currentYear = LocalDate.now().getYear();
        LocalDate fullDate = daySchedule.date.atYear(currentYear);

        // Format the date as day.month (e.g., 20.11)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale.forLanguageTag("uk"));
        String formattedDate = fullDate.format(dateFormatter);

        StringBuilder message = new StringBuilder();
        message.append("📅 ").append(formattedDate).append("\n\n");

        // Find continuous segments of same state
        List<TimeSegment> segments = findTimeSegments(daySchedule);

        // No outages case
        boolean hasOutages = segments.stream().anyMatch(s ->
                s.getState() == PowerState.CONFIRMED_DISCONNECTION ||
                        s.getState() == PowerState.POSSIBLE_DISCONNECTION);

        if (!hasOutages) {
            message.append("✅ Весь день без відключень");
            return message.toString();
        }

        // Add confirmed outages
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

        // Add possible outages
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


    /**
     * Groups consecutive time periods with the same power state
     */
    private List<TimeSegment> findTimeSegments(DaySchedule daySchedule) {
        List<TimeSegment> segments = new ArrayList<>();
        TimeSegment currentSegment = null;

        for (int hour = 0; hour < 24; hour++) {
            HalfHourStatus hourStatus = daySchedule.hours.get(hour);

            // Process left half hour (XX:00 - XX:30)
            if (currentSegment == null || currentSegment.getState() != hourStatus.leftHalf) {
                if (currentSegment != null) {
                    // Set end time for previous segment
                    currentSegment.setEndTime(String.format("%02d:00", hour));
                }
                // Start a new segment
                currentSegment = TimeSegment.builder()
                        .startTime(String.format("%02d:00", hour))
                        .state(hourStatus.leftHalf)
                        .build();
                segments.add(currentSegment);
            }

            // Process right half hour (XX:30 - XX+1:00)
            if (hourStatus.leftHalf != hourStatus.rightHalf) {
                // Set end time for previous segment
                currentSegment.setEndTime(String.format("%02d:30", hour));
                // Start a new segment
                currentSegment = TimeSegment.builder()
                        .startTime(String.format("%02d:30", hour))
                        .state(hourStatus.rightHalf)
                        .build();
                segments.add(currentSegment);
            } else if (hour == 23 && currentSegment != null) {
                // Handle the last hour of the day
                currentSegment.setEndTime("00:00");
            }
        }

        return segments;
    }

    /**
     * Formats a power state with appropriate emoji and description
     */
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

    /**
     * Creates messages for multiple days
     * @param schedules List of day schedules
     * @return Array of formatted messages, one per day
     */
    public String[] prepareMessages(List<DaySchedule> schedules) {
        return schedules.stream()
                .map(this::prepareFullMessage)
                .toArray(String[]::new);
    }

    /**
     * Creates short messages for multiple days
     * @param schedules List of day schedules
     * @return Array of formatted short messages, one per day
     */
    public String[] prepareShortMessages(List<DaySchedule> schedules) {
        return schedules.stream()
                .map(this::prepareShortMessage)
                .toArray(String[]::new);
    }
}