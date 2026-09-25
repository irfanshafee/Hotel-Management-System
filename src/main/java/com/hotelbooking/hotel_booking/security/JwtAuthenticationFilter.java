package com.hotelbooking.hotel_booking.security;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements Filter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OpenApiRegistry openApiRegistry;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository,
                                   ObjectMapper objectMapper,
                                   OpenApiRegistry openApiRegistry) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.openApiRegistry = openApiRegistry;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest request)
                || !(servletResponse instanceof HttpServletResponse response)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!requiresAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)
                || authorizationHeader.length() == BEARER_PREFIX.length()) {
            writeUnauthorized(response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            writeUnauthorized(response);
            return;
        }

        try {
            String email = jwtService.validateAndExtractEmail(token);
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                writeUnauthorized(response);
                return;
            }

            request.setAttribute(
                    AuthenticatedUser.REQUEST_ATTRIBUTE,
                    new AuthenticatedUser(
                            user.getId(), user.getName(), user.getEmail(), user.getRole()));
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            writeUnauthorized(response);
        }
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (openApiRegistry.isPublic(method, path)) {
            return false;
        }
        return path.startsWith("/api/");
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                new ApiResponse<Void>(HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required", null));
    }
}
