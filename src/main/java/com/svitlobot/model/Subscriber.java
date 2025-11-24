package com.svitlobot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "subscribed_at")
    private LocalDateTime subscribedAt;

    @Column(name = "active")
    private boolean active = true;

}