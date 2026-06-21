package com.gamehubbot.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageUtil {

    public static <T> PageResponse<T> pageInfo(Page<?> page, List<T> responseData) {
        return new PageResponse<>(
                responseData,
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

    public record PageResponse<T>(
            List<T> content,
            int pageNumber,
            int totalPages,
            long totalElements
    ) {
    }
}


