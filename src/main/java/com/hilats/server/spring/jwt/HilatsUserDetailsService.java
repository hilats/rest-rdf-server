package com.hilats.server.spring.jwt;

import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Created by pduchesne on 11/02/16.
 */
public class HilatsUserDetailsService
    implements UserDetailsService
{
    private final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();

    private HilatsUserService userService;

    public HilatsUserDetailsService(HilatsUserService userService) {
        this.userService = userService;
    }

    @Override
    public HilatsUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HilatsUser user = userService.findUser(username);
        if (user == null) return null;
        else {
            HilatsUserDetails details = new HilatsUserDetails(user);
            detailsChecker.check(details);

            return details;
        }
    }
}
