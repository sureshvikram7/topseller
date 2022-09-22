package de.hybris.platform.yb2bacceleratorstorefront.security;

import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.user.CookieBasedLoginToken;
import de.hybris.platform.jalo.user.LoginToken;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import org.apache.log4j.Logger;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KodakSamlAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOG = Logger.getLogger(KodakSamlAuthenticationFilter.class);

    private UserDetailsService userDetailsService;
    private AuthenticationManager authenticationManager;
    private CustomerFacade customerFacade;
    @Resource
    private UserService userService;
    private RememberMeServices rememberMeServices;
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    private AuthenticationFailureHandler authenticationFailureHandler;
    private GUIDCookieStrategy guidCookieStrategy;

    /**
     * @return the guidCookieStrategy
     */
    public GUIDCookieStrategy getGuidCookieStrategy() {
        return guidCookieStrategy;
    }

    /**
     * @param guidCookieStrategy the guidCookieStrategy to set
     */
    public void setGuidCookieStrategy(final GUIDCookieStrategy guidCookieStrategy) {
        this.guidCookieStrategy = guidCookieStrategy;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                                 final FilterChain chain) throws IOException, ServletException {


        if (getSamlCookie(httpRequest) != null) {
            final LoginToken loginToken = new CookieBasedLoginToken(getSamlCookie(httpRequest));
            final String userId = loginToken.getUser().getUid();
            // perform login only in case token doesn't belong to currently logged in user
            if (getCustomerFacade().getCurrentCustomer() != null
                    && !getCustomerFacade().getCurrentCustomer().getUid().equals(loginToken.getUser().getUid())) {
                final UserDetails userDetails = getUserDetailsService().loadUserByUsername(loginToken.getUser().getUid());
                if((httpRequest.getSession().getAttribute("originalReferer")!=null && ! httpRequest.getSession().getAttribute("originalReferer").toString().contains("asm")) && (httpRequest.getSession().getAttribute("originalReferer")!=null && ! httpRequest.getSession().getAttribute("originalReferer").toString().contains("/backoffice"))) {
                    if (userService.getUserForUID(userId) instanceof B2BCustomerModel) {
                        final B2BCustomerModel userModel = (B2BCustomerModel) userService.getUserForUID(userId);
                        userService.setCurrentUser(userModel);
                        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                loginToken.getUser().getUid(), loginToken, userDetails.getAuthorities());
                        token.setDetails(new WebAuthenticationDetails(httpRequest));
                        try {
                            getGuidCookieStrategy().setCookie(httpRequest, httpResponse);
                            successfulAuthentication(httpRequest, httpResponse, token);
                        } catch (final Exception e) {
                            eraseSamlCookie(httpResponse);
                            LOG.debug(e.getMessage(), e);
                        }
                    } else {
                        if (getCustomerFacade().getCurrentCustomer() != null && userService.getUserForUID(getCustomerFacade().getCurrentCustomer().getUid()) instanceof B2BCustomerModel) {
                            final B2BCustomerModel userModel = (B2BCustomerModel) userService.getUserForUID(userId);
                            userService.setCurrentUser(userModel);
                            final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                    loginToken.getUser().getUid(), loginToken, userDetails.getAuthorities());
                            token.setDetails(new WebAuthenticationDetails(httpRequest));
                            try {
                                getGuidCookieStrategy().setCookie(httpRequest, httpResponse);
                                successfulAuthentication(httpRequest, httpResponse, token);
                            } catch (final Exception e) {
                                eraseSamlCookie(httpResponse);
                                LOG.debug(e.getMessage(), e);
                            }
                        }

                    }

                }
            }
        }
        chain.doFilter(httpRequest, httpResponse);
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


    private void successfulAuthentication(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                                          final Authentication authResult) throws IOException, ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SSO Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }
        SecurityContextHolder.getContext().setAuthentication(authResult);
        getAuthenticationSuccessHandler().onAuthenticationSuccess(httpRequest, httpResponse, authResult);
    }

    /**
     * Get SAML sso cookie.
     *
     * @param request
     * @return saml SSO cookie if persist
     */
    public static Cookie getSamlCookie(final HttpServletRequest request) {
        final String cookieName = Config.getParameter("sso.cookie.name");
        return cookieName != null ? WebUtils.getCookie(request, cookieName) : null;
    }

    /**
     * @return the userDetailsService
     */
    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    /**
     * @param userDetailsService the userDetailsService to set
     */
    public void setUserDetailsService(final UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * @return the authenticationManager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * @param authenticationManager the authenticationManager to set
     */
    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @return the customerFacade
     */
    public CustomerFacade getCustomerFacade() {
        return customerFacade;
    }

    /**
     * @param customerFacade the customerFacade to set
     */
    public void setCustomerFacade(final CustomerFacade customerFacade) {
        this.customerFacade = customerFacade;
    }

    /**
     * @return the rememberMeServices
     */
    public RememberMeServices getRememberMeServices() {
        return rememberMeServices;
    }

    /**
     * @param rememberMeServices the rememberMeServices to set
     */
    public void setRememberMeServices(final RememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
    }

    /**
     * @return the authenticationSuccessHandler
     */
    public AuthenticationSuccessHandler getAuthenticationSuccessHandler() {
        return authenticationSuccessHandler;
    }

    /**
     * @param authenticationSuccessHandler the authenticationSuccessHandler to set
     */
    public void setAuthenticationSuccessHandler(final AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    /**
     * @return the authenticationFailureHandler
     */
    public AuthenticationFailureHandler getAuthenticationFailureHandler() {
        return authenticationFailureHandler;
    }

    /**
     * @param authenticationFailureHandler the authenticationFailureHandler to set
     */
    public void setAuthenticationFailureHandler(final AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }
}
