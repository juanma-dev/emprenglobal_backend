package com.juanma.emprenglobal.security;

import com.juanma.emprenglobal.security.auth.EcommerceUserService;
import com.juanma.emprenglobal.security.jwt.JwtConfig;
import com.juanma.emprenglobal.security.jwt.JwtTokenVerifier;
import com.juanma.emprenglobal.security.jwt.JwtUsernameAndPasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //This is the second way
public class EcommerceSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final EcommerceUserService ecommerceUserService;
    private final JwtConfig jwtConfig; //This is a class for just inject some variables in jwt package classes, check it

    @Autowired //We put the @Autowired on the constructor instead of the field cause it has the key final.
    public EcommerceSecurityConfig(PasswordEncoder passwordEncoder,
                                     EcommerceUserService ecommerceUserService,
                                     JwtConfig jwtConfig) {
        this.passwordEncoder = passwordEncoder;
        this.ecommerceUserService = ecommerceUserService;
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        // Same as above but allowing some access
        http
            //.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()  // *(Explained on the bottom)
                .cors().configurationSource(request -> {
                    var cors = new CorsConfiguration();
                    cors.setAllowedOrigins(List.of("*"));
                    cors.setAllowedMethods(List.of("GET","POST", "PUT", "DELETE", "OPTIONS"));
                    cors.setAllowedHeaders(List.of("*"));
                    cors.setExposedHeaders(List.of("Authorization"));
                    return cors; })
                .and()
                .csrf().disable()
                .sessionManagement()  //Auth option (3) using JWT tokens(It ends in addFilterAfter(...)).
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //JWT is stateless(isn't saved)
                .and()
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfig))
                .addFilterAfter(new JwtTokenVerifier(jwtConfig), JwtUsernameAndPasswordAuthenticationFilter.class) //A 2nd Filter
                .authorizeRequests()
                // I created a src/main/resources/static/index.html file to prompt with "/"
                .antMatchers(
                        "/",
                        "index",
                        "/login",
                        "/emprenglobal/{.*s}",
                        "/emprenglobal/{[A-Z][a-z]+}/{\\d+}/*",
                        "/emprenglobal/stuffs/*"
                ).permitAll()
                .antMatchers("/api/**").hasRole("USER") //This is new here. It's used when create all Enums, roles and users.
                .anyRequest()
                .authenticated();
    }

    /* Users option 2: This way is in order to use a Data Base.
     * 1- Create an auth package, inside create:
     *  1.1 An ApplicationUser class which implements UserDetails.
     *  1.2 An ApplicationUserService class which implements UserDetailsService
     *    which returns user EcommerceUser through ApplicationUserDao
     *  1.3 An ApplicationUserDao interface with a method selectApplicationUserByUsername
     *     which will interact with a DB.
     *  1.4 A Class that implements ApplicationUserDao interface.
     *  1.5 Here implement two methods (they are bellow):
     *      -  protected void configure(AuthenticationManagerBuilder auth)
     *      -  protected void configure(AuthenticationManagerBuilder auth)
     * */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(ecommerceUserService);
        return provider;
    }
}