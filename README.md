# Quiz Leaderboard System — Bajaj Finserv Health Java Qualifier

## Problem Statement
Build a Java application that polls an external API 10 times, handles duplicate event data, aggregates scores per participant, and submits a correct leaderboard.

## My Approach
I used a `HashSet` to store unique keys formed by combining `roundId + participant`. Before adding any score, I check if the key already exists — if yes, it's a duplicate and gets ignored. This ensures each round score per participant is counted exactly once.

## How It Works
1. Poll `/quiz/messages` API 10 times (poll 0 to 9) with a 5 second delay between each call
2. For every event received, generate a unique key: `roundId_participant`
3. If key is already seen → skip (duplicate)
4. If key is new → add score to participant's total
5. Sort leaderboard by totalScore descending
6. Submit once to `/quiz/submit`

## Sample Output
```
Polling... poll=0 → Added: George +300, Added: Hannah +250
Polling... poll=1 → Added: Ivan +185
Polling... poll=2 → Added: George +220, Added: Ivan +195
Polling... poll=3 → Added: Hannah +310
Polling... poll=4 → DUPLICATE skipped: R1_George, Added: Ivan +160
Polling... poll=5 → Added: George +275
Polling... poll=6 → DUPLICATE skipped: R2_Hannah, Added: Hannah +190
Polling... poll=7 → Added: Ivan +205
Polling... poll=8 → DUPLICATE skipped: R1_Ivan, DUPLICATE skipped: R4_Hannah
Polling... poll=9 → DUPLICATE skipped: R3_George

Final Leaderboard:
George: 795
Hannah: 750
Ivan:   745
Grand Total: 2290
```

## Tech Stack
- Java 22
- Maven 3.9
- Jackson (JSON parsing)
- Java HttpClient (API calls)

## How to Run
```bash
mvn clean package
java -jar target/quiz-leaderboard-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Registration Number
RA2311026010677
