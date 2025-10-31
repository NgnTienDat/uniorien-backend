package com.ntd.uniorien.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MajorFilterResponse {
    String universityName;
    List<MajorDetailResponse> majorDetails;
}