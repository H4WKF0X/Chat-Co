package com.chatco.chatco.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    AuthenticationManager authenticationManager(
            BaseLdapPathContextSource contextSource,
            LdapToDbUserMapper ldapToDbUserMapper) {
        LdapBindAuthenticationManagerFactory factory =
                new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserSearchBase("ou=people");
        factory.setUserSearchFilter("(uid={0})");
        factory.setUserDetailsContextMapper(ldapToDbUserMapper);
        return factory.createAuthenticationManager();
    }

    /** Stateless JWT chain for all /api/** endpoints. */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    /** Session-based chain for the Vaadin UI. */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        return http
                // Vaadin manages its own CSRF via synchronised token pattern
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Vaadin framework resources
                        .requestMatchers(
                                "/VAADIN/**", "/PUSH/**", "/UIDL/**",
                                "/vaadinServlet/**", "/frontend/**",
                                "/sw.js", "/sw-runtime-resources-precache.js",
                                "/offline.html", "/manifest.webmanifest",
                                "/icons/**", "/images/**", "/themes/**"
                        ).permitAll()
                        // Login page itself must be public
                        .requestMatchers("/ldap-login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/ldap-login")
                        .permitAll()
                )
                .build();
    }
}
