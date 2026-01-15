package com.eraf.core.converter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity ↔ DTO 매핑 베이스 클래스
 *
 * @param <E> Entity 타입
 * @param <D> DTO 타입
 */
public abstract class BaseMapper<E, D> {

    /**
     * Entity를 DTO로 변환
     */
    public abstract D toDto(E entity);

    /**
     * DTO를 Entity로 변환
     */
    public abstract E toEntity(D dto);

    /**
     * Entity 리스트를 DTO 리스트로 변환
     */
    public List<D> toDtoList(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * DTO 리스트를 Entity 리스트로 변환
     */
    public List<E> toEntityList(List<D> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 기존 Entity를 DTO 값으로 업데이트
     */
    public void updateEntity(E entity, D dto) {
        // 서브클래스에서 구현
    }
}
