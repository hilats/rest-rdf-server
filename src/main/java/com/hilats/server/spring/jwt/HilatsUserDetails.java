package com.hilats.server.spring.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Created by pduchesne on 11/02/16.
 */
public class HilatsUserDetails
    implements UserDetails
{
    private HilatsUser user;

    public HilatsUserDetails(HilatsUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(user.roles.toArray(new String[] {}));
    }

    @Override
    public String getPassword() {
        return user.password;
    }

    @Override
    public String getUsername() {
        return user.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public HilatsUser getUser() {
        return user;
    }
}
