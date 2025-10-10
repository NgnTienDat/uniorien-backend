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
public class AdmissionInfoRawData {
    String year;
    String admissionMethod;
    List<BenchmarkRawData> benchmarks = new ArrayList<>();;

}
