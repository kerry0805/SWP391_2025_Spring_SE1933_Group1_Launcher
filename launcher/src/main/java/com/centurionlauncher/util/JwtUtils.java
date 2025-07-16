package com.centurionlauncher.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtUtils {
    public static String getUserName(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            System.out.println(jwt.getClaim("username").asString());
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException exception) {
            return null;
        }
    }

    public static Long getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            System.out.println(jwt.getClaim("userId").asLong());
            return jwt.getClaim("userId").asLong();
        } catch (JWTDecodeException exception) {
            return null;
        }
    }

    public static String getRole(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            System.out.println(jwt.getClaim("role").asString());
            return jwt.getClaim("role").asString();
        } catch (JWTDecodeException exception) {
            return null;
        }
    }

    public static String getAvatarUrl(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            System.out.println(jwt.getClaim("avatarUrl").asString());
            return jwt.getClaim("avatarUrl").asString();
        } catch (JWTDecodeException exception) {
            return null;
        }
    }

}
