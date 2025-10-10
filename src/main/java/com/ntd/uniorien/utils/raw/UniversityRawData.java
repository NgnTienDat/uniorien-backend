package com.ntd.uniorien.utils.raw;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UniversityRawData {
    String universityCode;
    String universityName;
    String websiteUrl;
    List<AdmissionInfoRawData> admissions = new ArrayList<>();;

}
