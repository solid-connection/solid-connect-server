package com.example.solidconnection.mentor.dto;

import java.util.List;

public record MentorPreviewsResponse(
        List<MentorPreviewResponse> content,
        int nextPageNumber
) {
}
