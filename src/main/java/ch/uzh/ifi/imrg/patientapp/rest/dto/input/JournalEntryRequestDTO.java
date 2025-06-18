package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JournalEntryRequestDTO {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @NotBlank(message = "Content must not be blank")
    private String content;

    private Set<@NotBlank(message = "Tag must not be blank") String> tags;

    private boolean sharedWithTherapist;

    private boolean aiAccessAllowed;

}
