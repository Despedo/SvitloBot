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

    @Transactional
    public void subscribe(Long chatId) {
        log.info("Subscribing user: chatId={}", chatId);
        Optional<Subscriber> existingSubscriber = subscriberRepository.findById(chatId);

        Subscriber subscriber;
        if (existingSubscriber.isPresent()) {
            subscriber = existingSubscriber.get();
            subscriber.setActive(true);

            log.info("Updated existing subscriber: {}", subscriber);
        } else {
            subscriber = new Subscriber();
            subscriber.setChatId(chatId);
            subscriber.setSubscribedAt(LocalDateTime.now());
            subscriber.setActive(true);

            log.info("Created new subscriber: {}", subscriber);
        }
        subscriberRepository.save(subscriber);
    }

    @Transactional
    public void unsubscribe(Long chatId) {
        log.info("Unsubscribing user: chatId={}", chatId);

        subscriberRepository.findById(chatId).ifPresent(subscriber -> {
            subscriber.setActive(false);
            subscriberRepository.save(subscriber);
            log.info("Subscriber marked as inactive: {}", subscriber);
        });
    }

    @Transactional(readOnly = true)
    public List<Subscriber> getAllActiveSubscribers() {
        return subscriberRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long getActiveSubscribersCount() {
        return subscriberRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public boolean isSubscribed(Long chatId) {
        return subscriberRepository.findById(chatId)
                .map(Subscriber::isActive)
                .orElse(false);
    }

    @Transactional
    public void deleteSubscriber(Long chatId) {
        log.info("Deleting subscriber: chatId={}", chatId);
        subscriberRepository.deleteById(chatId);
    }


    @Transactional(readOnly = true)
    public List<Subscriber> getAllSubscribersSortedByDate() {
        List<Subscriber> subscribers = subscriberRepository.findAllActiveOrderBySubscribedAtDesc();
        return subscribers;
    }
}