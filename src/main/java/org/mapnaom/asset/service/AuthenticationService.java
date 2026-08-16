package org.mapnaom.asset.service;

import lombok.RequiredArgsConstructor;
import org.mapnaom.asset.dto.LoginRequest;
import org.mapnaom.asset.dto.LoginResponse;
import org.mapnaom.asset.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim(), request.password())
        );
        return new LoginResponse(jwtService.generateToken((UserDetails) authentication.getPrincipal()));
    }
}
