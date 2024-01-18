package com.prj.chatgpt.domain.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtUtil {

    // Create default keys and algorithms for no-argument constructor
    private static final String defaultBase64EncodedSecretKey = "B*B^";
    private static final SignatureAlgorithm defaultSignatureAlgorithm = SignatureAlgorithm.HS256;

    private final String base64EncodedSecretKey;
    private final SignatureAlgorithm signatureAlgorithm;

    public JwtUtil() {
        this(defaultBase64EncodedSecretKey, defaultSignatureAlgorithm);
    }

    public JwtUtil(String secretKey, SignatureAlgorithm signatureAlgorithm) {
        this.base64EncodedSecretKey = Base64.encodeBase64String(secretKey.getBytes());
        this.signatureAlgorithm = signatureAlgorithm;
    }

    /**
     * This is where the jwt string is generated
     * jwt string consists of three parts
     * 1. header
     *      -The type of the current string, usually "JWT"
     *      -Which algorithm to encrypt, "HS256" or other encryption algorithm
     *      So it is generally fixed and there is no change.
     * 2. payload
     *      Generally there are four most common standard fields (listed below)
     *      iat: issuance time, that is, when the jwt was generated
     *      jti: The unique identifier of JWT
     *      iss: issuer, usually username or userId
     *      exp: expiration time
     * */
    // iss issuer, ttlMillis survival time, claims refer to some non-private information store in jwt
    public String encode(String issuer, long ttlMillis, Map<String, Object> claims){
        /**
         * String issuer: The issuer identifier, usually referring to the entity creating the JWT (like a username or user ID).
         * long ttlMillis: The token's time-to-live in milliseconds. This is how long the token will be valid before it expires.
         * Map<String, Object> claims: The claims to be included in the token. Claims are pieces of information stored in the token,
         * such as user roles or other relevant data.
         */
        /**
         * If no claims are provided when the method is called, a new empty HashMap is created.
         * The current time in milliseconds is obtained, which will be used as the token's issue time.
         */
        if(claims==null){
            claims = new HashMap<>();
        }
        long nowMillis = System.currentTimeMillis();

        /**
         * Starts building the JWT using Jwts.builder().
         * .setClaims(claims): Sets the token's claims.
         * .setId(UUID.randomUUID().toString()): Sets a unique identifier for the JWT (using UUID).
         * .setIssuedAt(new Date(nowMillis)): Sets the token's issue time.
         * .setSubject(issuer): Sets the token's subject, i.e., the issuer.
         * .signWith(signatureAlgorithm, base64EncodedSecretKey): Sets the signature algorithm and key, used to verify the token's integrity.
         */
        JwtBuilder builder = Jwts.builder()
                // load part
                .setClaims(claims)
                // This is the unique identifier of JWT. It is generally set to be unique. This method can generate a unique identifier.
                .setId(UUID.randomUUID().toString())//2.
                //Issue time
                .setIssuedAt(new Date(nowMillis))
                // The issuer, that is, who the JWT is for (logically it is usually username or userId)
                .setSubject(issuer)
                .signWith(signatureAlgorithm, base64EncodedSecretKey);//This place is the algorithm and secret key used to generate jwt

        /**
         * If a valid time-to-live (ttlMillis >= 0) is provided, calculates and sets the expiration time.
         * The expiration time is the current time plus the ttl.
         */
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);//4. Expiration time, this is also generated using milliseconds, using the current time + the previously passed duration.
            builder.setExpiration(exp);
        }

        return builder.compact();
    }

    // The reverse operation of encoding, pass in jwtToken to generate the corresponding username and password, etc. content.
    // Claim is a map to store all key-value pairs.
    public Claims decode(String jwtToken) {
        // Get DefaultJwtParser
        return Jwts.parser()
                // Set the signature key
                .setSigningKey(base64EncodedSecretKey)
                // Set the jwt that needs to be parsed
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    //Check if the jwtToken is valid
    public boolean isVerify(String jwtToken) {
        // This is only one official verification algorithm written here.
        // You can add it yourself later....
        Algorithm algorithm = null;
        switch (signatureAlgorithm) {
            case HS256:
                algorithm = Algorithm.HMAC256(Base64.decodeBase64(base64EncodedSecretKey));
                break;
            default:
                throw new RuntimeException("不支持该算法");
        }
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(jwtToken);
        // If the verification fails, an exception will be thrown.
        // Criteria for judging legality: 1. The head and load parts have not been tampered with. 2. Not expired
        return true;
    }
}
