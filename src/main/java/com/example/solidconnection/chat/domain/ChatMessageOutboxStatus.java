package com.example.solidconnection.chat.domain;

public enum ChatMessageOutboxStatus {

    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
