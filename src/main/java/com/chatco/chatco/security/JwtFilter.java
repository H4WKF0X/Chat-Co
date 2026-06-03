// security/JwtFilter.java
package com.chatco.chatco.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
/**
 * Reads JWT bearer tokens from API requests and puts the username into the
 * Spring Security context.
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserAuthorityService userAuthorityService;

    public JwtFilter(JwtUtil jwtUtil, UserAuthorityService userAuthorityService) {
        this.jwtUtil = jwtUtil;
        this.userAuthorityService = userAuthorityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.isValid(token)) {
                String username = jwtUtil.extractUsername(token);
                // The principal is only the username because controllers use it
                // to load the full AppUser from the database when needed.
                var auth = new UsernamePasswordAuthenticationToken(
                        username, null, userAuthorityService.authoritiesForUsername(username)
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
