# 인기글 서비스

인기글 서비스는 아래 기준에 해당하는 상위 N개의 사용자 게시글을 보여줍니다.

인기글 선정 기준은 아래와 같습니다.

- **인기글 선정 기준**
    - `1.0 * 좋아요 수 + 0.05 * 조회 수 + 0.002 * 글자 수 - 1.0 * (현재 시각 - 작성 시각)`
    - 인기글은 **최근 3시간 내 작성된 게시글**에서 선정합니다.
    - 한 번 인기글이 되면 5분 정도는 유지되어도 무방합니다.

해당 커뮤니티에 게시글이 많아지고 많은 사용자들이 접속할 것을 가정하였습니다.

# 1. 기술스택

Docker, MySQL 8.0.2, Java 21, Spring Boot 4.1

# 2. 서비스 규모 가정

- 하루 평균 방문자 수 (DAU) : 350만 명
    
- 하루 평균 게시글 생성량 : 100만 개
    
- 인기글 산정 기준 중 `현재 시각 - 작성 시각` 이 있는데 단위를 ‘분’으로 가정함
    
    가장 오래된 게시글(3시간 전)의 시간감점의 경우
    
    if ‘**시간**’ : `현재 시각 - 작성 시각` = -3
    
    → 이는 3시간 동안 좋아요 3개/조회수 60개(0.05*60=3) 추가된다면 커버 가능하다는 의미.
    
    **시간 조건이 순위변동에 변별력을 주지 못함.**
    
    if ‘**분**’ : `현재 시각 - 작성 시각` = -180
    
    → 1분마다 1점씩 깎임. 1분마다 최소 좋아요 1개/조회수 20개(0.05*20=1)여야 커버 가능하다는 의미
    
    → **시간 조건이 순위변동에 변별력을 많이 줌.**

# 3. 주요 API 시퀀스 다이어그램

- 인기글 목록 조회 API
    
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as PostController
    participant Svc as PostService
    participant Repo as TrendingPostCacheRepository
    participant R as Redis
    participant Ref as TrendingPostCacheRefresher
    participant PAS as PostAccountService
    participant DB as MySQL

    C->>Ctrl: GET /api/posts/trending
    Ctrl->>Svc: getTrendingCacheJson()
    Svc->>Repo: findJson()
    Repo->>R: GET trending:posts:v1
    R-->>Repo: JSON 문자열 또는 nil

    alt 인기글 캐시 히트
        Repo-->>Svc: Optional.of(json) (역직렬화 없음)
    else 인기글 캐시 미스 -> 캐시 갱신
        Repo-->>Svc: Optional.empty()
        Svc->>Ref: 캐시 갱신 요청: refresh() (orElseGet)
        Note over Ref,DB: @Transactional(readOnly = true)
        Ref->>DB: findTrendingPostIds(10)<br/>created_at >= NOW() - 3시간, 점수 DESC, post_id DESC, LIMIT 10
        DB-->>Ref: 인기글 목록

        opt Post-Account 테이블 서버에서 조인
            Ref->>PAS: getPostWithAccounts(postIds)
            PAS->>DB: postRepository.findAllById(postIds)
            DB-->>PAS: Post 목록
            PAS->>DB: accountRepository.findAllById(userIds)
            DB-->>PAS: 이에 해당하는 Account 목록
            PAS-->>Ref: PostWithAccount 목록 
            Ref->>Ref: API 스펙에 맞춰 TrendingPostResponse(DTO)로 가공<br/>본문 100자 미리보기, 탈퇴 작성자는 "탈퇴한 사용자"
        end

        Ref->>Repo: JSON 직렬화: save(List<TrendingPostResponse>)
        Repo->>Repo: objectMapper.writeValueAsString(posts)
        Repo->>R: SET trending:posts:v1 json (TTL 10분)
        Repo-->>Ref: jsonString (캐시 갱신 완료)
        Ref-->>Svc: jsonString (갱신한 값 전달)
    end

    Svc-->>Ctrl: jsonString
    Ctrl->>Ctrl: new RawValue(json)
    Note right of Ctrl: Jackson이 RawValue를 파싱 없이<br/>data 자리에 원문 그대로 기록
    Ctrl-->>C: 200 {status: 2000, data: [...]}
```

- 게시글 상세 조회 API

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as PostController
    participant Svc as PostService
    participant PAS as PostAccountService
    participant DB as MySQL
    participant EH as GlobalExceptionHandler

    C->>Ctrl: GET /api/posts/{postId}
    Ctrl->>Svc: getPost(postId)
    Note over Svc,DB: @Transactional(readOnly = true)
    Svc->>PAS: getPostWithAccount(postId)
    PAS->>DB: postRepository.findById(postId)
    DB-->>PAS: Post 또는 없음

    alt 게시글 없음
        PAS--xEH: PostNotFoundException
        EH-->>C: 404 {status: 4040}
    else 게시글 있음
        PAS->>DB: accountRepository.findById(post.userId)
        DB-->>PAS: Account 또는 없음
        Note right of PAS: Account가 없으면 null로 두고<br/>닉네임을 "탈퇴한 사용자"로 표시
        PAS-->>Svc: PostWithAccount
        Svc-->>Ctrl: PostWithAccount
        Ctrl->>Ctrl: PostDetailResponse(DTO)로 가공
        Ctrl-->>C: 200 {status: 2000, </br>data: PostDetailResponse}
    end
```

- 게시글 조회수 업데이트 API

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as PostController
    participant Svc as PostService
    participant VBP as PostViewBufferPublisher
    participant SQS as SQS<br/>post-view-queue
    participant VC as ViewCountConsumer<br/>(module-batch)
    participant VCS as ViewCountService<br/>(module-batch)
    participant DB as MySQL

    C->>Ctrl: POST /api/posts/{postId}/views<br/>X-User-Id: userId
    Ctrl->>Svc: recordViews(postId, userId)
    Svc->>VBP: enqueue(postId, userId)
    VBP->>VBP: ConcurrentLinkedQueue에 PostViewEvent 추가
    opt 버퍼 크기 100 이상
        VBP->>VBP: flush() 호출 (요청 스레드, synchronized)
    end
    Ctrl-->>C: 202 {status: 2020}

    Note over VBP,DB: 이후 비동기 처리

    loop 200ms마다/버퍼 100건 도달/종료
        VBP->>VBP: 최대 100건 poll
        VBP-)SQS: sqsTemplate.sendAsync(message)
        Note right of VBP: 발행 실패 시 error 로그만 남김 (재시도 없음)
    end

    SQS-)VC: @SqsListener 메시지 수신
    VC->>VCS: applyViewCounts(events)
    Note over VCS,DB: @Transactional
    VCS->>VCS: 쿼리 최적화(distinct)<br/>postId별 userId 그룹핑 (TreeMap, postId 오름차순)
    loop 조회 이력 테이블 추가
        VCS->>DB: INSERT IGNORE INTO post_view
        DB-->>VCS: 삽입된 행 수 = 처음 조회한 사용자 수
    end
    VCS->>DB: 조회수 정보 갱신: <br/>batchUpdate(view_count = view_count + ?)
    VCS-->>VC: 완료 (커밋)
    VC-->>SQS: 정상 종료 시 메시지 삭제(ack)
    Note over SQS,VC: 예외 시 ack 없음, 30초 뒤 재전달<br/>3회 실패하면 post-view-dlq로 이동
```

- 게시글 좋아요 API

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as PostController
    participant Svc as PostService
    participant LR as PostLikeJdbcRepository
    participant LBP as PostLikeBufferPublisher
    participant SQS as SQS<br/>post-like-queue
    participant LC as LikeCountConsumer<br/>(module-batch)
    participant LCS as LikeCountService<br/>(module-batch)
    participant DB as MySQL

    C->>Ctrl: POST /api/posts/{postId}/like<br/>X-User-Id: userId
    Ctrl->>Svc: likePost(postId, userId)
    Svc->>LR: insertLike(postId, userId)
    LR->>DB: 좋아요 이력 추가(INSERT IGNORE INTO post_like)
    DB-->>LR: 신규 좋아요 여부 (0 또는 1)
    LR-->>Svc: 0 또는 1

    alt 0 (이미 좋아요 상태)
        Svc-->>Ctrl: false (큐에 보내지 않음)
        Ctrl-->>C: 200 {status: 2000, data: {postId, <br/>isLiked: true, <br/>isChanged: false}}
    else 1 (신규 좋아요)
        Svc->>LBP: enqueue(postId, +1)
        opt 버퍼 크기 100 이상
            LBP->>LBP: flush() 호출 (요청 스레드)
        end
        Svc-->>Ctrl: true
        Ctrl-->>C: 200 {status: 2000, data: {postId, <br/>isLiked: true, <br/>isChanged: true}}
    end

    Note over LBP,DB: 이후 비동기 처리

    loop 200ms마다/버퍼 100건 도달/종료
        LBP->>LBP: 최대 100건 poll
        LBP-)SQS: sqsTemplate.sendAsync(message)
    end

    SQS-)LC: @SqsListener 메시지 수신
    LC->>LCS: applyLikeCounts(events)
    Note over LCS,DB: @Transactional
    LCS->>LCS: postId별 delta 합산 (TreeMap, postId 오름차순)<br/>합이 0인 postId는 제외
    LCS->>DB: 좋아요 수 갱신<br/>batchUpdate(like_count = like_count + ?)
    LCS-->>LC: 완료 (커밋)
    LC-->>SQS: 정상 종료 시 메시지 삭제(ack)
    Note over SQS,LC: 예외 시 30초 뒤 재전달, 3회 실패하면 post-like-dlq로 이동
```
