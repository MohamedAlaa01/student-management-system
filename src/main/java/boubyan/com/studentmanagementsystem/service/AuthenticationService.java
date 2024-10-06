package boubyan.com.studentmanagementsystem.service;

import boubyan.com.studentmanagementsystem.dto.LoginRequest;
import boubyan.com.studentmanagementsystem.exception.UniqueConstraintViolationException;
import boubyan.com.studentmanagementsystem.model.SecurityUser;
import boubyan.com.studentmanagementsystem.model.UserProfile;
import boubyan.com.studentmanagementsystem.repository.UserRepository;
import boubyan.com.studentmanagementsystem.dto.RegisterRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfile signup(RegisterRequest input) {
        UserProfile user = new UserProfile();
        user.setEmail(input.getEmail());
        user.setUsername(input.getUsername());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setDateOfBirth(input.getDateOfBirth());
        user.setPhoneNumber(input.getPhoneNumber());
        user.setAddress(input.getAddress());
        user.setRoles(Collections.singletonList("USER"));

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new UniqueConstraintViolationException("A student with this username already exists.");
            }
            throw e;
        }
    }


    public SecurityUser authenticate(LoginRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        UserProfile userProfile = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new SecurityUser(userProfile);
    }
}