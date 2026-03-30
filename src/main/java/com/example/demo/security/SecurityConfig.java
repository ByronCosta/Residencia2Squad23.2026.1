package com.example.demo.security;

import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private  JwtAuthenticationFilter jwtAuthFilter;
    @Autowired
    private  AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors( cors -> cors.disable())// Ativa o CORS explicitamente na segurança
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/accenture/auth/authenticate").permitAll()//Rota Login/Cadastro liberada
                        .requestMatchers("/api/accenture/auth/register").hasAnyRole("ADMIN","LIDER")
                        .requestMatchers("/api/accenture/auth/**").permitAll()
                        .requestMatchers("/login/**", "/saml2/**").permitAll()
                        .requestMatchers("/api/accenture/auth/saml-login").authenticated()
                        .anyRequest().authenticated()                   // Todo o resto exige Token
                )
                .saml2Login(saml2 -> saml2
                        .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository) // Adicione esta linha
                        .successHandler(this.saml2SuccessHandler())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sem estado (Stateless)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Para o H2 funcionar no navegador

        return http.build();
    }
    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        RelyingPartyRegistration registration = RelyingPartyRegistrations
                .fromMetadataLocation("classpath:dev-3ur3hy6il3k3anuy_us_auth0_com-metadata.xml")
                .registrationId("auth0")
                .build();
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173","http://localhost:80","http://localhost","http://localhost:8080", "http://127.0.0.1")); // Sua porta do Vue
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public AuthenticationSuccessHandler saml2SuccessHandler() {
        return (request, response, authentication) -> {
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            String email = principal.getFirstAttribute("email"); // ou o atributo do seu IdP

            // Gerar o Token (Método utilitário)
            String token = JwtUtils.generateToken(email);

            // Redireciona para o Front-end passando o Token (exemplo via URL ou Header)
            response.sendRedirect("http://localhost:3000/login-success?token=" + token);
        };
    }
}