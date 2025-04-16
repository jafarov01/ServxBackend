package com.servx.servx.util;

import com.servx.servx.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // Return email as username
    }

    public Long getId() {
        return user.getId();
    }

    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // Return password for authentication
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Adjust based on your logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Adjust based on your logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Adjust based on your logic
    }

    @Override
    public boolean isEnabled() {
        return user.isVerified();  // Ensure user is verified
    }

    public User getUser() {
        return user;
    }
}