package com.svitlobot.repository;

import com.svitlobot.model.ScheduleState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleStateRepository extends JpaRepository<ScheduleState, Long> {

    ScheduleState findFirstByOrderByUpdatedAtDesc();

}