package com.juanma.emprenglobal.security.auth;

import com.juanma.emprenglobal.model.Authority;
import com.juanma.emprenglobal.model.User;
import com.juanma.emprenglobal.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("jpa")
public class JpaEcommerceUserDao implements EcommerceUserDao{
    @Autowired
    UserRepository repository;
    @Override
    public Optional<EcommerceUser> selectApplicationUserByUsername(String username) {
        Optional<User> optionalUser = repository.findByUsername(username);
        User user = optionalUser.orElseThrow(InvalidParameterException::new);
        return Optional.of(new EcommerceUser(
                user.getUsername(),
                user.getPassword(),
                getGrantedAuthorities(user.getAuthorities()),
                true,
                true,
                true,
                true
                ));
    }

    private Set<SimpleGrantedAuthority> getGrantedAuthorities(Set<Authority> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAname()))
                .collect(Collectors.toSet());
    }
}
