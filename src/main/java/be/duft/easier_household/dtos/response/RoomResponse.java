package be.duft.easier_household.dtos.response;

import be.duft.easier_household.dl.enums.RoomType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class RoomResponse {

    private Long id;
    private String name;
    private int floor;
    private LocalDateTime createdAt;
    private Long householdId;
    private RoomType roomType;
    private Integer deviceCount;

}
