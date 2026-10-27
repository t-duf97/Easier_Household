package be.duft.easier_household.bll.services;

import be.duft.easier_household.dl.repositories.UserRepository;
import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found."));
    }

}
