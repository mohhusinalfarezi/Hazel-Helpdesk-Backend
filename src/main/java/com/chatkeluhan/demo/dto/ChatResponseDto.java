package com.chatkeluhan.demo.dto;

public class ChatResponseDto {
    private String reply;
    private boolean shouldEscalate;
    private String suggestedCategory;
    private String suggestedSummary;

    public ChatResponseDto() {}

    public ChatResponseDto(String reply, boolean shouldEscalate, String suggestedCategory, String suggestedSummary) {
        this.reply = reply;
        this.shouldEscalate = shouldEscalate;
        this.suggestedCategory = suggestedCategory;
        this.suggestedSummary = suggestedSummary;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isShouldEscalate() {
        return shouldEscalate;
    }

    public void setShouldEscalate(boolean shouldEscalate) {
        this.shouldEscalate = shouldEscalate;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public String getSuggestedSummary() {
        return suggestedSummary;
    }

    public void setSuggestedSummary(String suggestedSummary) {
        this.suggestedSummary = suggestedSummary;
    }
}
