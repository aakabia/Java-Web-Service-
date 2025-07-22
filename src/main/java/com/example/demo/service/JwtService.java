package com.example.demo.service;


import io.jsonwebtoken.Claims;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.security.jwt.expiration-time}")
    private long jwtExpiration;


    // generating a token with basic claims,subject and NO extra claims
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }


    // generating a token with basic claims,subject and extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // check for token validation using username and expiration of the token
    public boolean isTokenValid(String jwtToken, UserDetails userDetails){
        final String username = extractUsername(jwtToken);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken);
    }

    // check if token expiration date is before current date
    private boolean isTokenExpired(String jwtToken){
        return extractExpiration(jwtToken).before(new Date());

    }

    // extract expiration date from claims using our extractClaim method
    private Date extractExpiration(String jwtToken){
        return extractClaim(jwtToken, Claims::getExpiration);
    }


    // extract username claim from the jwt token
    public String extractUsername(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }


    //This is a reusable function that extracts a claim from our claims.
    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver ){
        final Claims claims  = extractAllClaims(jwtToken);
        return claimsResolver.apply(claims);
    }


    //This method parses the JWT token using the signing key and extracts all claims from the token's body.
    private Claims extractAllClaims (String jwtToken){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();


    }


    // This method is responsible for generating a cryptographic signing key from a Base64-encoded secret.
    private Key getSignInKey() {
        byte[] keyByte = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyByte);

    }



}
