package com.example.kuwalog.entity.enums;

import java.util.List;

public enum Species {
    OOKUWA("オオクワガタ"),
    HIRATA("ヒラタクワガタ"),
    NOKOGIRI("ノコギリクワガタ"),
    MIYAMA("ミヤマクワガタ"),
    KOKUWAGATA("コクワガタ"),
    KABUTOMUSHI("カブトムシ");

    private final String label;

    Species(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static List<Species> rankingTargets() {
        return List.of(values());
    }
}
