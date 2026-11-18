package com.helixion.sarkariPariksha.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String logId = request.getHeader("X-Log-Id");
        if (!StringUtils.hasText(logId)) {
            logId = UUID.randomUUID().toString();
        }

        MDC.put("logId", logId);
        request.setAttribute("logId", logId);
        response.setHeader("X-Log-Id", logId);

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateToken(jwt)) {
                String mobileNumber = jwtUtils.getMobileNumberFromToken(jwt);
                Long userId = jwtUtils.getUserIdFromToken(jwt);
                Long candidateId = jwtUtils.getCandidateIdFromToken(jwt);
                String role = jwtUtils.getRoleFromToken(jwt);

                if (mobileNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(mobileNumber);

                    if (userDetails != null && userDetails.isEnabled()) {
                        if (userDetails instanceof CustomUserDetails customUserDetails) {
                            if (userId == null) {
                                userId = customUserDetails.getUserId();
                            }
                            if (candidateId == null) {
                                candidateId = customUserDetails.getCandidateId();
                            }
                        }

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authenticated user: {} with authorities: {}", mobileNumber, userDetails.getAuthorities());
                    }
                }

                LoginUser loginUser = LoginUser.builder()
                        .userId(userId)
                        .candidateId(candidateId)
                        .logId(logId)
                        .mobileNumber(mobileNumber)
                        .role(role)
                        .build();

                LoginUserContext.setLoginUser(loginUser);
                request.setAttribute("loginUser", loginUser);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Cannot set user authentication in SecurityContext: {}", e.getMessage());
            filterChain.doFilter(request, response);
        } finally {
            LoginUserContext.clear();
            MDC.remove("logId");
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
