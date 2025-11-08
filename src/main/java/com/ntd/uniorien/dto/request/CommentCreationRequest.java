package com.ntd.uniorien.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentCreationRequest {

    @NotBlank(message = "NOT_BLANK")
    String content;

    @NotBlank(message = "NOT_BLANK")
    String universityId;

//    @NotBlank(message = "NOT_BLANK")
//    String userId;


}
