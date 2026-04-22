package com.example.sentimentbackend.service;

import com.example.sentimentbackend.dto.FeedbackRequest;
import com.example.sentimentbackend.dto.RecentComment;
import com.example.sentimentbackend.dto.SummaryResponse;
import com.example.sentimentbackend.model.Feedback;
import com.example.sentimentbackend.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository repository;

    public FeedbackService(FeedbackRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void submit(FeedbackRequest request) {
        String comment = (request.comment() != null) ? request.comment().trim() : null;
        repository.save(new Feedback(request.rating(), comment));
    }

    public SummaryResponse getSummary() {
        long total = repository.count();
        if (total == 0) {
            return new SummaryResponse(0, null, List.of());
        }

        Double avg = repository.findAverageRating();
        String averageRating = avg != null ? String.format("%.1f", avg) : null;

        List<RecentComment> recentComments = repository
                .findTop3ByCommentIsNotNullAndCommentNotOrderByCreatedAtDesc("")
                .stream()
                .map(f -> new RecentComment(f.getComment(), f.getRating()))
                .toList();

        return new SummaryResponse(total, averageRating, recentComments);
    }
}
