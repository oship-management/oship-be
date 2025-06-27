package org.example.oshipserver.global.common.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PageResponseDto<T> {
    private List<T> content; // 결과 Data 목록

    private Long totalElements; // 총 Data 수

    private Integer page; // 페이지 번호

    private Integer size; // 한 페이지에 표시할 데이터 개수

    private Integer totalPages; // 총 페이지 수

    public static <T> PageResponseDto<T> toDto(Page<T> result) {
        return PageResponseDto.<T>builder()
                .content(result.get().toList())
                .totalElements(result.getTotalElements())
                .size(result.getSize())
                .page(result.getNumber() + 1)
                .totalPages(result.getTotalPages())
                .build();
    }
}
