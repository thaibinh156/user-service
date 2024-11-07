package com.infodation.userservice.models.dto.user;

import com.infodation.userservice.models.Sex;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateUserDTO {
    @Valid

    @NotNull(message = "User ID is required")
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "First name is required")
    @NotBlank(message = "first Name is required")
    private String firstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last Name is required")
    private String lastName;

    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotNull(message = "Sex is required")
    private Sex sex;
}
