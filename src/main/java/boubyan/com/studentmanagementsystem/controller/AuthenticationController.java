package boubyan.com.studentmanagementsystem.controller;

import boubyan.com.studentmanagementsystem.dto.AuthenticationResponse;
import boubyan.com.studentmanagementsystem.dto.LoginRequest;
import boubyan.com.studentmanagementsystem.dto.RegisterRequest;
import boubyan.com.studentmanagementsystem.model.SecurityUser;
import boubyan.com.studentmanagementsystem.model.UserProfile;
import boubyan.com.studentmanagementsystem.service.AuthenticationService;
import boubyan.com.studentmanagementsystem.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody  RegisterRequest registerUserDto) {
        UserProfile registeredUser = authenticationService.signup(registerUserDto);
        SecurityUser securityUser = new SecurityUser(registeredUser);
        String jwtToken = jwtService.generateToken(securityUser);

        AuthenticationResponse response = new AuthenticationResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody LoginRequest loginUserDto) {
        SecurityUser authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        AuthenticationResponse response = new AuthenticationResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(response);
    }

}