package com.datapilot.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider=jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )throws ServletException, IOException{
        String authorization = request.getHeader("Authorization");

        if(authorization==null || !authorization.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        try{
            String token = authorization.substring(7);

            Claims claims= jwtTokenProvider.parseToken(token);

            Number userIdNumber=claims.get("userId", Number.class);
            Long userId=userIdNumber.longValue();

            String username=claims.get("username",String.class);

            LoginUser loginUser=new LoginUser(userId,username);

            UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(
                    loginUser,
                    null,
                    Collections.emptyList()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch (JwtException | IllegalArgumentException e){
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request,response);

    }
}
