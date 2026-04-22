package com.example.sentimentbackend.repository;

import com.example.sentimentbackend.model.Feedback;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends CrudRepository<Feedback, Long> {

    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double findAverageRating();

    List<Feedback> findTop3ByCommentIsNotNullAndCommentNotOrderByCreatedAtDesc(String empty);
}

