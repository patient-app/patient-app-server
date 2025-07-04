package ch.uzh.ifi.imrg.patientapp.entity;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@Entity
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("EXERCISE")
@Table(name = "exercise_conversations")
public class ExerciseConversation extends Conversation{

}
