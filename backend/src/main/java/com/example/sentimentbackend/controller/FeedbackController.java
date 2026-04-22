package com.example.sentimentbackend.controller;

import com.wogaa.sentiment.dto.FeedbackRequest;
import com.wogaa.sentiment.dto.SummaryResponse;
import com.wogaa.sentiment.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@Tag(name = "Feedback", description = "Sentiment feedback endpoints")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a feedback rating with an optional comment")
    public void submit(@Valid @RequestBody FeedbackRequest request) {
        service.submit(request);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get total submissions, average rating, and 3 most recent comments")
    public SummaryResponse getSummary() {
        return service.getSummary();
    }
}
