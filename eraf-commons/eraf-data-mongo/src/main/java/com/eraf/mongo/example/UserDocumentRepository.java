package com.eraf.mongo.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB Repository 사용 예제
 */
public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<UserDocument> findByEmail(String email);

    /**
     * 이름으로 검색 (부분 일치)
     */
    List<UserDocument> findByNameContaining(String name);

    /**
     * 활성 사용자 조회
     */
    List<UserDocument> findByActiveTrue();

    /**
     * 나이 범위로 조회
     */
    List<UserDocument> findByAgeBetween(Integer minAge, Integer maxAge);

    /**
     * Custom Query 예제
     */
    @Query("{ 'email': ?0, 'active': true }")
    Optional<UserDocument> findActiveByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}
