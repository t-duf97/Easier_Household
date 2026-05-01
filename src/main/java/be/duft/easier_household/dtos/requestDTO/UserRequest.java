package be.duft.easier_household.dtos.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotNull
    private String username;

    @Email @NotNull
    private String email;

    @NotBlank @NotNull
    private String passwordHash;

}
