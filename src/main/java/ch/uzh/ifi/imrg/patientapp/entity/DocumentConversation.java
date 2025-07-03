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
@DiscriminatorValue("DOCUMENT")
@Table(name = "document_conversations")
public class DocumentConversation extends Conversation {

}
