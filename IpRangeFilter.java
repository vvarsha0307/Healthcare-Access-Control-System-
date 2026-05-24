package com.example.demo.security.filter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class IpRangeFilter extends OncePerRequestFilter {
    private boolean isAllowedIP(String ip) {
        // DEV MODE (localhost allow)
        if(ip.equals("127.0.0.1") ||
                ip.equals("0:0:0:0:0:0:0:1"))
            return true;
        // Hospital network
        return ip.startsWith("192.168.1.");
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // 🔥 LOGIN API allow
        if(path.startsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }
        String ip = request.getRemoteAddr();
        if(!isAllowedIP(ip)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter()
                    .write("Access denied ❌ Outside hospital network");
            return;
        }
        chain.doFilter(request, response);
    }
}
