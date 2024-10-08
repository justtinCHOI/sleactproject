package com.unitekndt.mqnavigator.security.handler;

import com.google.gson.Gson;
import com.unitekndt.mqnavigator.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Log4j2 // Config 에서 따로 bean 추가하기에 굳이 따로 빈등록 할 필요 없다.
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        //client 가 로그인 성공 -> authentication 생성 -> accessToken, refreshToken, 맴버정보 반환.

        //authentication -> memberDTO -> claims -> (accessToken, refreshToken) -> (gson) jsonStr -> jsonStr

        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();//인증정보로부터 memberDTO 를 추출

        Map<String, Object> claims = memberDTO.getClaims();

        try {
            String accessToken = JWTUtil.generateToken(claims, 10);
            String refreshToken = JWTUtil.generateToken(claims, 60 * 24);

            claims.put("accessToken", accessToken);
            claims.put("refreshToken", refreshToken);

            Gson gson = new Gson();
            String jsonStr = gson.toJson(claims);

            response.setContentType("application/json; charset=UTF-8");
            PrintWriter printWriter = response.getWriter();
            printWriter.println(jsonStr);
            printWriter.close();
        } catch (Exception e) {
            log.error("Error generating tokens: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token generation failed");
        }
    }

}