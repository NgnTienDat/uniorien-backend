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
    float rating;
    int students;
    String founded;
    String institutionType; // loại hình: Công lập / tư thục / Quốc tế...
    String programsOffered; // Đào tạo: Đa ngành / Kỹ thuật / Kinh tế...

    @NotBlank(message = "NOT_BLANK")
    String universityId;


}
