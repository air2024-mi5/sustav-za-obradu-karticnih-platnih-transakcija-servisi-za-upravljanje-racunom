package foi.air.szokpt.accountmng.services;

import foi.air.szokpt.accountmng.entitites.User;
import foi.air.szokpt.accountmng.exceptions.AuthenticationException;
import foi.air.szokpt.accountmng.repositories.UserRepository;
import foi.air.szokpt.accountmng.util.JwtUtil;
import foi.air.szokpt.accountmng.util.hashing.Hasher;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final Hasher hasher;
    private final JwtUtil jwtUtil;

    public AuthenticationService(UserRepository userRepository, Hasher hasher, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.hasher = hasher;
        this.jwtUtil = jwtUtil;
    }

    public String authenticate(String username, String password) {
        User registeredUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!hasher.verifyHash(password, registeredUser.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        if (registeredUser.isBlocked()) {
            throw new AuthenticationException("User is blocked");
        }

        return jwtUtil.generateToken(username, registeredUser.getRole().getName());
    }
}
