package com.wogaa.sentiment.service;

import com.wogaa.sentiment.dto.FeedbackRequest;
import com.wogaa.sentiment.dto.RecentComment;
import com.wogaa.sentiment.dto.SummaryResponse;
import com.wogaa.sentiment.model.Feedback;
import com.wogaa.sentiment.repository.FeedbackRepository;
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
