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
    String universityName;
    String universityId;
    String universityCode;
    String location;
    String websiteAddress;
    String about;
    String logo;
    float rating;
    int students;
    String founded;
    String institutionType; // loại hình: Công lập / tư thục / Quốc tế...
    String programsOffered; // Đào tạo: Đa ngành / Kỹ thuật / Kinh tế...
}


/*
  id: string;
  universityName: string;
  universityId: string;
  universityCode: string;
  location: string;
  websiteAddress: string;
  about: string;
  logo?: string;
  rating?: number;
  students?: number;
  founded?: number;
  acceptance?: string;
  campusImages: { id: number; url: string }[];
 */
