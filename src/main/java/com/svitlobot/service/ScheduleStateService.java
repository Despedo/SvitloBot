package com.svitlobot.service;

import com.svitlobot.dto.DaySchedule;
import com.svitlobot.model.ScheduleState;
import com.svitlobot.repository.ScheduleStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleStateService {

    private final ScheduleStateRepository scheduleStateRepository;
    private final MessageFormatService messageFormatter;

    public ScheduleState getLastState() {
        return scheduleStateRepository.findFirstByOrderByUpdatedAtDesc();
    }

    public ScheduleState updateState(DaySchedule todaySchedule) {
        MonthDay monthDay = todaySchedule.getDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale.forLanguageTag("uk"));
        String date = monthDay.format(dateFormatter);
        String shortMessage = messageFormatter.prepareShortMessage(todaySchedule);
        String fullMessage = messageFormatter.prepareFullMessage(todaySchedule);

        ScheduleState newState = new ScheduleState();
        newState.setMonthDay(date);
        newState.setShortState(shortMessage);
        newState.setFullState(fullMessage);
        return scheduleStateRepository.save(newState);
    }

}
