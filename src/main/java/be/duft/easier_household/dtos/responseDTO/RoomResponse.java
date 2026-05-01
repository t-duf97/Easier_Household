package be.duft.easier_household.dtos.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class RoomResponse {

    private Long id;
    private String name;
    private int floor;
    private LocalDateTime created_at;
    private Long householdId;

}
