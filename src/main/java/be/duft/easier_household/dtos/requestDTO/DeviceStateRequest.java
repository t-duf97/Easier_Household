package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeviceStateRequest {

    // Il s'agit de l'historique, je risque d'avoir aucune requete user.
    // Je vais quand meme utiliser le request/response et les mappers.

    @NotNull
    private DeviceStatus deviceStatus;

    private String value;

    private LocalDateTime changedAt;

}
