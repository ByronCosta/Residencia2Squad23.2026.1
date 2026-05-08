package com.example.demo.security;

import com.example.demo.repository.UserRepository; // Importante
import com.example.demo.model.User;             // Ajuste para seu pacote
import com.example.demo.model.UserRole;         // Ajuste para seu pacote
import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.core.Saml2ResponseValidatorResult;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository; // Adicionado para salvar novos usuários

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/accenture/auth/**", "/login/**", "/saml2/**", "/favicon.ico", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .saml2Login(saml2 -> saml2
                        .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
                        .authenticationManager(samlAuthManager(relyingPartyRegistrationRepository)) // Agora ele vai achar o método
                        .successHandler(saml2SuccessHandler())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // MÉTODO QUE ESTAVA FALTANDO (Aquele que deu erro na linha 66)
    @Bean
    public AuthenticationManager samlAuthManager(RelyingPartyRegistrationRepository registrations) {
        OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();

        // Relaxa as validações para ambiente local (localhost)
        provider.setAssertionValidator(context -> Saml2ResponseValidatorResult.success());
        provider.setResponseValidator(context -> Saml2ResponseValidatorResult.success());

        return new ProviderManager(provider);
    }
    @Bean
    public AuthenticationSuccessHandler saml2SuccessHandler() {
        return (request, response, authentication) -> {
            try {
                Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();

                // 1. Extrair o Email
                String email = principal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");
                if (email == null) email = principal.getName();

                // 2. Extrair a Role da Microsoft
                String microsoftRole = principal.getFirstAttribute("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
                if (microsoftRole == null) microsoftRole = "USER";

                UserDetails userDetails;
                try {
                    // Tenta carregar o usuário existente
                    userDetails = userDetailsService.loadUserByUsername(email);

                    // ATUALIZAÇÃO DA ROLE PARA USUÁRIO EXISTENTE
                    User user = userRepository.findByEmail(email).orElseThrow();
                    try {
                        UserRole roleVindaDaMicrosoft = UserRole.valueOf(microsoftRole.toUpperCase());

                        if (user.getRole() != roleVindaDaMicrosoft) {
                            user.setRole(roleVindaDaMicrosoft);
                            userRepository.save(user);
                            // Recarrega o userDetails para o JWT sair com a role nova
                            userDetails = userDetailsService.loadUserByUsername(email);
                        }
                    } catch (IllegalArgumentException ex) {
                        // Se a role da Microsoft não existir no seu Enum, mantém a que já estava no banco
                        System.out.println("Role vinda da Microsoft desconhecida: " + microsoftRole);
                    }

                } catch (Exception e) {
                    // CRIAÇÃO DE NOVO USUÁRIO (Caso não exista)
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setPassword("");

                    try {
                        newUser.setRole(UserRole.valueOf(microsoftRole.toUpperCase()));
                    } catch (IllegalArgumentException ex) {
                        newUser.setRole(UserRole.USER); // Default se o nome for inválido
                    }

                    userRepository.save(newUser);
                    userDetails = userDetailsService.loadUserByUsername(email);
                }

                // 3. Gera o Token com as informações atualizadas
                String token = jwtService.generateToken(userDetails);
                response.sendRedirect("http://localhost:5173/login-success?token=" + token);

            } catch (Exception ex) {
                ex.printStackTrace();
                response.sendRedirect("http://localhost:5173/login?error=auth_failed");
            }
        };
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173", "https://login.microsoftonline.com", "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}