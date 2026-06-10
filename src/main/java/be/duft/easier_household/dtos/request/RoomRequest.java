package be.duft.easier_household.dtos.request;

import be.duft.easier_household.dl.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomRequest {


    private String name;

    @NotNull
    private RoomType roomType;

    private Integer floor;



}
