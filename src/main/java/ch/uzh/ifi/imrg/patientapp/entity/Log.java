package ch.uzh.ifi.imrg.patientapp.entity;

import ch.uzh.ifi.imrg.patientapp.constant.LogTypes;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Log {
    @Id
    private String id = UUID.randomUUID().toString();
    private String patientId;

    @Enumerated(EnumType.STRING)
    private LogTypes logType;

    private Instant timestamp;
    private String uniqueIdentifier;
}
