package com.ntd.uniorien.utils.raw;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MajorGroupRawData {
    private String groupName;
    private Integer numberOfMajors;
    private List<String> majors = new ArrayList<>();
}
