package com.example.solidconnection.custom.security.authentication;


import com.example.solidconnection.custom.security.userdetails.SiteUserDetails;

public class SiteUserAuthentication extends JwtAuthentication {

    public SiteUserAuthentication(String token) {
        super(token, null);
        setAuthenticated(false);
    }

    public SiteUserAuthentication(String token, SiteUserDetails principal) {
        super(token, principal);
        setAuthenticated(true);
    }
}
