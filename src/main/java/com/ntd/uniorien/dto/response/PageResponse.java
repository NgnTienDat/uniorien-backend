package com.ntd.uniorien.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    List<T> content;
    int pageNumber;
    int pageSize;
    long totalElements;
    int totalPages;
    boolean first;
    boolean last;
    boolean empty;
}