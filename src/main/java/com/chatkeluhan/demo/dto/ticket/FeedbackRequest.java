package com.chatkeluhan.demo.dto.ticket;

public class FeedbackRequest {

    private Integer rating;
    private String feedback;

    public FeedbackRequest() {}

    public FeedbackRequest(Integer rating, String feedback) {
        this.rating = rating;
        this.feedback = feedback;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
