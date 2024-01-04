package com.individueleproject.backendrunner.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QuizService {

    private final Map<String, Map<String, Long>> quizUserScores = new HashMap<>();
    private final Map<String, String> currentAnswer = new HashMap<>();

    public void createQuiz(String quizCode) {
        quizUserScores.computeIfAbsent(quizCode, k -> new HashMap<>());
    }

    public boolean joinQuiz(String quizCode, String playerName) {
        if (!quizUserScores.containsKey(quizCode)) {
            return false;
        }

        if (quizUserScores.get(quizCode).containsKey(playerName)) {
            return false;
        }

        quizUserScores.get(quizCode).put(playerName, 0L);
        return true;
    }

    public Map<String, Long> getUserScores(String quizId) {
        return quizUserScores.getOrDefault(quizId, new HashMap<>());
    }

    public void updateScore(String quizId, String playerName, Long points) {
        Map<String, Long> quizScores = quizUserScores.get(quizId);

        Long currentScore = quizScores.get(playerName);

        if (currentScore == null) {
            currentScore = 0L;
        }

        Long updatedScore = currentScore + points;

        quizUserScores.get(quizId).put(playerName, updatedScore);
    }

    public boolean checkAnswer(String quizCode, String answer) {
        String correctAnswer = currentAnswer.get(quizCode);
        return answer.equals(correctAnswer);
    }

    public long calculatePoints(long timeElapsed, boolean isCorrect) {
        int basePoints = isCorrect ? 20 : 0;
        long timePenalty = timeElapsed * 2;

        return Math.max(basePoints - timePenalty, 0);
    }

    public void setCorrectAnswer(String quizCode, String correctAnswer) {
        currentAnswer.put(quizCode, correctAnswer);
    }

    public void endQuiz(String quizCode) {
        currentAnswer.remove(quizCode);
        quizUserScores.remove(quizCode);
    }
}
