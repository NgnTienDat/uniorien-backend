package com.ntd.uniorien.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UniversityDetailResponse {
    String id;
    String universityName;
    String universityCode;
    String location;
    String websiteAddress;
    String about;
    String logo;
}
