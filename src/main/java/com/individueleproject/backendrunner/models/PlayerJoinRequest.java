package com.individueleproject.backendrunner.models;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PlayerJoinRequest {
    private String playerName;
    private String quizCode;
}
