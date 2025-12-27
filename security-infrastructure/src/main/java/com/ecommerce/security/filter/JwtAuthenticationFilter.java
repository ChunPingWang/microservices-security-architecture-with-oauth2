package com.ecommerce.security.filter;

import com.ecommerce.security.context.CurrentUserContext;
import com.ecommerce.security.jwt.JwtTokenProvider;
import com.ecommerce.security.jwt.JwtTokenProvider.TokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT Authentication Filter that validates JWT tokens in incoming requests.
 * Extracts token from Authorization header and sets up Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserContext currentUserContext;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                    CurrentUserContext currentUserContext) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.currentUserContext = currentUserContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        try {
            extractToken(request).ifPresent(token -> authenticateToken(token, request));
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     */
    private Optional<String> extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return Optional.of(bearerToken.substring(BEARER_PREFIX.length()));
        }

        return Optional.empty();
    }

    /**
     * Validate token and set up authentication context.
     */
    private void authenticateToken(String token, HttpServletRequest request) {
        Optional<TokenClaims> claimsOpt = jwtTokenProvider.validateToken(token);

        if (claimsOpt.isEmpty()) {
            log.debug("Invalid or expired token");
            return;
        }

        TokenClaims claims = claimsOpt.get();

        // Only accept ACCESS tokens or SERVICE tokens for API calls
        if (!claims.isAccessToken() && !claims.isServiceToken()) {
            log.debug("Token type not acceptable for API access: {}", claims.tokenType());
            return;
        }

        // Set up CurrentUserContext
        currentUserContext.setUser(claims.userId(), claims.email(), claims.role());

        // Create Spring Security Authentication
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + claims.role())
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(claims.userId(), null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authenticated user: {} with role: {}", claims.userId(), claims.role());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for certain paths if needed
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }
}
