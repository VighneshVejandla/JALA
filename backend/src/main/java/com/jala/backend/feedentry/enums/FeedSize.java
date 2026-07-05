package com.jala.backend.feedentry.enums;

public enum FeedSize {

    ONE("1"),
    TWO("2"),
    TWO_S("2S"),

    THREE("3"),
    THREE_S("3S"),

    FOUR("4"),
    FOUR_S("4S"),

    FIVE("5");

    private final String code;

    FeedSize(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}