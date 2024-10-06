package boubyan.com.studentmanagementsystem.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private Map<String, Object> extraClaims;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "8gUPHuF2TcGTR4xNb5ZvEK6yJwL3mWqX1pDsAoC7fVkQrY9jM");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 300000L); // 5 minutes
        userDetails = new User("testuser", "password", new ArrayList<>());

        extraClaims = new HashMap<>();
        extraClaims.put("stringClaim", "test");
        extraClaims.put("integerClaim", 123);
        extraClaims.put("booleanClaim", true);
        extraClaims.put("listClaim", Arrays.asList("item1", "item2"));
    }

    @Test
    void whenGenerateToken_thenTokenIsCreated() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void whenExtractUsername_thenUsernameIsCorrect() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void whenTokenIsValid_thenReturnTrue() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void whenExtractExpiration_thenDateIsCorrect() {
        String token = jwtService.generateToken(userDetails);
        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void whenExtractCustomClaims_thenReturnCorrectClaims() {
        String token = jwtService.generateToken(extraClaims, userDetails);

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("test", claims.get("stringClaim"));
        assertEquals(123, claims.get("integerClaim"));
        assertEquals(true, claims.get("booleanClaim"));
        assertEquals(Arrays.asList("item1", "item2"), claims.get("listClaim"));
    }

    @Test
    void whenExtractClaimWithCustomFunction_thenReturnCorrectClaim() {
        String token = jwtService.generateToken(userDetails);

        String extractedUsername = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void whenGetExpirationTime_thenReturnCorrectValue() {
        long expirationTime = jwtService.getExpirationTime();
        assertEquals(300000L, expirationTime);
    }

    @Test
    void whenGenerateToken_thenExpirationIsCorrect() {
        String token = jwtService.generateToken(userDetails);

        Date expiration = jwtService.extractExpiration(token);
        long expectedExpiration = System.currentTimeMillis() + 300000L;
        assertTrue(Math.abs(expiration.getTime() - expectedExpiration) < 1000);
    }

    @Test
    void whenExtractingStringClaim_thenReturnCorrectValue() {
        String token = jwtService.generateToken(extraClaims, userDetails);
        assertEquals("test", jwtService.extractClaim(token, claims -> claims.get("stringClaim", String.class)));
    }

    @Test
    void whenExtractingIntegerClaim_thenReturnCorrectValue() {
        String token = jwtService.generateToken(extraClaims, userDetails);
        Integer extractedClaim = jwtService.extractClaim(token, claims -> claims.get("integerClaim", Integer.class));
        assertEquals(123, extractedClaim);
    }

    @Test
    void whenExtractingBooleanClaim_thenReturnCorrectValue() {
        String token = jwtService.generateToken(extraClaims, userDetails);
        Boolean extractedClaim = jwtService.extractClaim(token, claims -> claims.get("booleanClaim", Boolean.class));
        assertTrue(extractedClaim);
    }

    @Test
    void whenExtractingListClaim_thenReturnCorrectValue() {
        String token = jwtService.generateToken(extraClaims, userDetails);
        List<String> extractedClaim = jwtService.extractClaim(token, claims -> claims.get("listClaim", List.class));
        assertEquals(Arrays.asList("item1", "item2"), extractedClaim);
    }

    @Test
    void whenTokenIsInvalid_thenThrowException() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        assertThrows(SignatureException.class, () -> {
            jwtService.extractAllClaims(invalidToken);
        });
    }

    @Test
    void whenTokenIsExpired_thenThrowException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000L);  // 1 second

        String token = jwtService.generateToken(userDetails);

        Thread.sleep(1500);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractAllClaims(token);
        });
    }

    @Test
    void whenExtractingNonExistentClaim_thenReturnNull() {
        String token = jwtService.generateToken(userDetails);

        assertNull(jwtService.extractClaim(token, claims -> claims.get("nonexistent", String.class)));
    }

    @Test
    void testIsTokenExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000L);  // 1 second

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenExpired(token));

        Thread.sleep(1100);

        assertTrue(jwtService.isTokenExpired(token));
    }
}