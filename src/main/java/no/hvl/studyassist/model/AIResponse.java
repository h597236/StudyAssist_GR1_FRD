package no.hvl.studyassist.model;

public class AIResponse {
    private String explanation;
    private String follow_up_question;

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getFollow_up_question() {
        return follow_up_question;
    }

    public void setFollow_up_question(String follow_up_question) {
        this.follow_up_question = follow_up_question;
    }
}