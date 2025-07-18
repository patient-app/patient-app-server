package ch.uzh.ifi.imrg.patientapp.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@Entity
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("JOURNAL")
@Table(name = "journal_entry_conversations")
public class JournalEntryConversation extends Conversation {

}
