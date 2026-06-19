package com.example.kuwalog.entity.enums;

public enum Stage {

    EGG("卵"),
    LARVA("幼虫"),
    PUPA("蛹"),
    ADULT("成虫");

    private final String label;

    Stage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Stage fromLabel(String label) {
        for (Stage s : values()) {
            if (s.label.equals(label)) return s;
        }
        throw new IllegalArgumentException("不正なステージ値: " + label);
    }
}
