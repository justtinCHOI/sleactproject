package com.unitekndt.mqnavigator.security.filter;

import com.google.gson.Gson;
import com.unitekndt.mqnavigator.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {
    // OncePerRequestFilter : spring security 가 여러가지 필터를 제공하지만 모든 경우에 체크하는 필터

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        //api/member/ 경로의 호출은 체크하지 않음
        if (path.startsWith("/api/member/login")) {
            return true;
        }

        return path.startsWith("/stock/item/entire/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //JWT 를 검증하고, 유효한 경우 SecurityContext 에 인증 정보를 설정합니다.
        //header 에 authorization 이 있는데 type 은 bearer 를 표준으로 쓴다.

        //1. Header ->  토큰 검사 -> claims 2. claims -> MemberDTO 3.

        String authHeaderStr = request.getHeader("Authorization");

        if (authHeaderStr == null || !authHeaderStr.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //"Bearer {JWT 문자열}"
            String accessToken = authHeaderStr.substring(7); // 7개 문자 잘라내기
            Map<String, Object> claims = JWTUtil.validateToken(accessToken); //유효한지 검사
            log.info("claims {}", claims);

            //SpringSecurityHolderContext 에다가 Member 정보를 넣어줘야한다.
            //authorization 성공시 -> MemberDTO 정보를 얻어낼 수 있다.

            long memberId = ((Number) claims.get("memberId")).longValue();
            String email = (String) claims.get("email");
            String name = (String) claims.get("name");
            String nickname = (String) claims.get("nickname");
            String password = (String) claims.get("password");
            List<Cash> cashList = (List<Cash>) claims.get("cashList");
            Boolean social = (Boolean) claims.get("social");
            List<String> roleNames = (List<String>) claims.get("roleNames");
            String status = (String) claims.get("status");

            MemberDTO memberDTO = new MemberDTO(
                    memberId, email, name, nickname, password, cashList, social, roleNames, status
            );

            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(memberDTO, password, memberDTO.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT Check Error..............");
            log.error(e.getMessage());

            Gson gson = new Gson();
            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));

            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
        }
    }
}
