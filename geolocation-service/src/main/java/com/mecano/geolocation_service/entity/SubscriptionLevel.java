package com.mecano.geolocation_service.entity;

public enum SubscriptionLevel {
    BASIC(1), SILVER(2), GOLD(3);
    private final int score;
    SubscriptionLevel(int score) { this.score = score; }
    public int getScore() { return score; }
}
