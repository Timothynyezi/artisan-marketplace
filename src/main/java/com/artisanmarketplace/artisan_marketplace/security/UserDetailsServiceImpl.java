package com.artisanmarketplace.artisan_marketplace.security;

import com.artisanmarketplace.artisan_marketplace.user.User;
import com.artisanmarketplace.artisan_marketplace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
