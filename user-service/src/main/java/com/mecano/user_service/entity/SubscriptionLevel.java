package com.mecano.user_service.entity;

public enum SubscriptionLevel {
    BASIC(1),
    SILVER(2),
    GOLD(3);

    private final int priorityScore;

    SubscriptionLevel(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public int getPriorityScore() { return priorityScore; }
}