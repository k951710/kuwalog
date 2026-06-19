package com.example.kuwalog.entity.enums;

public enum ReviewType {

    NORMAL("通常評価"),
    FOLLOW_UP("後追い評価");

    private final String label;

    ReviewType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
