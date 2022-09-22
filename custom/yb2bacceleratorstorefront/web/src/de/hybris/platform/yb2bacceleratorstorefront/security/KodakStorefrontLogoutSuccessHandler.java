package de.hybris.platform.yb2bacceleratorstorefront.security;

import com.kodak.security.web.authentication.logout.KodakSimpleUrlLogoutSuccessHandler;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static de.hybris.platform.commercefacades.constants.CommerceFacadesConstants.USER_CONSENTS;

public class KodakStorefrontLogoutSuccessHandler extends KodakSimpleUrlLogoutSuccessHandler
{
    public static final String CLOSE_ACCOUNT_PARAM = "&closeAcc=true";

    private GUIDCookieStrategy guidCookieStrategy;
    private List<String> restrictedPages;
    private SessionService sessionService;

    protected GUIDCookieStrategy getGuidCookieStrategy()
    {
        return guidCookieStrategy;
    }

    @Required
    public void setGuidCookieStrategy(final GUIDCookieStrategy guidCookieStrategy)
    {
        this.guidCookieStrategy = guidCookieStrategy;
    }

    protected List<String> getRestrictedPages()
    {
        return restrictedPages;
    }

    public void setRestrictedPages(final List<String> restrictedPages)
    {
        this.restrictedPages = restrictedPages;
    }

    @Override
    public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                final Authentication authentication) throws IOException, ServletException
    {
        getGuidCookieStrategy().deleteCookie(request, response);
        getSessionService().removeAttribute(USER_CONSENTS);
        if(request.getRequestURI().contains("/logout")){

            eraseSamlCookie(response);

        }

        eraseSamlCookie(response);
        // Delegate to default redirect behaviour
        response.sendRedirect("https://login.microsoftonline.com/common/oauth2/logout");
     //   super.onLogoutSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response)
    {
        if(request.getRequestURI().contains("/logout")){

            eraseSamlCookie(response);
            return "https://login.microsoftonline.com/common/oauth2/logout";
        }
        String targetUrl = super.determineTargetUrl(request, response);

        for (final String restrictedPage : getRestrictedPages())
        {
            // When logging out from a restricted page, return user to homepage.
            if (targetUrl.contains(restrictedPage))
            {
                targetUrl = super.getDefaultTargetUrl();
            }
        }

        // For closing an account, we need to append the closeAcc query string to the target url to display the close account message in the homepage.
        if (StringUtils.isNotBlank(request.getParameter(WebConstants.CLOSE_ACCOUNT)))
        {
            targetUrl = targetUrl + CLOSE_ACCOUNT_PARAM;
        }
        return targetUrl;
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

    protected SessionService getSessionService()
    {
        return sessionService;
    }

    @Required
    public void setSessionService(final SessionService sessionService)
    {
        this.sessionService = sessionService;
    }
}
