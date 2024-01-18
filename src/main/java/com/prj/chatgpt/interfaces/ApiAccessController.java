package com.prj.chatgpt.interfaces;

import com.prj.chatgpt.domain.security.service.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Decription API access access management: when accessing the OpenAI interface, access verification is required.
 * Rest controller for handling API access and authorization.
 * This controller manages access to the OpenAI interface and validates credentials.
 */
@RestController
public class ApiAccessController {

    private Logger logger = LoggerFactory.getLogger(ApiAccessController.class);

    /**
     * Endpoint for authorizing users and generating JWT token.
     * Simulates user authentication and provides a JWT token upon successful authentication.
     * Usage:
     * 1. Local access: http://localhost:8080/authorize?username=test&password=123
     * 2. Cloud server access(ESC public net ip): http://139.9.209.43:8080/authorize?username=test&password=123
     * 3. Intranet penetrate access(Cpolar ip): http://chatgpt-wechat.cpolar.top/authorize?username=test&password=123
     * @param username the username of the user
     * @param password the password of the user
     * @return ResponseEntity containing a map with authorization message and JWT token if successful.
     */
    @RequestMapping("/authorize")
    public ResponseEntity<Map<String, String> > authorize(String username, String password){
        Map<String, String> map = new HashMap<>();
        //Simulate the verification of account and password
        if (!"test".equals(username) || !"123".equals(password)) {
            map.put("msg", "Wrong user name or password!");
            return ResponseEntity.ok(map);
        }
        //The verification passed, then generate the token:
        JwtUtil jwtUtil = new JwtUtil();
        Map<String, Object> chaim = new HashMap<>();
        chaim.put("username", username);
        String jwtToken = jwtUtil.encode(username, 60 * 60 * 1000, chaim);
        map.put("msg", "Authorized successful!");
        map.put("token", jwtToken);
        //Return the token code
        return ResponseEntity.ok(map);
    }

    /**
     * Usage(Smae as before): http://localhost:8080/verify?token=
     */
     @RequestMapping("/verify")
     public ResponseEntity<String> verify(String token) {
         logger.info("Verified token：{}", token);
         return ResponseEntity.status(HttpStatus.OK).body("Verified successful!");
     }

    @RequestMapping("/success")
    public String success(){
        return "Test success by Shucun Zhao.";
    }

}
