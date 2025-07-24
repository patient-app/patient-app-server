package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import ch.uzh.ifi.imrg.patientapp.constant.ExerciseComponentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "exercise_components")
public class ExerciseComponent {
    @Id
    @Column(unique = true)
    private String id;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column
    private ExerciseComponentType exerciseComponentType;

    @Lob
    @Column
    private String userInput;

    @Lob
    @Column
    private String exerciseComponentDescription;

    private String fileName;

    private String fileType;

    @Lob
    private byte[] fileData;

    @Column()
    private Integer orderNumber;

    private String youtubeUrl;

    @ManyToOne
    @JoinColumn(name = "exercise_id", referencedColumnName = "id")
    private Exercise exercise;
}
