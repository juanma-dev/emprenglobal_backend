package com.juanma.emprenglobal.security.jwt;

import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

//Here we just want to inject some variables and remove them from the others three classes
@ConfigurationProperties(prefix = "application.jwt") //We'll use the application.properties file
@Component  //This is for Dependency Injection, other way is a method with @Bean.
            // There is no need to use @ComponentScan due spring boot does it by default
public class JwtConfig {
    private String secretKey;                   //"securesecuresecuresecuresecuresecuresecuresecure"
    private String tokenPrefix;                 //"Bearer "
    private Integer tokenExpirationAfterDays;   //14

    public JwtConfig() {
    }

    public String getSecretKey() {
        return secretKey;
    }
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    public String getTokenPrefix() {
        return tokenPrefix;
    }
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
    public Integer getTokenExpirationAfterDays() {
        return tokenExpirationAfterDays;
    }
    public void setTokenExpirationAfterDays(Integer tokenExpirationAfterDays) {
        this.tokenExpirationAfterDays = tokenExpirationAfterDays;
    }
    public SecretKey getSecretKeyForSigning() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    public String getAuthorizationHeader() {
        return HttpHeaders.AUTHORIZATION;    //"Authorization"
    }
}
