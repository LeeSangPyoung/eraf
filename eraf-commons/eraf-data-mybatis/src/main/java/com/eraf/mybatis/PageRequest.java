package com.eraf.mybatis;

/**
 * MyBatis 페이지네이션 요청
 *
 * 페이지 번호, 페이지 크기, 정렬 조건을 캡슐화합니다.
 */
public class PageRequest {

    private int page;
    private int size;
    private String orderBy;

    public PageRequest() {
        this.page = 0;
        this.size = 20;
    }

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageRequest(int page, int size, String orderBy) {
        this.page = page;
        this.size = size;
        this.orderBy = orderBy;
    }

    /**
     * 팩토리 메서드
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    /**
     * 정렬 포함 팩토리 메서드
     */
    public static PageRequest of(int page, int size, String orderBy) {
        return new PageRequest(page, size, orderBy);
    }

    /**
     * 오프셋 계산
     */
    public int getOffset() {
        return page * size;
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
