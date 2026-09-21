package com.datapilot.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    private SecurityUtils(){

    }

    public static LoginUser getCurrentUser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null){
            return null;
        }
        Object principal=authentication.getPrincipal();
        if(principal instanceof LoginUser loginUser){
            return loginUser;
        }
        return null;
    }

    public static Long getCurrentId(){
        LoginUser loginUser=getCurrentUser();

        return loginUser==null ? null : loginUser.getUserId();
    }

    public static  String getCurrentUsername(){
        LoginUser loginUser=getCurrentUser();

        return loginUser==null ? null: loginUser.getUsername();
    }
}
