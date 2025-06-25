package ch.uzh.ifi.imrg.patientapp.entity.Exercise;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class StoredExerciseFile {

    @Id
    @Column(unique = true)
    private String id = UUID.randomUUID().toString();

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private byte[] data;
}
