package dev.ronaldnguyen.portfolio.dto;

/** Body of POST /api/chat. */
public class ChatRequest {

    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
