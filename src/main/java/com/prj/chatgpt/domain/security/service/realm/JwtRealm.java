package com.prj.chatgpt.domain.security.service.realm;

import com.prj.chatgpt.domain.security.model.vo.JwtToken;
import com.prj.chatgpt.domain.security.service.JwtUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Custom Realm for JWT-based authentication using Shiro
public class JwtRealm extends AuthorizingRealm {

    // Logger for logging information
    private Logger logger = LoggerFactory.getLogger(JwtRealm.class);

    // Utility class instance for JWT operations
    private static JwtUtil jwtUtil = new JwtUtil();

    // Overriding the supports method to define the type of token this realm supports
    @Override
    public boolean supports(AuthenticationToken token){
        // This realm only supports JwtToken type
        return token instanceof JwtToken;
    }

    // Method for retrieving authorization information (not implemented in this case)
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // Not required for JWT authentication, returning null
        return null; //This method is no need to implement temporary
    }

    // Method for retrieving authentication information
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // Extract JWT string from the token
        String jwt = (String)token.getPrincipal();
        if (jwt == null) {
            throw new NullPointerException("jwtToken cannot be empty!");
        }
        // Validate the extracted JWT
        if (!jwtUtil.isVerify(jwt)) {
            throw new UnknownAccountException();
        }
        // Decode JWT to extract username information and do some log operations
        String username = (String)jwtUtil.decode(jwt).get("username");
        logger.info("Authentication username: {}", username);

        // Return authentication information based on JWT
        return new SimpleAuthenticationInfo(jwt, jwt, "JwtRealm");
    }
}
