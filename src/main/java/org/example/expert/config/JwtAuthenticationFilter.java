package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 1. 로그인 / 회원가입은 토큰 없이도 접근 가능
        if (requestUri.startsWith("/auth/signup") || requestUri.startsWith("/auth/signin")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Authorization 헤더 확인
        String bearerJwt = request.getHeader("Authorization");

        // 헤더가 없으면 여기서 바로 막지 않고 다음으로 넘긴다.
        // 실제 차단은 SecurityConfig의 authenticated()가 담당한다.
        if (bearerJwt == null || bearerJwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Bearer 제거해서 순수 JWT 추출
            String jwt = jwtUtil.substringToken(bearerJwt);

            // 4. claims 추출 및 유효성 검증
            Claims claims = jwtUtil.extractClaims(jwt);

            Long userId = jwtUtil.getUserId(claims);
            String email = jwtUtil.getEmail(claims);
            UserRole userRole = jwtUtil.getUserRole(claims);

            // 5. UserDetails 생성
            MyUserDetails myUserDetails = new MyUserDetails(userId, email, userRole);

            // 7. Authentication 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            myUserDetails,
                            null,
                            myUserDetails.getAuthorities()
                    );
            // 8. SecurityContextHolder 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 9. 다음 필터로 진행
            filterChain.doFilter(request, response);

        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않은 JWT 서명입니다.", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰입니다.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 JWT 토큰입니다.");
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 서버 오류", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}