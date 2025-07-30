package ch.uzh.ifi.imrg.patientapp.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "psychological_test_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "test_name"}))
public class PsychologicalTestAssignment {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "exercise_start")
    private Instant exerciseStart;

    @Column(name = "exercise_end")
    private Instant exerciseEnd;

    @Column(name = "is_paused")
    private Boolean isPaused;

    @Column(name = "do_every_n_days")
    private int doEveryNDays;

    @Column(name = "last_completed_at")
    private Instant lastCompletedAt;

    private Instant lastReminderSentAt;

}
