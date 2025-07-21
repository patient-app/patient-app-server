package ch.uzh.ifi.imrg.patientapp.entity.PsychologicalTest;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@Entity
public class PsychologicalTestQuestions {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psychological_test_id", referencedColumnName = "id", nullable = false)
    private PsychologicalTest psychologicalTest;

    String question;
    int score;
}
