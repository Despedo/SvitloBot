package com.svitlobot;

import com.svitlobot.dto.DaySchedule;
import com.svitlobot.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final PowerScheduleService powerScheduleService;
    private final PowerScheduleMessageFormatter powerScheduleMessageFormatter;

    // Store subscribed users
    private final SubscriberService subscriberService;

    @Autowired
    public TelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            PowerScheduleService powerScheduleService,
            PowerScheduleMessageFormatter powerScheduleMessageFormatter,
            SubscriberService subscriberService) {
        super(botToken);
        this.botUsername = botUsername;
        this.powerScheduleService = powerScheduleService;
        this.powerScheduleMessageFormatter = powerScheduleMessageFormatter;
        this.subscriberService = subscriberService;
//        subscribedUsers.put(799021336L, true);
//        subscribedUsers.put(1824310068L, true); me
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void handleCommands(String command, long chatId) {
        if (command.startsWith("/start")) {
            sendWelcomeMessage(chatId);
        } else if (command.equalsIgnoreCase("/subscribe")) {
            subscribeUser(chatId);
        } else if (command.equalsIgnoreCase("/unsubscribe")) {
            unsubscribeUser(chatId);
        } else if (command.equalsIgnoreCase("/today_full")) {
            sendTodaySchedule(chatId);
        } else if (command.equalsIgnoreCase("/today")) {
            sendTodayShortSchedule(chatId);
        } else if (command.equalsIgnoreCase("/tomorrow_full")) {
            sendTomorrowSchedule(chatId);
        } else if (command.equalsIgnoreCase("/tomorrow")) {
            sendTomorrowShortSchedule(chatId);
        } else if (command.equalsIgnoreCase("/help")) {
            sendHelpMessage(chatId);
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        // Handle regular messages
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.toLowerCase().startsWith("привіт")) {
                sendWelcomeMessage(chatId);
            }
            if (messageText.startsWith("/")) {
                handleCommands(messageText, chatId);
            }
        }
        // Handle callback queries (inline keyboard button clicks)
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String callbackId = update.getCallbackQuery().getId();

            // Answer the callback query to remove the loading indicator
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackId);
            try {
                execute(answer);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            // Handle different button callbacks
            handleCommands(callbackData, chatId);
        }
    }


    private void sendWelcomeMessage(long chatId) {
        String text = "Вітаю! Я бот для моніторингу графіку відключення електроенергії за адресою пров.Івана Миколайчука 6.";

        // Create inline keyboard
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

        // Create rows of buttons
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // First row
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton subscribeButton = InlineKeyboardButton.builder().text("📝 Підписатись").callbackData("/subscribe").build();
        InlineKeyboardButton unsubscribeButton = InlineKeyboardButton.builder().text("❌ Відписатись").callbackData("/unsubscribe").build();
        row1.add(subscribeButton);
        row1.add(unsubscribeButton);
        rowsInline.add(row1);

        // Second row
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton todayShortButton = InlineKeyboardButton.builder().text("📅 Сьогодні").callbackData("/today").build();
        row2.add(todayShortButton);
        InlineKeyboardButton tomorrowShortButton = InlineKeyboardButton.builder().text("📆 Завтра").callbackData("/tomorrow").build();
        row2.add(tomorrowShortButton);
        rowsInline.add(row2);

        // Third row
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton todayButton = InlineKeyboardButton.builder().text("📅 Сьогодні (повний графік)").callbackData("/today_full").build();
        row3.add(todayButton);
        rowsInline.add(row3);

        // Fourth row
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton tomorrowButton = InlineKeyboardButton.builder().text("📆 Завтра (повний графік)").callbackData("/tomorrow_full").build();
        row4.add(tomorrowButton);
        rowsInline.add(row4);

        // Fifth row
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        InlineKeyboardButton helpButton = InlineKeyboardButton.builder().text("❓ Допомога").callbackData("/help").build();
        row5.add(helpButton);
        rowsInline.add(row5);

        // Add rows to keyboard
        inlineKeyboard.setKeyboard(rowsInline);

        // Create message with keyboard
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void subscribeUser(long chatId) {
        subscriberService.subscribe(chatId);
        sendMessage(chatId, "Ви успішно підписались на щоденні оновлення графіку відключення!");
    }

    private void unsubscribeUser(long chatId) {
        subscriberService.unsubscribe(chatId);
        sendMessage(chatId, "Ви відписались від оновлень.");
    }

    private void sendTodaySchedule(long chatId) {
        try {
            DaySchedule todaySchedule = powerScheduleService.getTodaySchedule();
            if (todaySchedule != null) {
                String formattedMessage = powerScheduleMessageFormatter.prepareFullMessage(todaySchedule);
                sendMessage(chatId, formattedMessage);
            } else {
                sendMessage(chatId, "Немає інформації про графік відключень на сьогодні.");
            }
        } catch (Exception e) {
            sendMessage(chatId, "Помилка при отриманні графіку. Спробуйте пізніше.");
        }
    }

    private void sendTodayShortSchedule(long chatId) {
        try {
            DaySchedule todaySchedule = powerScheduleService.getTodaySchedule();
            if (todaySchedule != null) {
                String formattedMessage = powerScheduleMessageFormatter.prepareShortMessage(todaySchedule);
                sendMessage(chatId, formattedMessage);
            } else {
                sendMessage(chatId, "Немає інформації про графік відключень на сьогодні.");
            }
        } catch (Exception e) {
            sendMessage(chatId, "Помилка при отриманні графіку. Спробуйте пізніше.");
        }
    }


    private void sendTomorrowSchedule(long chatId) {
        try {
            DaySchedule tomorrowSchedule = powerScheduleService.getTomorrowSchedule();
            if (tomorrowSchedule != null) {
                String formattedMessage = powerScheduleMessageFormatter.prepareFullMessage(tomorrowSchedule);
                sendMessage(chatId, formattedMessage);
            } else {
                sendMessage(chatId, "Немає інформації про графік відключень на завтра.");
            }
        } catch (Exception e) {
            sendMessage(chatId, "Помилка при отриманні графіку. Спробуйте пізніше.");
        }
    }

    private void sendTomorrowShortSchedule(long chatId) {
        try {
            DaySchedule todaySchedule = powerScheduleService.getTomorrowSchedule();
            if (todaySchedule != null) {
                String formattedMessage = powerScheduleMessageFormatter.prepareShortMessage(todaySchedule);
                sendMessage(chatId, formattedMessage);
            } else {
                sendMessage(chatId, "Немає інформації про графік відключень на завтра.");
            }
        } catch (Exception e) {
            sendMessage(chatId, "Помилка при отриманні графіку. Спробуйте пізніше.");
        }
    }


//    private void sendHelpMessage(long chatId) {
//        String helpText = "Цей бот надає інформацію про графік відключення електроенергії.\n\n" +
//                "Доступні команди:\n" +
//                "/subscribe - Підписатись на щоденні оновлення\n" +
//                "/unsubscribe - Відписатись від оновлень\n" +
//                "/today_full - Отримати повний графік на сьогодні\n" +
//                "/today - Отримати короткий графік на сьогодні\n" +
//                "/tomorrow_full - Отримати графік на завтра\n" +
//                "/tomorrow - Отримати короткий графік на завтра\n" +
//                "/help - Отримати допомогу";
//
//        sendMessage(chatId, helpText);
//    }

    private void sendHelpMessage(long chatId) {
        String helpText = "Допомога платна, ціна 1 цьомчик!";

        sendMessage(chatId, helpText);
    }


    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void notifyAllSubscribers(String message) {
        subscriberService.getAllActiveSubscribers().forEach(subscriber -> {
            sendMessage(subscriber.getChatId(), message);
        });
    }

}