package boubyan.com.studentmanagementsystem.service;

import boubyan.com.studentmanagementsystem.dto.LoginRequest;
import boubyan.com.studentmanagementsystem.dto.RegisterRequest;
import boubyan.com.studentmanagementsystem.model.SecurityUser;
import boubyan.com.studentmanagementsystem.model.UserProfile;
import boubyan.com.studentmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignup_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("password");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setPhoneNumber("1234567890");
        request.setAddress("123 Test Street");

        UserProfile user = new UserProfile();
        user.setId(1L);

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(UserProfile.class))).thenReturn(user);

        UserProfile result = authenticationService.signup(request);

        // Ensure user is saved with encoded password
        verify(userRepository, times(1)).save(any(UserProfile.class));

        // Assert correct user is returned
        assertEquals(user.getId(), result.getId());
    }
    @Test
    void testSignup_PasswordIsEncoded() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password");

        authenticationService.signup(request);

        // Ensure password is encoded
        verify(passwordEncoder, times(1)).encode(request.getPassword());
    }

    @Test
    void testSignup_SaveUserCalled() {
        RegisterRequest request = new RegisterRequest();

        authenticationService.signup(request);

        // Ensure repository save method is called once
        verify(userRepository, times(1)).save(any(UserProfile.class));
    }
    @Test
    void testAuthenticate_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        UserProfile user = new UserProfile();
        user.setUsername("testuser");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));

        SecurityUser result = authenticationService.authenticate(request);

        // Assert user is authenticated and returned
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown_user");
        request.setPassword("password");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        // Assert that the exception is thrown when the user is not found
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(request));
    }
    @Test
    void testAuthenticate_FindByUsernameCalled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");

        // Mocking the UserProfile object to be returned by findByUsername
        UserProfile mockUser = new UserProfile();
        mockUser.setUsername("testuser");

        // Mock the behavior of userRepository to return a user profile when called
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(mockUser));

        // Call the authenticate method
        authenticationService.authenticate(request);

        // Ensure findByUsername method is called
        verify(userRepository, times(1)).findByUsername(request.getUsername());
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong_password");

        // Simulate failed authentication (exception thrown by AuthenticationManager)
        doThrow(new RuntimeException("Authentication failed"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Expect an exception due to failed authentication
        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(request));
    }

}
