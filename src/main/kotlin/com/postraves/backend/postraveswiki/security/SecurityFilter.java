//package com.postraves.backend.postraveswiki.security;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthException;
//import com.google.firebase.auth.FirebaseToken;
//import com.postraves.backend.postraveswiki.security.dataclass.CredentialType;
//import com.postraves.backend.postraveswiki.security.dataclass.Credentials;
//import com.postraves.backend.postraveswiki.security.dataclass.SecurityProperties;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class SecurityFilter extends OncePerRequestFilter {
//
//    private final SecurityService securityService;
//    private final SecurityProperties restSecProps;
//    private final CookieUtils cookieUtils;
//    private final SecurityProperties securityProps;
//    private final UserService userService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        verifyToken(request);
//        filterChain.doFilter(request, response);
//    }
//
//    private void verifyToken(HttpServletRequest request) {
//        String session = null;
//        FirebaseToken decodedToken = null;
//        CredentialType type = null;
//        boolean strictServerSessionEnabled = securityProps.getFirebaseProps().isEnableStrictServerSession();
//        Cookie sessionCookie = cookieUtils.getCookie("session");
//        String token = securityService.getBearerToken(request);
//        if (token == null || token.isEmpty()) {
//            log.info("Incoming token is not provided");
//        } else {
//            log.info("Incoming token is provided");
//        }
//        try {
//            if (sessionCookie != null) {
//                session = sessionCookie.getValue();
//                decodedToken = FirebaseAuth.getInstance().verifySessionCookie(session,
//                        securityProps.getFirebaseProps().isEnableCheckSessionRevoked());
//                type = CredentialType.SESSION;
//            } else if (!strictServerSessionEnabled) {
//                if (token != null && !token.equalsIgnoreCase("undefined")) {
//                    decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
//                    type = CredentialType.ID_TOKEN;
//                }
//            }
//        } catch (FirebaseAuthException e) {
//            e.printStackTrace();
//            log.error("Firebase Exception:: " + e.getLocalizedMessage());
//        }
//        UserEntity user = firebaseTokenToUserEntity(decodedToken);
//        if (user != null) {
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
//                    new Credentials(type, decodedToken, token, session), null);
//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//    }
//
//    private UserEntity firebaseTokenToUserEntity(FirebaseToken decodedToken) {
//        UserEntity userEntity = null;
//        if (decodedToken != null) {
//            String authUid = decodedToken.getUid();
//            userEntity = userService.findByAuthUidReturnEntity(authUid);
//            // user has logged in via firebase but didn't create account in our backend
//            if (userEntity == null) {
//                UserEntity userEntityWithAuthUuid = new UserEntity();
//                userEntityWithAuthUuid.setAuthUid(authUid);
//                return userEntityWithAuthUuid;
//            }
//        }
//        return userEntity;
//    }
//}