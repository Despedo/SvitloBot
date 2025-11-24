package com.svitlobot.repository;

import com.svitlobot.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    /**
     * Знаходить всіх активних підписників.
     */
    List<Subscriber> findByActiveTrue();

    /**
     * Підраховує кількість активних підписників.
     */
    long countByActiveTrue();

    /**
     * Перевіряє, чи є підписник з вказаним chatId активним.
     */
    boolean existsByChatIdAndActiveTrue(Long chatId);

    /**
     * Отримує список активних підписників, які підписались після вказаної дати.
     */
    @Query("SELECT s FROM Subscriber s WHERE s.active = true ORDER BY s.subscribedAt DESC")
    List<Subscriber> findAllActiveOrderBySubscribedAtDesc();
}