package com.ajpeng.mall.mmall.validate;

import com.ajpeng.mall.mmall.common.Constant;
import com.ajpeng.mall.mmall.exception.ValidateCodeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("validateCodeFilter")
@Slf4j
public class ValidateCodeFilter extends OncePerRequestFilter implements InitializingBean {
    /**
     * 操作session的工具类
     */
    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
    /**
     * 验证码校验失败处理器
     */
    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //System.out.println(request.getRequestURI());
        Pattern p = Pattern.compile("/logon");
        Matcher m = p.matcher(request.getRequestURI());
        if (m.find()) {
            try {
                ServletWebRequest servletWebRequest = new ServletWebRequest(request);
                String imageCode = (String) sessionStrategy.getAttribute(servletWebRequest, Constant.SESSION_KEY);
                String validateCode = request.getParameter("validateCode");
                if (!StringUtils.equals(imageCode, validateCode)) {
                    throw new ValidateCodeException("验证码不匹配");
                }
            } catch (ValidateCodeException exception) {
                logger.error(exception);
                authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
