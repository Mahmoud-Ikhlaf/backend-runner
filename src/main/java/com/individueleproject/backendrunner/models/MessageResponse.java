package com.individueleproject.backendrunner.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResponse {
    private final String message;
}
