package com.juanma.emprenglobal.security.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//This a second filter of JWT tokens
//Here are the Verified token Jwt's processes
public class JwtTokenVerifier  extends OncePerRequestFilter {
    private final JwtConfig jwtConfig;
    public JwtTokenVerifier(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(jwtConfig.getAuthorizationHeader());
        if(Strings.isNullOrEmpty(authorizationHeader) || !authorizationHeader.startsWith(jwtConfig.getTokenPrefix())) {
            filterChain.doFilter(request, response);     //If it's wrong then reject it.
            return;
        }
        try {
            String token = authorizationHeader.replace(jwtConfig.getTokenPrefix(), "");
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecretKeyForSigning())
                    .parseClaimsJws(token);              //Decode the Jwt token in order to check it.
            Claims body = claimsJws.getBody();

            String username = body.getSubject();         //Retrieve username from token
            List<Map<String, String >> authorities = (List<Map<String, String >>) body.get("authorities"); //Retrieve authorities from token

            Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream().
                    map(m -> new SimpleGrantedAuthority(m.get("authority")))
            .collect(Collectors.toSet());  //This is to map(convert) a List of HashMaps of Strings into a List of SimpleGrantedAuthority.

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                simpleGrantedAuthorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication); // This line is to try to authenticate
        } catch (JwtException e) {
            throw  new IllegalStateException("Token can not be trusted");
        }
        filterChain.doFilter(request, response); //Send the response to the next filter or API.
    }
}
