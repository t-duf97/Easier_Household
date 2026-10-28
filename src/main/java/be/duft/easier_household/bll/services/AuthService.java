package be.duft.easier_household.bll.services;

import be.duft.easier_household.dl.repositories.UserRepository;
import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.api.dtos.request.LoginRequest;
import be.duft.easier_household.api.dtos.request.RegisterRequest;
import be.duft.easier_household.api.dtos.response.AuthResponse;
import be.duft.easier_household.bll.exceptions.InvalidCredentialsException;
import be.duft.easier_household.bll.exceptions.ResourceAlreadyExistsException;
import be.duft.easier_household.api.security.JwtService;
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
    private volatile String dummyHash;

    public AuthResponse register(RegisterRequest request) {
        // Vérifier que l'email est pas déjà utilisé
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already in use: " + request.getEmail());
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

    /**
     * @throws InvalidCredentialsException si l'email est inconnu ou le mot de passe faux (même réponse, et un
     *         hachage BCrypt est calculé dans les deux cas pour que le temps de réponse ne trahisse pas l'email)
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        String hash = user != null ? user.getPasswordHash() : dummyHash();

        if (!passwordEncoder.matches(request.getPassword(), hash) || user == null) {
            throw new InvalidCredentialsException();
        }
        return buildAuthResponse(user);
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode("not-a-real-password");
        }
        return dummyHash;
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .tokenType("Bearer")
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
