package ch.uzh.ifi.imrg.patientapp.entity.Document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "documents", uniqueConstraints = @UniqueConstraint(columnNames = "sha256"))
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @Column
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String contentType;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    @Column(nullable = false, unique = true, length = 64)
    private String sha256; // hex-encoded 32-byte hash

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant uploadedAt;

}