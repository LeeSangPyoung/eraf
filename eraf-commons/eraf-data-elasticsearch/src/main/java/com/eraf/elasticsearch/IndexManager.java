package com.eraf.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

/**
 * Elasticsearch 인덱스 생명주기 관리
 *
 * ElasticsearchOperations를 사용하여 인덱스 생성, 삭제, 존재 확인, 리프레시를 지원합니다.
 */
public class IndexManager {

    private static final Logger log = LoggerFactory.getLogger(IndexManager.class);

    private final ElasticsearchOperations elasticsearchOperations;

    public IndexManager(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * Document 클래스 기반 인덱스 생성
     *
     * @param documentClass Document 클래스 (@Document 어노테이션 필요)
     * @return 생성 성공 여부
     */
    public boolean createIndex(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        if (!indexOps.exists()) {
            boolean created = indexOps.create();
            if (created) {
                indexOps.putMapping(indexOps.createMapping(documentClass));
                log.info("Index created for document class: {}", documentClass.getSimpleName());
            }
            return created;
        }
        log.debug("Index already exists for document class: {}", documentClass.getSimpleName());
        return false;
    }

    /**
     * 인덱스 이름으로 인덱스 생성
     *
     * @param indexName 인덱스 이름
     * @return 생성 성공 여부
     */
    public boolean createIndex(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        if (!indexOps.exists()) {
            boolean created = indexOps.create();
            log.info("Index created: {}", indexName);
            return created;
        }
        log.debug("Index already exists: {}", indexName);
        return false;
    }

    /**
     * Document 클래스 기반 인덱스 삭제
     *
     * @param documentClass Document 클래스
     * @return 삭제 성공 여부
     */
    public boolean deleteIndex(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        if (indexOps.exists()) {
            boolean deleted = indexOps.delete();
            log.info("Index deleted for document class: {}", documentClass.getSimpleName());
            return deleted;
        }
        return false;
    }

    /**
     * 인덱스 이름으로 인덱스 삭제
     *
     * @param indexName 인덱스 이름
     * @return 삭제 성공 여부
     */
    public boolean deleteIndex(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        if (indexOps.exists()) {
            boolean deleted = indexOps.delete();
            log.info("Index deleted: {}", indexName);
            return deleted;
        }
        return false;
    }

    /**
     * Document 클래스 기반 인덱스 존재 확인
     *
     * @param documentClass Document 클래스
     * @return 존재 여부
     */
    public boolean indexExists(Class<?> documentClass) {
        return elasticsearchOperations.indexOps(documentClass).exists();
    }

    /**
     * 인덱스 이름으로 존재 확인
     *
     * @param indexName 인덱스 이름
     * @return 존재 여부
     */
    public boolean indexExists(String indexName) {
        return elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists();
    }

    /**
     * Document 클래스 기반 인덱스 리프레시
     * 검색 가능 상태로 즉시 반영
     *
     * @param documentClass Document 클래스
     */
    public void refreshIndex(Class<?> documentClass) {
        elasticsearchOperations.indexOps(documentClass).refresh();
        log.debug("Index refreshed for document class: {}", documentClass.getSimpleName());
    }

    /**
     * 인덱스 이름으로 리프레시
     *
     * @param indexName 인덱스 이름
     */
    public void refreshIndex(String indexName) {
        elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).refresh();
        log.debug("Index refreshed: {}", indexName);
    }
}
