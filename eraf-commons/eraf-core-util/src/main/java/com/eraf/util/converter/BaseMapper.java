package com.eraf.util.converter;

import java.util.Collections;
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
            return Collections.emptyList();
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
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 기존 Entity를 DTO 값으로 업데이트
     *
     * <p>기본 구현은 지원하지 않습니다.
     * 필요한 경우 서브클래스에서 오버라이드하세요.</p>
     *
     * @param entity 업데이트할 Entity
     * @param dto    업데이트에 사용할 DTO
     * @throws UnsupportedOperationException 오버라이드하지 않고 호출 시
     */
    public void updateEntity(E entity, D dto) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + ".updateEntity() is not implemented. Override in subclass.");
    }
}
