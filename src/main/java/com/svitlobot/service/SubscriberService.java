package com.svitlobot.service;

import com.svitlobot.model.Subscriber;
import com.svitlobot.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;

    /**
     * Підписує користувача або оновлює його дані, якщо він уже підписаний.
     *
     * @param chatId ID чату Telegram
     * @return Оновлений об'єкт підписника
     */
    @Transactional
    public Subscriber subscribe(Long chatId) {
        log.info("Subscribing user: chatId={}", chatId);

        Optional<Subscriber> existingSubscriber = subscriberRepository.findById(chatId);

        if (existingSubscriber.isPresent()) {
            // Оновлюємо існуючого підписника
            Subscriber subscriber = existingSubscriber.get();
            subscriber.setActive(true);

            log.info("Updated existing subscriber: {}", subscriber);
            return subscriberRepository.save(subscriber);
        } else {
            // Створюємо нового підписника
            Subscriber subscriber = new Subscriber();
            subscriber.setChatId(chatId);
            subscriber.setSubscribedAt(LocalDateTime.now());
            subscriber.setActive(true);

            log.info("Created new subscriber: {}", subscriber);
            return subscriberRepository.save(subscriber);
        }
    }

    /**
     * Відписує користувача (позначає його як неактивного).
     *
     * @param chatId ID чату Telegram
     */
    @Transactional
    public void unsubscribe(Long chatId) {
        log.info("Unsubscribing user: chatId={}", chatId);

        subscriberRepository.findById(chatId).ifPresent(subscriber -> {
            subscriber.setActive(false);
            subscriberRepository.save(subscriber);
            log.info("Subscriber marked as inactive: {}", subscriber);
        });
    }

    /**
     * Отримує список всіх активних підписників.
     *
     * @return Список активних підписників
     */
    @Transactional(readOnly = true)
    public List<Subscriber> getAllActiveSubscribers() {
        List<Subscriber> subscribers = subscriberRepository.findByActiveTrue();
        log.info("Found {} active subscribers", subscribers.size());
        return subscribers;
    }

    /**
     * Отримує кількість активних підписників.
     *
     * @return Кількість активних підписників
     */
    @Transactional(readOnly = true)
    public long getActiveSubscribersCount() {
        long count = subscriberRepository.countByActiveTrue();
        log.info("Active subscribers count: {}", count);
        return count;
    }

    /**
     * Перевіряє, чи підписаний користувач.
     *
     * @param chatId ID чату Telegram
     * @return true, якщо користувач підписаний, інакше false
     */
    @Transactional(readOnly = true)
    public boolean isSubscribed(Long chatId) {
        return subscriberRepository.findById(chatId)
                .map(Subscriber::isActive)
                .orElse(false);
    }

    /**
     * Видаляє підписника з бази даних (повне видалення, а не просто позначення як неактивного).
     * Використовувати обережно, переважно для адміністративних цілей.
     *
     * @param chatId ID чату Telegram
     */
    @Transactional
    public void deleteSubscriber(Long chatId) {
        log.info("Deleting subscriber: chatId={}", chatId);
        subscriberRepository.deleteById(chatId);
    }

    /**
     * Отримує список всіх підписників, відсортований за датою підписки (від найновіших).
     *
     * @return Список підписників
     */
    @Transactional(readOnly = true)
    public List<Subscriber> getAllSubscribersSortedByDate() {
        List<Subscriber> subscribers = subscriberRepository.findAllActiveOrderBySubscribedAtDesc();
        log.info("Found {} subscribers sorted by date", subscribers.size());
        return subscribers;
    }
}