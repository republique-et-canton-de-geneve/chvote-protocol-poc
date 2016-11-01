package ch.ge.ve.protopoc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This configuration class manages the spring-security configuration,
 * <ul>
 *     <li>request authorization (access to REST services for accounts is always denied)</li>
 *     <li>authentication mechanism (currently BASIC)</li>
 * </ul>
 */
@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/accounts/*").denyAll()
                .anyRequest().fullyAuthenticated()
                .and().httpBasic()
                .and().csrf().disable();
    }
}
