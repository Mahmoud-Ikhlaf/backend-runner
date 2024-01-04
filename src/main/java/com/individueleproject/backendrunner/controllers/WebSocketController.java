package com.individueleproject.backendrunner.controllers;

import com.individueleproject.backendrunner.models.PlayerInfo;
import com.individueleproject.backendrunner.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WebSocketController {
    private final QuizService quizService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/join/{quizCode}")
    @SendTo("/topic/{quizCode}/playerJoined")
    public PlayerInfo playerJoined(@DestinationVariable String quizCode, String playerName) {

        if (!quizService.joinQuiz(quizCode, playerName)) {
            return PlayerInfo.builder().build();
        }

        return PlayerInfo.builder().name(playerName).build();
    }

    @MessageMapping("/start")
    public void quizStart(String message) {
        String[] parts = message.split(",");
        String quizCode = parts[0];
        String firstCorrectAnswer = parts[1];
        long serverTime = System.currentTimeMillis();

        quizService.setCorrectAnswer(quizCode, firstCorrectAnswer);

        messagingTemplate.convertAndSend("/topic/" + quizCode + "/quizStart", "true," + String.valueOf(serverTime));
    }

    @MessageMapping("/create")
    public void quizCreated(String quizCode) {
        quizService.createQuiz(quizCode);
    }

    @MessageMapping("/{quizCode}/answer")
    public void playerAnswered(@DestinationVariable String quizCode, String message) {
        String[] parts = message.split(",");
        String playerName = parts[0];
        String answer = parts[1];
        Long timeElapsed = Long.parseLong(parts[2]);

        boolean isCorrect = quizService.checkAnswer(quizCode, answer);
        Long points = quizService.calculatePoints(timeElapsed, isCorrect);
        quizService.updateScore(quizCode, playerName, points);

        messagingTemplate.convertAndSend("/topic/" + quizCode + "/" + playerName + "/answer", isCorrect);
        messagingTemplate.convertAndSend("/topic/" + quizCode + "/playerAnswered", true);
    }

    @MessageMapping("/{quizCode}/questionEnded")
    public void playerAnswered(@DestinationVariable String quizCode, Boolean bool) {
        Map<String, Long> scores = quizService.getUserScores(quizCode);

        List<Map<String, Object>> scoresList = new ArrayList<>();

        for (Map.Entry<String, Long> entry : scores.entrySet()) {
            Map<String, Object> scoreMap = new HashMap<>();
            scoreMap.put("playerName", entry.getKey());
            scoreMap.put("score", entry.getValue());
            scoresList.add(scoreMap);
        }

        messagingTemplate.convertAndSend("/topic/" + quizCode + "/questionEnded", bool);
        messagingTemplate.convertAndSend("/topic/" + quizCode + "/scores", scoresList);
    }

    @MessageMapping("/{quizCode}/nextQuestion")
    public void nextQuestion(@DestinationVariable String quizCode, String correctAnswer) {
        quizService.setCorrectAnswer(quizCode, correctAnswer);
        long serverTime = System.currentTimeMillis();

        messagingTemplate.convertAndSend("/topic/" + quizCode + "/nextQuestion", String.valueOf(serverTime));
    }

    @MessageMapping("/{quizCode}/quizEnded")
    public void quizEnded(@DestinationVariable String quizCode, Boolean bool) {
        quizService.endQuiz(quizCode);

        messagingTemplate.convertAndSend("/topic/" + quizCode + "/quizEnded", bool);
    }
}
