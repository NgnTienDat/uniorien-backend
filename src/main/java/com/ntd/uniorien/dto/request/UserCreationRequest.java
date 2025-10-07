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
public class UserCreationRequest {

    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "NOT_BLANK")
    String email;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    String avatar;

    @Size(max = 24, message = "INVALID_NAME")
    @NotBlank(message = "NOT_BLANK")
    String fullName;
}
