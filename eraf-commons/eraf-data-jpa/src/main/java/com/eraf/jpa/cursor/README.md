# 커서 기반 페이징 (Cursor-based Pagination)

대용량 데이터셋을 효율적으로 페이징하는 커서 기반 페이징 기능입니다.

## 왜 커서 기반 페이징인가?

### Offset 기반 페이징의 문제점

```java
// Offset 기반 (비효율적)
SELECT * FROM users ORDER BY id LIMIT 100 OFFSET 10000;
// 데이터베이스는 10,000개를 스캔한 후 100개 반환 → 느림!
```

**문제점:**
- 페이지 번호가 클수록 성능 저하 (O(n) 복잡도)
- 데이터 추가/삭제 시 중복/누락 발생 가능
- 대규모 데이터셋에서 사용 불가

### 커서 기반 페이징의 장점

```java
// 커서 기반 (효율적)
SELECT * FROM users WHERE id > 10000 ORDER BY id LIMIT 100;
// 인덱스를 사용하여 바로 위치 찾기 → 빠름!
```

**장점:**
- 일정한 성능 (O(1) 복잡도, 인덱스 사용)
- 실시간 데이터 변경에도 안정적
- 무한 스크롤에 최적화
- 대규모 데이터셋 처리 가능

## 사용 예시

### 1. 기본 사용법 (ID 기반)

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ID를 커서로 사용하는 쿼리
    @Query("""
        SELECT u FROM User u
        WHERE (:cursor IS NULL OR u.id > :cursor)
        ORDER BY u.id ASC
        """)
    List<User> findAllByCursor(
        @Param("cursor") Long cursor,
        Pageable pageable
    );
}

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public CursorPage<User, Long> getUsers(Long cursor, int size) {
        // 1. 페이지 요청 생성
        CursorPageRequest<Long> request = cursor == null
                ? CursorPageRequest.first(size)
                : CursorPageRequest.of(cursor, size);

        // 2. size+1 개 조회 (다음 페이지 존재 여부 확인)
        Pageable pageable = PageRequest.of(0, request.getFetchSize());
        List<User> results = repository.findAllByCursor(cursor, pageable);

        // 3. 커서 페이지로 변환
        return CursorPageBuilder.build(results, request, User::getId);
    }
}
```

### 2. 컨트롤러에서 사용

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public CursorPage<UserDto, Long> getUsers(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {

        CursorPage<User, Long> page = userService.getUsers(cursor, size);

        // DTO 변환
        List<UserDto> content = page.getContent().stream()
                .map(UserDto::from)
                .toList();

        return new CursorPage<>(content, page.getNextCursor(),
                                page.hasMore(), page.getSize());
    }
}
```

### 3. 응답 형식

**첫 페이지 요청:**
```http
GET /api/users?size=3
```

**응답:**
```json
{
  "content": [
    {"id": 1, "name": "Alice"},
    {"id": 2, "name": "Bob"},
    {"id": 3, "name": "Charlie"}
  ],
  "nextCursor": 3,
  "hasMore": true,
  "size": 3,
  "contentSize": 3
}
```

**다음 페이지 요청:**
```http
GET /api/users?cursor=3&size=3
```

**응답:**
```json
{
  "content": [
    {"id": 4, "name": "David"},
    {"id": 5, "name": "Eve"}
  ],
  "nextCursor": null,
  "hasMore": false,
  "size": 3,
  "contentSize": 2
}
```

### 4. Timestamp 기반 커서

```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        SELECT p FROM Post p
        WHERE (:cursor IS NULL OR p.createdAt < :cursor)
        ORDER BY p.createdAt DESC
        """)
    List<Post> findAllByCursor(
        @Param("cursor") Instant cursor,
        Pageable pageable
    );
}

@Service
public class PostService {

    public CursorPage<Post, Instant> getPosts(String encodedCursor, int size) {
        // 1. 커서 디코딩
        Instant cursor = CursorUtils.decodeInstant(encodedCursor);

        // 2. 페이지 요청
        CursorPageRequest<Instant> request = cursor == null
                ? CursorPageRequest.first(size)
                : CursorPageRequest.of(cursor, size, SortDirection.DESC);

        // 3. 조회
        Pageable pageable = PageRequest.of(0, request.getFetchSize());
        List<Post> results = repository.findAllByCursor(cursor, pageable);

        // 4. 커서 페이지 생성
        CursorPage<Post, Instant> page = CursorPageBuilder.build(
            results, request, Post::getCreatedAt
        );

        // 5. 커서 인코딩 (응답에 사용)
        String nextCursor = CursorUtils.encodeInstant(page.getNextCursor());

        return new CursorPage<>(
            page.getContent(),
            nextCursor,
            page.hasMore(),
            page.getSize()
        );
    }
}
```

### 5. 복합 커서 (ID + Timestamp)

중복된 timestamp를 가진 데이터가 있을 때 ID를 추가로 사용:

```java
@Query("""
    SELECT p FROM Post p
    WHERE (:cursor IS NULL) OR
          (p.createdAt < :cursorTime) OR
          (p.createdAt = :cursorTime AND p.id > :cursorId)
    ORDER BY p.createdAt DESC, p.id ASC
    """)
List<Post> findAllByCursor(
    @Param("cursorTime") Instant cursorTime,
    @Param("cursorId") Long cursorId,
    Pageable pageable
);

// 서비스에서 복합 커서 처리
public CursorPage<Post, String> getPosts(String encodedCursor, int size) {
    Long cursorId = null;
    Instant cursorTime = null;

    if (encodedCursor != null) {
        String[] parts = CursorUtils.decodeComposite(encodedCursor);
        cursorTime = Instant.ofEpochMilli(Long.parseLong(parts[0]));
        cursorId = Long.parseLong(parts[1]);
    }

    CursorPageRequest<String> request = CursorPageRequest.of(encodedCursor, size);

    List<Post> results = repository.findAllByCursor(cursorTime, cursorId,
        PageRequest.of(0, request.getFetchSize()));

    return CursorPageBuilder.build(results, request, post ->
        CursorUtils.encodeComposite(post.getCreatedAt().toEpochMilli(), post.getId())
    );
}
```

### 6. QueryDSL 사용

```java
@Repository
public class PostRepositoryCustom {

    @Autowired
    private JPAQueryFactory queryFactory;

    public List<Post> findAllByCursor(Long cursor, int limit) {
        QPost post = QPost.post;

        BooleanBuilder whereClause = new BooleanBuilder();
        if (cursor != null) {
            whereClause.and(post.id.gt(cursor));
        }

        return queryFactory
                .selectFrom(post)
                .where(whereClause)
                .orderBy(post.id.asc())
                .limit(limit)
                .fetch();
    }

    public CursorPage<Post, Long> findAllWithCursor(Long cursor, int size) {
        CursorPageRequest<Long> request = cursor == null
                ? CursorPageRequest.first(size)
                : CursorPageRequest.of(cursor, size);

        List<Post> results = findAllByCursor(cursor, request.getFetchSize());

        return CursorPageBuilder.build(results, request, Post::getId);
    }
}
```

## 프론트엔드 통합

### React 예시 (Infinite Scroll)

```typescript
import { useState, useEffect } from 'react';

interface CursorPage<T> {
  content: T[];
  nextCursor: string | null;
  hasMore: boolean;
  size: number;
}

function UserList() {
  const [users, setUsers] = useState<User[]>([]);
  const [cursor, setCursor] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const loadMore = async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (cursor) params.append('cursor', cursor);
      params.append('size', '20');

      const response = await fetch(`/api/users?${params}`);
      const page: CursorPage<User> = await response.json();

      setUsers(prev => [...prev, ...page.content]);
      setCursor(page.nextCursor);
      setHasMore(page.hasMore);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMore();
  }, []);

  return (
    <div>
      {users.map(user => (
        <div key={user.id}>{user.name}</div>
      ))}
      {hasMore && (
        <button onClick={loadMore} disabled={loading}>
          {loading ? 'Loading...' : 'Load More'}
        </button>
      )}
    </div>
  );
}
```

## Best Practices

### 1. 인덱스 설정

커서로 사용하는 필드에 인덱스 필수:

```sql
-- 단일 필드 인덱스
CREATE INDEX idx_user_id ON users(id);
CREATE INDEX idx_post_created_at ON posts(created_at);

-- 복합 인덱스 (복합 커서 사용 시)
CREATE INDEX idx_post_created_at_id ON posts(created_at DESC, id ASC);
```

### 2. 커서 필드 선택 기준

**좋은 커서 필드:**
- Unique 또는 Unique + 추가 필드 (중복 방지)
- Sequential (순차적): ID, Timestamp
- Immutable (불변): 생성 시간, ID
- Indexed (인덱스 존재)

**나쁜 커서 필드:**
- Non-unique without secondary field (중복 가능)
- Mutable (변경 가능): 업데이트 시간, 이름
- Non-indexed (인덱스 없음)

### 3. 페이지 크기 제한

```java
public CursorPageRequest(C cursor, int size) {
    if (size <= 0 || size > 1000) {
        throw new IllegalArgumentException("Size must be between 1 and 1000");
    }
    // ...
}
```

### 4. 커서 인코딩

- 보안: Base64 인코딩으로 내부 구조 숨김
- URL-safe: URL에 사용 가능한 Base64 변형 사용
- 클라이언트는 커서를 불투명한 토큰으로 취급

### 5. 에러 처리

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handleInvalidCursor(IllegalArgumentException e) {
    return ResponseEntity
        .badRequest()
        .body(new ErrorResponse("Invalid cursor format"));
}
```

## 성능 비교

### Offset vs Cursor (1M 레코드)

| 페이지        | Offset (ms) | Cursor (ms) | 개선율   |
|---------------|-------------|-------------|----------|
| 1 (0-100)     | 10          | 5           | 2x       |
| 100 (10K)     | 150         | 5           | 30x      |
| 1000 (100K)   | 1,200       | 5           | 240x     |
| 10000 (1M)    | 12,000      | 5           | 2400x    |

**결론**: 페이지가 뒤로 갈수록 커서 기반이 압도적으로 빠름!

## 제한사항

### 1. 페이지 번호 없음

- 특정 페이지로 점프 불가
- "1, 2, 3..." 페이지네이션 UI 사용 불가
- 무한 스크롤에만 적합

### 2. 총 개수 없음

- 전체 레코드 수를 제공하지 않음
- COUNT(*) 쿼리는 비용이 크므로 피함
- "Showing 1-20 of 1000" 같은 UI 불가

### 3. 역방향 탐색 제한

- 이전 페이지로 돌아가기 어려움
- 앞으로만 진행하는 것이 일반적
- 양방향 필요 시 prev/next 커서 모두 제공 필요

## 대안 전략

### Offset을 사용해야 하는 경우

- 페이지 번호 기반 UI 필요
- 특정 페이지로 점프 필요
- 데이터셋이 작음 (< 10,000 레코드)
- 전체 개수 표시 필요

### Keyset Pagination

커서 기반과 유사하지만:
- 여러 필드를 조합한 복잡한 정렬 지원
- 양방향 탐색 가능
- 구현이 더 복잡

## 트러블슈팅

### 문제: 데이터가 중복/누락됨

**원인**: 커서 필드가 unique하지 않음

**해결**: 복합 커서 사용 (예: timestamp + id)

### 문제: 성능이 여전히 느림

**원인**: 인덱스 누락

**해결**:
```sql
-- 인덱스 확인
EXPLAIN SELECT * FROM posts WHERE created_at < ? ORDER BY created_at DESC LIMIT 20;

-- 인덱스 추가
CREATE INDEX idx_post_created_at ON posts(created_at);
```

### 문제: Invalid cursor error

**원인**: 클라이언트가 잘못된 커서 전송

**해결**: 커서 검증 및 친절한 에러 메시지
```java
try {
    Long cursor = CursorUtils.decodeLong(encodedCursor);
} catch (IllegalArgumentException e) {
    throw new BadRequestException("Invalid cursor. Please start from the first page.");
}
```
