package ch.uzh.ifi.imrg.patientapp.rest.dto.output.document;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentDownloadDTO {
    private final String filename;
    private final String contentType;
    private final byte[] data;
}
