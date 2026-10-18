package com.chatkeluhan.demo.dto;

public class FeedbackRequest extends com.chatkeluhan.demo.dto.ticket.FeedbackRequest {

    public FeedbackRequest() {
        super();
    }

    public FeedbackRequest(Integer rating, String feedback) {
        super(rating, feedback);
    }
}
