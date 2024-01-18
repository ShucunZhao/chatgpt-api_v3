package com.prj.chatgpt.domain.security.service;

import com.prj.chatgpt.domain.security.model.vo.JwtToken;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter extends AccessControlFilter {

    // Define logger object for log
    private Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    /**
     * If authentication failed, return 401 status code by default.
     */
    private void onLoginFail(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("Auth Err!");
    }

    /**
     * 'isAccessAllowed' determines if it carries the valid JwtToken
     * Here let it return 'false' directly to make it do the onAccessDenied method
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    /**
     * If return true means the login step pass
     * @param servletRequest
     * @param servletResponse
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //If the token you set was put in header, you can get by this way:
        //request.getHeader("Authorization");
        JwtToken jwtToken = new JwtToken(request.getParameter("token"));
        //Authentication step:
        try{
            getSubject(servletRequest, servletResponse).login(jwtToken);
            return true;
        }catch (Exception e){
            logger.error("Authentication failure", e);
            onLoginFail(servletResponse);
            return false;
        }

    }
}
