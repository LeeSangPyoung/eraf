# ERAF Data Elasticsearch

Spring Data Elasticsearch 기반 검색 솔루션

## 개요

ERAF Data Elasticsearch는 Elasticsearch와의 통합을 위한 자동 구성 및 유틸리티를 제공합니다.

### 주요 기능

- **Spring Data Elasticsearch 자동 구성**
- **Repository 패턴 지원**
- **전문 검색 (Full-Text Search)**
- **집계 및 분석 쿼리**
- **자동 인덱스 생성**

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-data-elasticsearch</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 빠른 시작

### 1. 설정

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: changeme
    connection-timeout: 5s
    socket-timeout: 30s
```

### 2. 도큐먼트 정의

```java
@Document(indexName = "products")
public class Product {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;
}
```

### 3. Repository 작성

```java
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    List<Product> findByName(String name);

    List<Product> findByCategory(String category);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}]}}")
    List<Product> searchByName(String name);
}
```

### 4. 서비스 활용

```java
@Service
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public List<Product> searchProducts(String keyword) {
        // 전문 검색
        Query query = new StringQuery(
            "{\"match\": {\"name\": \"" + keyword + "\"}}"
        );

        SearchHits<Product> hits = elasticsearchOperations.search(query, Product.class);

        return hits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }

    public List<Product> searchWithFilters(String keyword, String category, Double minPrice) {
        // 복합 검색
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (keyword != null) {
            boolQuery.must(m -> m.match(
                t -> t.field("name").query(keyword)
            ));
        }

        if (category != null) {
            boolQuery.filter(f -> f.term(
                t -> t.field("category").value(category)
            ));
        }

        if (minPrice != null) {
            boolQuery.filter(f -> f.range(
                r -> r.field("price").gte(JsonData.of(minPrice))
            ));
        }

        Query query = new NativeQuery(boolQuery.build()._toQuery());

        return elasticsearchOperations.search(query, Product.class)
            .stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }

    public Map<String, Long> getProductCountByCategory() {
        // 집계 쿼리
        Query query = new NativeQuery(
            new TermsAggregationBuilder("categories")
                .field("category")
                .size(100)
        );

        SearchHits<Product> hits = elasticsearchOperations.search(query, Product.class);

        Aggregations aggregations = hits.getAggregations();
        // 집계 결과 처리...
    }
}
```

## 실전 예제

### 페이지네이션

```java
public Page<Product> searchProducts(String keyword, Pageable pageable) {
    Query query = new StringQuery(
        "{\"match\": {\"name\": \"" + keyword + "\"}}"
    );
    query.setPageable(pageable);

    SearchHits<Product> hits = elasticsearchOperations.search(query, Product.class);

    return new PageImpl<>(
        hits.stream().map(SearchHit::getContent).collect(Collectors.toList()),
        pageable,
        hits.getTotalHits()
    );
}
```

### 자동완성

```java
public List<String> autocomplete(String prefix) {
    Query query = new NativeQuery(
        new PrefixQuery.Builder()
            .field("name.keyword")
            .value(prefix)
            .build()._toQuery()
    );

    return elasticsearchOperations.search(query, Product.class)
        .stream()
        .map(hit -> hit.getContent().getName())
        .distinct()
        .limit(10)
        .collect(Collectors.toList());
}
```

## 참고 자료

- [Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch)
- [Elasticsearch Documentation](https://www.elastic.co/guide/)

## 라이선스

Copyright 2024 ERAF Platform
