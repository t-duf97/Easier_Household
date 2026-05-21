package be.duft.easier_household.mappers;

import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.dtos.request.UserRequest;
import be.duft.easier_household.dtos.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapper {

    public UserResponse fromEntitytoResponse(User user, Set<Household> households) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        if (user.getUpdatedAt() != null)
        {response.setUpdatedAt(user.getUpdatedAt());}

        response.setUsername(user.getUsername());
        response.setHouseholds(households);
        return response;
    }

    public User fromRequesttoEntity(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPasswordHash(userRequest.getPasswordHash());
        return user;
    }
}
