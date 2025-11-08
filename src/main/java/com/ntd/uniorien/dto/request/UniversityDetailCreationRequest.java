package com.ntd.uniorien.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UniversityDetailCreationRequest {

    @NotBlank(message = "NOT_BLANK")
    String name;

    String location;
    String websiteAddress;
    String about;
    String logo;

    @NotBlank(message = "NOT_BLANK")
    String universityId;


}
