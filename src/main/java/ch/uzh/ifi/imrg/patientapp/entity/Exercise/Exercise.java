package ch.uzh.ifi.imrg.patientapp.entity.Exercise;


import ch.uzh.ifi.imrg.patientapp.entity.ExerciseConversation;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Exercise implements Serializable {

    @Id
    @Column(unique = true)
    private String id;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "exercise_start")
    private Instant exerciseStart;

    @Column(name = "exercise_end")
    private Instant exerciseEnd;

    @Column()
    private Boolean isPaused;
    private int doEveryNDays;

    private String exerciseTitle;
    private String exerciseDescription;
    //used in teh system prompt
    private String exerciseExplanation;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseComponent> exerciseComponents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List <ExerciseCompletionInformation> exerciseCompletionInformation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "exercise_conversation_id", referencedColumnName = "id")
    private ExerciseConversation exerciseConversation;


}
