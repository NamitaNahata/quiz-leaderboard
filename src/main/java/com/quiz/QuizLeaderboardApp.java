package com.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class QuizLeaderboardApp {

    // ✅ REPLACE THIS WITH YOUR ACTUAL REGISTRATION NUMBER
    private static final String REG_NO = "RA2311026010677";

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int TOTAL_POLLS = 10;
    private static final int DELAY_MS = 5000;

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        System.out.println("=== Quiz Leaderboard System ===");
        System.out.println("Registration Number: " + REG_NO);

        Set<String> seenKeys = new HashSet<>();
        Map<String, Integer> scoreMap = new TreeMap<>();

        for (int poll = 0; poll < TOTAL_POLLS; poll++) {
            System.out.println("\nPolling... poll=" + poll);

            String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + poll;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            System.out.println("Response: " + body);

            Map<?, ?> json = mapper.readValue(body, Map.class);
            List<?> events = (List<?>) json.get("events");

            if (events == null) {
                System.out.println("No events in this poll, skipping.");
            } else {
                for (Object eventObj : events) {
                    Map<?, ?> event = (Map<?, ?>) eventObj;
                    String roundId = (String) event.get("roundId");
                    String participant = (String) event.get("participant");
                    int score = ((Number) event.get("score")).intValue();

                    String uniqueKey = roundId + "_" + participant;

                    if (seenKeys.contains(uniqueKey)) {
                        System.out.println("  DUPLICATE skipped: " + uniqueKey);
                    } else {
                        seenKeys.add(uniqueKey);
                        scoreMap.merge(participant, score, Integer::sum);
                        System.out.println("  Added: " + participant + " +" + score);
                    }
                }
            }

            if (poll < TOTAL_POLLS - 1) {
                System.out.println("Waiting 5 seconds...");
                Thread.sleep(DELAY_MS);
            }
        }

        System.out.println("\n=== Building Leaderboard ===");
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scoreMap.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        int grandTotal = 0;
        for (Map.Entry<String, Integer> entry : sorted) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("participant", entry.getKey());
            item.put("totalScore", entry.getValue());
            leaderboard.add(item);
            grandTotal += entry.getValue();
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Grand Total: " + grandTotal);

        System.out.println("\n=== Submitting Leaderboard ===");
        Map<String, Object> submitPayload = new LinkedHashMap<>();
        submitPayload.put("regNo", REG_NO);
        submitPayload.put("leaderboard", leaderboard);

        String requestBody = mapper.writeValueAsString(submitPayload);
        System.out.println("Payload: " + requestBody);

        HttpRequest submitRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> submitResponse = httpClient.send(submitRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Submit Response: " + submitResponse.body());

        Map<?, ?> result = mapper.readValue(submitResponse.body(), Map.class);
        System.out.println("\n✅ Submission complete!");
        System.out.println("Submitted Total: " + result.get("submittedTotal"));
        System.out.println("Attempt Count: " + result.get("attemptCount"));
    }
}