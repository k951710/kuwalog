package com.example.kuwalog.entity.enums;

import java.util.List;

public enum Species {
    OOKUWA("オオクワガタ", "国内", "クワガタ"),
    HIRATA("ヒラタクワガタ", "国内", "クワガタ"),
    NOKOGIRI("ノコギリクワガタ", "国内", "クワガタ"),
    MIYAMA("ミヤマクワガタ", "国内", "クワガタ"),
    KOKUWAGATA("コクワガタ", "国内", "クワガタ"),
    KABUTOMUSHI("カブトムシ", "国内", "カブトムシ"),
    HERCULES("ヘラクレスオオカブト", "海外", "カブトムシ"),
    CAUCASUS("コーカサスオオカブト", "海外", "カブトムシ"),
    ACTAEON("アクティオンゾウカブト", "海外", "カブトムシ"),
    PALAWAN("パラワンオオヒラタ", "海外", "クワガタ"),
    SUMATRA("スマトラオオヒラタ", "海外", "クワガタ"),
    TARANDUS("タランドゥスオオツヤクワガタ", "海外", "クワガタ");

    private final String label;
    private final String origin;
    private final String category;

    Species(String label, String origin, String category) {
        this.label = label;
        this.origin = origin;
        this.category = category;
    }

    public String getLabel() { return label; }
    public String getOrigin() { return origin; }
    public String getCategory() { return category; }

    public static List<Species> rankingTargets() {
        return List.of(values());
    }
}
