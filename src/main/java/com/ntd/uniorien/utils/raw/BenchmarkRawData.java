package com.ntd.uniorien.utils.raw;

import com.ntd.uniorien.entity.AdmissionInformation;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class BenchmarkRawData {
    String majorCode;
    String majorName;
    String subjectCombinations;
    String score;
    String notes;

}
