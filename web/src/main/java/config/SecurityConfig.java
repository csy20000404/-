package config;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.Hr;
import model.RespBean;
import service.HrService;

public class SecurityConfig extends WebSecurityConfigurerAdapter{
@Autowired
HrService hrService;
@Autowired
CustomFilterInvocationSecurityMetadataSource customFilterInvocationSecurityMetadataSource;
@Autowired 
CustomUrlDecisionManager customUrlDecisionManager;
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(hrService);
}

@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/css/**", "/js/**", "/index.html", "/img/**", "/fonts/**", "/favicon.ico", "/verifyCode");
}

@Bean
LoginFilter loginFilter() throws Exception {
    LoginFilter loginFilter = new LoginFilter();
    loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
                response.setContentType("application/json;charset=utf-8");
                PrintWriter out = response.getWriter();
                Hr hr = (Hr) authentication.getPrincipal();
                hr.setPassword(null);
                RespBean ok = RespBean.ok("��¼�ɹ�!", hr);
                String s = new ObjectMapper().writeValueAsString(ok);
                out.write(s);
                out.flush();
                out.close();
            }
    );
    loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
                response.setContentType("application/json;charset=utf-8");
                PrintWriter out = response.getWriter();
                RespBean respBean = RespBean.error(exception.getMessage());
                if (exception instanceof LockedException) {
                    respBean.setMsg("�˻�������������ϵ����Ա!");
                } else if (exception instanceof CredentialsExpiredException) {
                    respBean.setMsg("������ڣ�����ϵ����Ա!");
                } else if (exception instanceof AccountExpiredException) {
                    respBean.setMsg("�˻����ڣ�����ϵ����Ա!");
                } else if (exception instanceof DisabledException) {
                    respBean.setMsg("�˻������ã�����ϵ����Ա!");
                } else if (exception instanceof BadCredentialsException) {
                    respBean.setMsg("�û����������������������������!");
                }
                out.write(new ObjectMapper().writeValueAsString(respBean));
                out.flush();
                out.close();
            }
    );
    loginFilter.setAuthenticationManager(authenticationManagerBean());
    loginFilter.setFilterProcessesUrl("/doLogin");
    ConcurrentSessionControlAuthenticationStrategy sessionStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
    sessionStrategy.setMaximumSessions(1);
    loginFilter.setSessionAuthenticationStrategy(sessionStrategy);
    return loginFilter;
}

@Bean
SessionRegistryImpl sessionRegistry() {
    return new SessionRegistryImpl();
}

@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                @Override
                public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                    object.setAccessDecisionManager(customUrlDecisionManager);
                    object.setSecurityMetadataSource(customFilterInvocationSecurityMetadataSource);
                    return object;
                }
            })
            .and()
            .logout()
            .logoutSuccessHandler((req, resp, authentication) -> {
                        resp.setContentType("application/json;charset=utf-8");
                        PrintWriter out = resp.getWriter();
                        out.write(new ObjectMapper().writeValueAsString(RespBean.ok("ע���ɹ�!")));
                        out.flush();
                        out.close();
                    }
            )
            .permitAll()
            .and()
            .csrf().disable().exceptionHandling()
            //û����֤ʱ�������ﴦ��������Ҫ�ض���
            .authenticationEntryPoint((req, resp, authException) -> {
                        resp.setContentType("application/json;charset=utf-8");
                        resp.setStatus(401);
                        PrintWriter out = resp.getWriter();
                        RespBean respBean = RespBean.error("����ʧ��!");
                        if (authException instanceof InsufficientAuthenticationException) {
                            respBean.setMsg("����ʧ�ܣ�����ϵ����Ա!");
                        }
                        out.write(new ObjectMapper().writeValueAsString(respBean));
                        out.flush();
                        out.close();
                    }
            );
        http.addFilterAt(new ConcurrentSessionFilter(sessionRegistry(), event -> {
        HttpServletResponse resp = event.getResponse();
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(401);
        PrintWriter out = resp.getWriter();
        out.write(new ObjectMapper().writeValueAsString(RespBean.error("��������һ̨�豸��¼�����ε�¼������!")));
        out.flush();
        out.close();
    }), ConcurrentSessionFilter.class);
    http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);
}
}
