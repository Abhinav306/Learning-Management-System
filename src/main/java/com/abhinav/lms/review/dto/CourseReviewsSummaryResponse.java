package com.abhinav.lms.review.dto;

import com.abhinav.lms.common.dto.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseReviewsSummaryResponse {

    private Double averageRating;
    private long totalReviews;
    private PagedResponse<ReviewResponse> reviews;
}
