package com.prj.chatgpt.domain.security.model.vo;

import org.apache.shiro.authc.AuthenticationToken;

public class JwtToken implements AuthenticationToken {

    private String jwt;

    public JwtToken(String jwt) {
        this.jwt = jwt;
    }

    // This is same as the account
    @Override
    public Object getPrincipal() {
        return jwt;
    }

    // This is same as the password
    @Override
    public Object getCredentials() {
        return jwt;
    }
}
