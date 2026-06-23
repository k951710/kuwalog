package com.example.kuwalog.entity.enums;

public enum Classification {
    KUWAGATA("クワガタ"),
    KABUTOMUSHI("カブトムシ");

    private final String label;

    Classification(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    public static Classification fromLabel(String label) {
        for (Classification c : values()) {
            if (c.label.equals(label)) return c;
        }
        throw new IllegalArgumentException("Unknown classification label: " + label);
    }
}
