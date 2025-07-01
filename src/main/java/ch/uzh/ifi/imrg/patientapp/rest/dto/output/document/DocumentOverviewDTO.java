package ch.uzh.ifi.imrg.patientapp.rest.dto.output.document;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentOverviewDTO {
    private String id;
    private String filename;
    private String contentType;
    private Instant uploadedAt;
}