package com.svitlobot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "month_day")
    private String monthDay;

    @Column(name = "short_state")
    private String shortState;

    @Column(name = "full_state")
    private String fullState;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
