package com.svitlobot;

import com.svitlobot.dto.DaySchedule;
import com.svitlobot.model.ScheduleState;
import com.svitlobot.service.MessageFormatService;
import com.svitlobot.service.VoeService;
import com.svitlobot.service.ScheduleStateService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ScheduleNotificationJob {

    private final TelegramBot telegramBot;
    private final VoeService voeService;
    private final ScheduleStateService scheduleStateService;
    private final MessageFormatService messageFormatter;

//    @Scheduled(fixedRate = 60 * 10 * 1000)
    public void sendDailyScheduleNotification() {
        DaySchedule todaySchedule = voeService.getTodaySchedule();
        ScheduleState lastState = scheduleStateService.getLastState();

        if (!isScheduleChanged(todaySchedule, lastState)) {
            return;
        }

        ScheduleState updatedState = scheduleStateService.updateState(todaySchedule);
        telegramBot.notifyAllSubscribers("Графік відключень оновлено\n\n" + updatedState.getShortState());
    }

    private boolean isScheduleChanged(DaySchedule todaySchedule, ScheduleState lastState) {
        if (lastState == null) {
            return true;
        }

        String shortMessage = messageFormatter.prepareShortMessage(todaySchedule);
        String fullMessage = messageFormatter.prepareFullMessage(todaySchedule);

        return !lastState.getShortState().equals(shortMessage)
                || !lastState.getFullState().equals(fullMessage);
    }

}