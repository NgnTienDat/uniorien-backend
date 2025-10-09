package com.ntd.uniorien.utils.raw;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SchoolInfo {
    String code;
    String name;
    String url;
}
