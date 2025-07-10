package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Setter
@Entity
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("GENERAL")
@Table(name = "general_conversations")
public class GeneralConversation extends Conversation {

    @Column(name = "updated_last")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "share_with_coach")
    private Boolean shareWithCoach = true;

    @Column(name = "share_with_ai")
    private Boolean shareWithAi = true;

}
