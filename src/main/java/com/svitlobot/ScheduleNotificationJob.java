package com.svitlobot;

import com.svitlobot.dto.DaySchedule;
import com.svitlobot.repository.SubscriberRepository;
import com.svitlobot.service.SubscriberService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ScheduleNotificationJob {

    private final PowerScheduleService scheduleService;
    private final PowerScheduleMessageFormatter powerScheduleMessageFormatter;
    private final TelegramBot telegramBot;
    private final SubscriberRepository subscriberRepository;
    private final SubscriberService subscriberService;

    @Scheduled(fixedRate = 60*10*1000)
//    @Scheduled(fixedRate = 1000)
    public void sendDailyScheduleNotification() {

    }
}