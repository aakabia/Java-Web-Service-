package com.example.demo.config;

import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor // for private field constructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {


        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userName;



        // check if Authorization exists
        // If the header does not exist move on to the next filter
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response); // continues request
            return;
        }


        // extract token from authHeader
        jwtToken = authHeader.substring(7);


        String tokenType = jwtService.extractClaim(jwtToken, claims -> claims.get("token_type", String.class)); // get specific claim
        String path = request.getRequestURI(); // get request URI



        // Allow refresh tokens ONLY at /refresh-token
        // Do not allow Access token on refresh token route
        // Do not use filterChain.doFilter because we handle request here
        if ("REFRESH".equals(tokenType)) {
            if (!path.equals("/refresh-token")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Only ACCESS Tokens are allowed here!");
                return;
            }

        }else if ("ACCESS".equals(tokenType)) {
            if (path.equals("/refresh-token")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Only REFRESH Tokens are allowed here!");
                return;
            }

        }else if ("VERIFY".equals(tokenType)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Verification tokens are only used to Verify!");
                return;
        }




        userName = jwtService.extractUsername(jwtToken);

        // check if username form jwt exists and check if user authenticated
        // we can check if user already authenticated by using SecurityContextHolder.getContext().getAuthentication()
        // if authentication is null user is not yet authenticated
        if(userName != null && SecurityContextHolder.getContext().getAuthentication() == null){

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);

            if(jwtService.isTokenValid(jwtToken, userDetails)){

                // object of UsernamePasswordAuthenticationToken is needed in order to update SecurityContextHolder
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()

                );

                // add extra details from the request
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)
                );


                // update our SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

        }

        // pass to next filters
        filterChain.doFilter(request, response);


    }
}
