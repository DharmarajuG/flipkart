package shop.krishna.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Serialization-friendly pagination wrapper (avoids exposing Spring's PageImpl JSON). */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isLast());
    }
}
