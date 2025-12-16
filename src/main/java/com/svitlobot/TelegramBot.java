package com.svitlobot;

import com.svitlobot.dto.DaySchedule;
import com.svitlobot.service.MessageFormatService;
import com.svitlobot.service.SubscriberService;
import com.svitlobot.service.VoeService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final VoeService voeService;
    private final MessageFormatService messageFormatService;
    private final SubscriberService subscriberService;

    @Autowired
    public TelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            VoeService voeService,
            MessageFormatService messageFormatService,
            SubscriberService subscriberService) {
        super(botToken);
        this.botUsername = botUsername;
        this.voeService = voeService;
        this.messageFormatService = messageFormatService;
        this.subscriberService = subscriberService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void handleCommands(String command, long chatId) {
        sendMessage(chatId, "Сервіс тимчасово не працює");
        if (command.startsWith("/start")) {
            sendWelcomeMessage(chatId);
        } else if (command.equalsIgnoreCase("/subscribe")) {
            subscribeUser(chatId);
        } else if (command.equalsIgnoreCase("/unsubscribe")) {
            unsubscribeUser(chatId);
        } else if (command.equalsIgnoreCase("/today_full")) {
            sendTodaySchedule(chatId);
        }
//        else if (command.equalsIgnoreCase("/today")) {
//            sendTodayShortSchedule(chatId);
//        } else if (command.equalsIgnoreCase("/tomorrow_full")) {
//            sendTomorrowSchedule(chatId);
//        } else if (command.equalsIgnoreCase("/tomorrow")) {
//            sendTomorrowShortSchedule(chatId);
//        } else if (command.equalsIgnoreCase("/help")) {
//            sendHelpMessage(chatId);
//        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.toLowerCase().startsWith("привіт")) {
                sendWelcomeMessage(chatId);
            }
            if (messageText.startsWith("/")) {
                handleCommands(messageText, chatId);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackId = update.getCallbackQuery().getId();

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackId);
            try {
                execute(answer);
            } catch (TelegramApiException e) {
                log.error("Error while answering callback query: {}", e.getMessage());
            }

            handleCommands(callbackData, chatId);
        }
    }


    private void sendWelcomeMessage(long chatId) {
        String text = "Вітаю! Я бот для моніторингу графіку відключення електроенергії за адресою пров.Івана Миколайчука 6.";

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton subscribeButton = InlineKeyboardButton.builder().text("📝 Підписатись").callbackData("/subscribe").build();
        InlineKeyboardButton unsubscribeButton = InlineKeyboardButton.builder().text("❌ Відписатись").callbackData("/unsubscribe").build();
        row1.add(subscribeButton);
        row1.add(unsubscribeButton);
        rowsInline.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton todayShortButton = InlineKeyboardButton.builder().text("📅 Сьогодні").callbackData("/today").build();
        row2.add(todayShortButton);
        InlineKeyboardButton tomorrowShortButton = InlineKeyboardButton.builder().text("📆 Завтра").callbackData("/tomorrow").build();
        row2.add(tomorrowShortButton);
        rowsInline.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton todayButton = InlineKeyboardButton.builder().text("📅 Сьогодні (повний графік)").callbackData("/today_full").build();
        row3.add(todayButton);
        rowsInline.add(row3);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton tomorrowButton = InlineKeyboardButton.builder().text("📆 Завтра (повний графік)").callbackData("/tomorrow_full").build();
        row4.add(tomorrowButton);
        rowsInline.add(row4);

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        InlineKeyboardButton helpButton = InlineKeyboardButton.builder().text("❓ Допомога").callbackData("/help").build();
        row5.add(helpButton);
        rowsInline.add(row5);

        inlineKeyboard.setKeyboard(rowsInline);

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
            DaySchedule todaySchedule = voeService.getTodaySchedule();
            if (todaySchedule != null) {
                String formattedMessage = messageFormatService.prepareFullMessage(todaySchedule);
                sendMessage(chatId, formattedMessage);
            } else {
                sendMessage(chatId, "Немає інформації про графік відключень на сьогодні.");
            }
        } catch (Exception e) {
            sendMessage(chatId, "Помилка при отриманні графіку. Спробуйте пізніше.");
        }
    }

    public void sendTodayShortSchedule(long chatId) {
        try {
            DaySchedule todaySchedule = voeService.getTodaySchedule();
            if (todaySchedule != null) {
                String formattedMessage = messageFormatService.prepareShortMessage(todaySchedule);
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
            DaySchedule tomorrowSchedule = voeService.getTomorrowSchedule();
            if (tomorrowSchedule != null) {
                String formattedMessage = messageFormatService.prepareFullMessage(tomorrowSchedule);
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
            DaySchedule todaySchedule = voeService.getTomorrowSchedule();
            if (todaySchedule != null) {
                String formattedMessage = messageFormatService.prepareShortMessage(todaySchedule);
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

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Unexpected error while sending message to user {}: {}", chatId, e.getMessage());
        }
    }

    public void notifyAllSubscribers(String message) {
        subscriberService.getAllActiveSubscribers().forEach(subscriber -> {
            sendMessage(subscriber.getChatId(), message);
        });
    }

}