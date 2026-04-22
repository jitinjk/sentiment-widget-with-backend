package com.example.sentimentbackend.dto;

import java.util.List;

public record SummaryResponse(
    long totalSubmissions,
    String averageRating,
    List<RecentComment> recentComments
) {}
