package com.ecommerce.security.filter;

import com.ecommerce.security.context.CurrentUserContext;
import com.ecommerce.security.jwt.JwtProperties;
import com.ecommerce.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtTokenProvider jwtTokenProvider;
    private CurrentUserContext currentUserContext;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("this-is-a-very-long-secret-key-for-testing-purposes-only-256-bits");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        currentUserContext = new CurrentUserContext();
        filter = new JwtAuthenticationFilter(jwtTokenProvider, currentUserContext);

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("With valid access token")
    class WithValidAccessTokenTests {

        @Test
        @DisplayName("should authenticate user from access token")
        void shouldAuthenticateUserFromAccessToken() throws ServletException, IOException {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(currentUserContext.isAuthenticated()).isTrue();
            assertThat(currentUserContext.getUserId()).contains("user-123");
            assertThat(currentUserContext.getEmail()).contains("test@example.com");
            assertThat(currentUserContext.getRole()).contains("CUSTOMER");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }

        @Test
        @DisplayName("should set security context with correct authorities")
        void shouldSetSecurityContextWithCorrectAuthorities() throws ServletException, IOException {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "ADMIN");
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
    }

    @Nested
    @DisplayName("With valid service token")
    class WithValidServiceTokenTests {

        @Test
        @DisplayName("should authenticate service from service token")
        void shouldAuthenticateServiceFromServiceToken() throws ServletException, IOException {
            String token = jwtTokenProvider.generateServiceToken("order-service");
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(currentUserContext.isAuthenticated()).isTrue();
            assertThat(currentUserContext.isServiceAccount()).isTrue();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
        }
    }

    @Nested
    @DisplayName("Without token")
    class WithoutTokenTests {

        @Test
        @DisplayName("should not authenticate without authorization header")
        void shouldNotAuthenticateWithoutAuthorizationHeader() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(currentUserContext.isAuthenticated()).isFalse();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("should not authenticate with empty bearer token")
        void shouldNotAuthenticateWithEmptyBearerToken() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer ");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(currentUserContext.isAuthenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("With invalid token")
    class WithInvalidTokenTests {

        @Test
        @DisplayName("should not authenticate with invalid token")
        void shouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid.token.here");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(currentUserContext.isAuthenticated()).isFalse();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("should not authenticate with non-Bearer token")
        void shouldNotAuthenticateWithNonBearerToken() throws ServletException, IOException {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(currentUserContext.isAuthenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("With refresh token")
    class WithRefreshTokenTests {

        @Test
        @DisplayName("should not authenticate with refresh token for API calls")
        void shouldNotAuthenticateWithRefreshTokenForApiCalls() throws ServletException, IOException {
            String token = jwtTokenProvider.generateRefreshToken("user-123", "test@example.com", "CUSTOMER");
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(currentUserContext.isAuthenticated()).isFalse();
        }
    }

    @Test
    @DisplayName("should skip filter for health endpoint")
    void shouldSkipFilterForHealthEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        boolean shouldNotFilter = filter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isTrue();
    }
}
