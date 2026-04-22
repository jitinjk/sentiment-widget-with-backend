package com.example.sentimentbackend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

    public Feedback() {}

    public Feedback(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId()          { return id; }
    public int getRating()       { return rating; }
    public String getComment()   { return comment; }
    public Instant getCreatedAt(){ return createdAt; }
}
