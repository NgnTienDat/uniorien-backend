package com.ntd.uniorien.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MajorGroupResponse {
    int id;
    String majorGroupName;
    int numberOfMajors;
    List<String> majors = new ArrayList<>();

}
