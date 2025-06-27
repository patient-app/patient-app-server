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
    private String id = UUID.randomUUID().toString();

    @Lob
    private byte[] data;
}
