package com.juanma.emprenglobal.security.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EcommerceUserService implements UserDetailsService {
    @Autowired
    @Qualifier("jpa")
    EcommerceUserDao ecommerceUserDao;

    @Override
    public EcommerceUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return ecommerceUserDao.selectApplicationUserByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(String.format("Username %s not found", username)));
    }
}
