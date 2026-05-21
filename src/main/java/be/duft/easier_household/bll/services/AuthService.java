package be.duft.easier_household.bll.services;

import be.duft.easier_household.dal.repositories.UserRepository;
import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.dtos.request.LoginRequest;
import be.duft.easier_household.dtos.request.RegisterRequest;
import be.duft.easier_household.dtos.response.AuthResponse;
import be.duft.easier_household.exceptions.EmailAlreadyUsedException;
import be.duft.easier_household.exceptions.ResourceNotFoundException;
import be.duft.easier_household.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.security.JwtService;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        // Vérifier que l'email est pas déjà utilisé
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("Email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedAccessException("Wrong password");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .build();
    }
}
