package com.svitlobot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    /**
     * ID чату Telegram, який використовується як унікальний ідентифікатор підписника.
     * Для приватних чатів це унікальний ідентифікатор користувача.
     */
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    /**
     * Дата та час, коли користувач підписався.
     */
    @Column(name = "subscribed_at")
    private LocalDateTime subscribedAt;

    /**
     * Статус підписки.
     * true - активна підписка, false - користувач відписався.
     * Зберігаємо запис у БД навіть після відписки для можливості аналітики.
     */
    @Column(name = "active")
    private boolean active = true;

}