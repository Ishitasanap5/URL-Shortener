package com.ishita.urlshortener.filter;

import com.ishita.urlshortener.shortener.service.RateLimiterService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimiterService rateLimiterService;

    @Override
    public void doFilter(

            ServletRequest request,

            ServletResponse response,

            FilterChain chain

    ) throws IOException, ServletException {

        HttpServletRequest req =
                (HttpServletRequest) request;

        HttpServletResponse res =
                (HttpServletResponse) response;

        String ip = req.getRemoteAddr();

        if(!rateLimiterService.isAllowed(ip)){

            res.setStatus(429);

            res.getWriter()
                    .write("Too Many Requests");

            return;

        }

        chain.doFilter(request,response);

    }
}