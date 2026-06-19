package com.example.kuwalog.entity.enums;

public enum Sex {

    MALE("オス"),
    FEMALE("メス"),
    UNKNOWN("不明");

    private final String label;

    Sex(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Sex fromLabel(String label) {
        for (Sex s : values()) {
            if (s.label.equals(label)) return s;
        }
        throw new IllegalArgumentException("不正な性別値: " + label);
    }
}
