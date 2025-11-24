package com.svitlobot;

import com.svitlobot.dto.DaySchedule;
import com.svitlobot.dto.HalfHourStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PowerScheduleParser {

    public List<DaySchedule> parse(String body) {
        List<DaySchedule> result = new ArrayList<>();
        Document doc = Jsoup.parse(body);

        Elements dayHeaders = doc.select(".disconnection-detailed-table-cell.legend.day_col");
        Elements allCells = doc.select(".disconnection-detailed-table-cell.cell");

        int cellIndex = 0;
        for (Element dayHeader : dayHeaders) {
            String dayText = dayHeader.text(); // напр. "чт 20.11"
            MonthDay date = parseDate(dayText);

            DaySchedule daySchedule = new DaySchedule();
            daySchedule.date = date;
            boolean hasSchedule = false;  // Flag to track if day has any disconnection

            for (int h = 0; h < 24; h++) {
                Element cell = allCells.get(cellIndex++);
                HalfHourStatus status = new HalfHourStatus();

                // Перевіряємо, чи є клас "confirm_1 full_hour" у батьківського елемента
                if (cell.hasClass("confirm_1") && cell.hasClass("full_hour")) {
                    // Якщо так, встановлюємо CONFIRMED_DISCONNECTION для обох половин
                    status.leftHalf = PowerState.CONFIRMED_DISCONNECTION;
                    status.rightHalf = PowerState.CONFIRMED_DISCONNECTION;
                    hasSchedule = true;
                } else if (cell.hasClass("confirm_0") && cell.hasClass("full_hour")) {
                    // Якщо є confirm_0 full_hour, встановлюємо POSSIBLE_DISCONNECTION для обох половин
                    status.leftHalf = PowerState.POSSIBLE_DISCONNECTION;
                    status.rightHalf = PowerState.POSSIBLE_DISCONNECTION;
                    hasSchedule = true;
                } else {
                    // Обробляємо кожну половину години окремо
                    Elements halves = cell.select(".half");
                    status.leftHalf = parseState(halves.get(0));
                    status.rightHalf = parseState(halves.get(1));
                }

                // Check if either half has a disconnection (confirmed or possible)
                if (status.leftHalf == PowerState.CONFIRMED_DISCONNECTION ||
                        status.leftHalf == PowerState.POSSIBLE_DISCONNECTION ||
                        status.rightHalf == PowerState.CONFIRMED_DISCONNECTION ||
                        status.rightHalf == PowerState.POSSIBLE_DISCONNECTION) {
                    hasSchedule = true;
                }

                daySchedule.hours.add(status);
            }
            // Only add days that have a schedule
            if (hasSchedule) {
                result.add(daySchedule);
            }
        }
        return result;
    }

    private static MonthDay parseDate(String dayText) {
        // dayText: "чт 20.11"
        String[] parts = dayText.split(" ");
        String datePart = parts[1];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M");
        MonthDay monthDay = MonthDay.parse(datePart, fmt);
        // Додаємо рік (наприклад, поточний)
        return monthDay;
    }

    private static PowerState parseState(Element half) {
        String classAttr = half.className();
        if (classAttr.contains("no_disconnection")) return PowerState.NO_DISCONNECTION;
        if (classAttr.contains("has_disconnection") && classAttr.contains("confirm_1"))
            return PowerState.CONFIRMED_DISCONNECTION;
        if (classAttr.contains("has_disconnection") && classAttr.contains("confirm_0"))
            return PowerState.POSSIBLE_DISCONNECTION;
        return PowerState.NO_DISCONNECTION; // Замість null, повертаємо NO_DISCONNECTION за замовчуванням
    }
}