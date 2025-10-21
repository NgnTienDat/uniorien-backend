package com.ntd.uniorien.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UniversityResponse {
    String id;
    String universityCode;
    String universityName;
    String website;

    List<AdmissionResponse> admissionList;

    public UniversityResponse(String id, String universityCode, String universityName, String website) {
        this.id = id;
        this.universityCode = universityCode;
        this.universityName = universityName;
        this.website = website;
    }

    public UniversityResponse(String id, String universityCode, String universityName, String website, List<AdmissionResponse> admissionList) {
        this.id = id;
        this.universityCode = universityCode;
        this.universityName = universityName;
        this.website = website;
        this.admissionList = admissionList;
    }
}
