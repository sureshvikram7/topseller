package de.hybris.platform.yb2bacceleratorstorefront.security;

import de.hybris.platform.util.Config;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FbmUsernamePasswordAuthenticationFilter extends GenericFilterBean {

    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private boolean postOnly = true;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if(request.getServletPath().equalsIgnoreCase("/login")){
            eraseSamlCookie(response);
            response.sendRedirect("www.google.com");
            return ;
        }else{
            filterChain.doFilter(request, response);
        }
    }

    public static Cookie getSamlCookie(final HttpServletRequest request) {
        final String cookieName = Config.getParameter("sso.cookie.name");
        return cookieName != null ? WebUtils.getCookie(request, cookieName) : null;
    }
    private void eraseSamlCookie(final HttpServletResponse httpResponse) {
        final String cookieName = Config.getParameter("sso.cookie.name");
        if (cookieName != null) {
            final Cookie cookie = new Cookie(cookieName, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            httpResponse.addCookie(cookie);
        }
    }
}

