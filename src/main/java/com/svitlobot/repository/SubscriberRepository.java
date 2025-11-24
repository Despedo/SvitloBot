package com.svitlobot.repository;

import com.svitlobot.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    List<Subscriber> findByActiveTrue();

    long countByActiveTrue();

    @Query("SELECT s FROM Subscriber s WHERE s.active = true ORDER BY s.subscribedAt DESC")
    List<Subscriber> findAllActiveOrderBySubscribedAtDesc();
}