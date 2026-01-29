# DoubleCross API 명세서

## 목차
1. [REST API](#rest-api)
2. [WebSocket 통신](#websocket-통신)
3. [Enum 타입](#enum-타입)

---

## REST API

Base URL: `/api/games`

### 1. 게임 생성
새 게임 세션을 생성합니다.

**Endpoint:** `POST /api/games`

**Request Body:**
```json
{
  "difficulty": "NORMAL",    // optional, default: NORMAL (EASY | NORMAL | HARD)
  "maxTurns": 10,            // optional, default: 10 (5~20)
  "turnTimeLimit": 90        // optional, default: 90초 (30~180)
}
```

**Response:** `201 Created`
```json
{
  "sessionId": "uuid",
  "status": "WAITING",
  "currentTurn": 0,
  "player": {
    "participantId": "uuid",
    "secretGenre": "ROMANCE",
    "keyword": {
      "word": "단검",
      "status": "PENDING"
    },
    "guessesRemaining": 3,
    "score": 0
  },
  "story": [],
  "settings": {
    "maxTurns": 10,
    "turnTimeLimit": 90
  },
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

### 2. 게임 정보 조회
게임 세션 정보를 조회합니다.

**Endpoint:** `GET /api/games/{sessionId}`

**Response:** `200 OK`
```json
{
  "sessionId": "uuid",
  "status": "IN_PROGRESS",
  "currentTurn": 3,
  "player": {
    "participantId": "uuid",
    "secretGenre": "ROMANCE",
    "keyword": {
      "word": "단검",
      "status": "USED"
    },
    "guessesRemaining": 2,
    "score": 150
  },
  "story": [
    {
      "turn": 0,
      "author": null,
      "content": "어느 조용한 마을의 오래된 저택에서..."
    },
    {
      "turn": 1,
      "author": "AI",
      "content": "AI가 작성한 스토리..."
    },
    {
      "turn": 2,
      "author": "PLAYER",
      "content": "플레이어가 작성한 스토리..."
    }
  ],
  "settings": {
    "maxTurns": 10,
    "turnTimeLimit": 90
  },
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

### 3. 게임 시작
WAITING 상태의 게임을 시작합니다. (초기 상황 생성)

**Endpoint:** `POST /api/games/{sessionId}/start`

**Response:** `200 OK`
```json
{
  "sessionId": "uuid",
  "status": "IN_PROGRESS",
  "currentTurn": 1,
  "player": { ... },
  "story": [
    {
      "turn": 0,
      "author": null,
      "content": "어느 조용한 마을의 오래된 저택에서..."
    }
  ],
  "settings": { ... },
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

### 4. 게임 취소
게임을 취소합니다. (FINISHED 상태가 아닌 경우만 가능)

**Endpoint:** `DELETE /api/games/{sessionId}`

**Response:** `204 No Content`

---

### 5. 스토리 제출 (테스트용)
플레이어 스토리를 제출합니다.

> **Note:** 실제 게임에서는 WebSocket 사용 권장 (타이머, AI 자동 턴 지원)

**Endpoint:** `POST /api/games/{sessionId}/story`

**Request Body:**
```json
{
  "content": "스토리 내용 (최대 500자)",  // required
  "useKeyword": true,                     // 제시어 사용 여부
  "timeSpent": 45                         // 작성 소요 시간(초)
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "turn": 2,
  "keywordUsed": true,
  "keywordStatus": "USED",
  "nextTurn": {
    "turn": 3,
    "author": "AI",
    "timeLimit": 90
  }
}
```

---

### 6. 제시어 추측
AI의 제시어를 추측합니다.

**Endpoint:** `POST /api/games/{sessionId}/guess`

**Request Body:**
```json
{
  "guessWord": "꽃다발"   // required, 2~50자
}
```

**Response:** `200 OK`
```json
{
  "correct": true,
  "points": 200,
  "guessesRemaining": 2,
  "totalScore": 350,
  "message": "정답입니다! 200점 획득!"
}
```

---

### 7. 게임 결과 조회
종료된 게임의 결과를 조회합니다.

**Endpoint:** `GET /api/games/{sessionId}/result`

**Response:** `200 OK`
```json
{
  "sessionId": "uuid",
  "winner": "PLAYER",
  "genreAnalysis": {
    "ROMANCE": 65,
    "THRILLER": 35
  },
  "qualityFactor": 0.95,
  "finalScores": {
    "romanceScore": 61.75,
    "thrillerScore": 33.25
  },
  "scores": {
    "player": {
      "total": 450,
      "breakdown": {
        "genreWin": 200,
        "genreBonus": 50,
        "guessCorrect": 200,
        "guessWrong": -50,
        "digestSuccess": 50
      }
    },
    "ai": {
      "total": 300,
      "breakdown": {
        "genreWin": 0,
        "genreBonus": 0,
        "guessCorrect": 200,
        "guessWrong": 0,
        "digestSuccess": 100
      }
    }
  },
  "revealed": {
    "playerGenre": "ROMANCE",
    "aiGenre": "THRILLER",
    "playerKeywords": [
      {
        "word": "단검",
        "status": "DIGESTED",
        "usedAtTurn": 4,
        "caughtAtTurn": null
      }
    ],
    "aiKeywords": [
      {
        "word": "꽃다발",
        "status": "CAUGHT",
        "usedAtTurn": 3,
        "caughtAtTurn": 5
      }
    ]
  },
  "unnaturalElements": [
    {
      "turn": 3,
      "element": "꽃다발",
      "reason": "스릴러 분위기에 어울리지 않는 로맨스 요소"
    }
  ],
  "fullStory": "전체 스토리 내용..."
}
```

---

### 8. AI 턴 실행 (테스트용)
AI 턴을 수동으로 실행합니다.

**Endpoint:** `POST /api/games/{sessionId}/ai-turn?useKeyword=false`

**Query Parameters:**
- `useKeyword`: AI가 제시어를 사용할지 여부 (default: false)

**Response:** `200 OK`
```json
{
  "success": true,
  "turn": 3,
  "keywordUsed": false,
  "keywordStatus": "PENDING",
  "nextTurn": {
    "turn": 4,
    "author": "PLAYER",
    "timeLimit": 90
  }
}
```

---

## WebSocket 통신

### 연결 정보
- **STOMP Endpoint:** `/ws`
- **Subscribe:** `/topic/game/{sessionId}`
- **Send:** `/app/game/{sessionId}/{action}`

### 클라이언트 → 서버 메시지

#### 1. 게임 준비 완료
게임 시작을 요청합니다. (WAITING → IN_PROGRESS)

**Destination:** `/app/game/{sessionId}/ready`

**Payload:** 없음

---

#### 2. 스토리 제출
플레이어 스토리를 제출합니다.

**Destination:** `/app/game/{sessionId}/story`

**Payload:**
```json
{
  "content": "스토리 내용",
  "useKeyword": true,
  "timeSpent": 45
}
```

---

#### 3. 제시어 추측
AI의 제시어를 추측합니다.

**Destination:** `/app/game/{sessionId}/guess`

**Payload:**
```json
{
  "guessWord": "꽃다발"
}
```

---

### 서버 → 클라이언트 이벤트

모든 이벤트는 `/topic/game/{sessionId}`로 전송됩니다.

**공통 구조:**
```json
{
  "type": "EVENT_TYPE",
  "payload": { ... },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

---

#### 1. GAME_STARTED
게임이 시작되었습니다.

```json
{
  "type": "GAME_STARTED",
  "payload": {
    "sessionId": "uuid",
    "initialSituation": "어느 조용한 마을의 오래된 저택에서...",
    "firstTurn": {
      "turn": 1,
      "author": "AI",
      "timeLimit": 90,
      "startTime": 1704067200000
    }
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

---

#### 2. AI_TURN_COMPLETED
AI가 스토리를 작성했습니다.

```json
{
  "type": "AI_TURN_COMPLETED",
  "payload": {
    "turn": 1,
    "content": "AI가 작성한 스토리 내용...",
    "nextTurn": {
      "turn": 2,
      "author": "PLAYER",
      "timeLimit": 90,
      "startTime": 1704067260000
    }
  },
  "timestamp": "2024-01-01T00:01:00Z"
}
```

---

#### 3. PLAYER_TURN_COMPLETED
플레이어가 스토리를 제출했습니다.

```json
{
  "type": "PLAYER_TURN_COMPLETED",
  "payload": {
    "turn": 2,
    "content": "플레이어가 작성한 스토리...",
    "keywordUsed": true,
    "nextTurn": {
      "turn": 3,
      "author": "AI",
      "timeLimit": 90,
      "startTime": 1704067320000
    }
  },
  "timestamp": "2024-01-01T00:02:00Z"
}
```

---

#### 4. GUESS_RESULT
제시어 추측 결과입니다.

```json
{
  "type": "GUESS_RESULT",
  "payload": {
    "guesser": "PLAYER",
    "guessWord": "꽃다발",
    "correct": true,
    "points": 200,
    "guessesRemaining": 2,
    "totalScore": 350
  },
  "timestamp": "2024-01-01T00:02:30Z"
}
```

---

#### 5. TIMER_UPDATE
남은 시간 업데이트입니다.

```json
{
  "type": "TIMER_UPDATE",
  "payload": {
    "remainingSeconds": 45,
    "turn": 2
  },
  "timestamp": "2024-01-01T00:01:45Z"
}
```

---

#### 6. TIMER_EXPIRED
턴 시간이 초과되었습니다.

```json
{
  "type": "TIMER_EXPIRED",
  "payload": {
    "turn": 2,
    "author": "PLAYER",
    "penalty": "턴 스킵"
  },
  "timestamp": "2024-01-01T00:03:00Z"
}
```

---

#### 7. GAME_FINISHED
게임이 종료되었습니다.

```json
{
  "type": "GAME_FINISHED",
  "payload": {
    "reason": "ALL_TURNS_COMPLETED",
    "resultAvailable": true
  },
  "timestamp": "2024-01-01T00:10:00Z"
}
```

---

#### 8. ERROR
오류가 발생했습니다.

```json
{
  "type": "ERROR",
  "payload": {
    "code": "INVALID_STATE",
    "message": "진행 중인 게임이 아닙니다."
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**Error Codes:**
- `INVALID_STATE`: 잘못된 게임 상태
- `INVALID_TURN`: 잘못된 턴 (플레이어 턴이 아님)
- `AI_ERROR`: AI 스토리 생성 오류
- `ERROR`: 일반 오류

---

## Enum 타입

### GameStatus
```
WAITING      // 대기 중 (게임 생성됨, 시작 전)
IN_PROGRESS  // 진행 중
FINISHED     // 종료됨
CANCELLED    // 취소됨
```

### Genre
```
ROMANCE   // 로맨스
THRILLER  // 스릴러
COMEDY    // 코미디
SF        // SF
FANTASY   // 판타지
MYSTERY   // 미스터리
```

### Difficulty
```
EASY    // 쉬움
NORMAL  // 보통
HARD    // 어려움
```

### ParticipantType
```
PLAYER  // 플레이어
AI      // AI
```

### KeywordStatus
```
PENDING   // 대기 (아직 사용 안 함)
USED      // 사용됨 (스토리에 삽입함)
CAUGHT    // 들킴 (상대가 맞춤)
DIGESTED  // 소화 성공 (게임 종료까지 안 들킴)
```

### Winner
```
PLAYER  // 플레이어 승리
AI      // AI 승리
DRAW    // 무승부
```

---

## 게임 흐름

```
1. POST /api/games                    → 게임 생성 (WAITING)
2. WebSocket 연결                      → /ws (STOMP)
3. Subscribe                          → /topic/game/{sessionId}
4. Send /app/game/{sessionId}/ready   → 게임 시작 요청
5. Receive GAME_STARTED               → 초기 상황 + 첫 턴 정보
6. (AI 턴이면) Receive AI_TURN_COMPLETED
7. Send /app/game/{sessionId}/story   → 플레이어 스토리 제출
8. Receive PLAYER_TURN_COMPLETED
9. (선택) Send /app/game/{sessionId}/guess → 제시어 추측
10. Receive GUESS_RESULT
11. 반복 (6~10)
12. Receive GAME_FINISHED             → 게임 종료
13. GET /api/games/{sessionId}/result → 결과 조회
```

### 턴 순서
- **홀수 턴 (1, 3, 5, ...)**: AI
- **짝수 턴 (2, 4, 6, ...)**: PLAYER
