package com.example.demo.controller;

import com.example.demo.auth.AuthenticationResponse;
import com.example.demo.auth.RegisterRequest;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.model.EntProfissional;
import com.example.demo.repository.ProfissionalRepository;
import com.example.demo.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/accenture/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    @Autowired
    public ProfissionalRepository profissionalRepository;

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN','LIDER')")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {

        System.out.println("Role no response entity : "+request.getRole());
        return ResponseEntity.ok(service.register(request));
    }

    @GetMapping("/saml-login")
    public void initiateSamlLogin() {
        // Não precisa de código aqui!
        // O Spring Security notará que esta rota exige autenticação,
        // verá que não há sessão/token e redirecionará automaticamente para o IdP.
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
    @GetMapping("/test-auth")
    public ResponseEntity<?> test(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Erro: O Spring Security não reconheceu sua autenticação. O contexto está vazio.");
        }
        return ResponseEntity.ok("Olá " + userDetails.getUsername() + ", seu acesso está funcionando!");
    }
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,Principal connectedUser) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/register-profissional")
    public ResponseEntity registerProfissional(@RequestBody EntProfissional data) {
        // Lógica para verificar se o email já existe
        // Lógica para encriptar a senha: String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());

        // Salva usando o repositório de profissional
        this.profissionalRepository.save(data);

        return ResponseEntity.ok().build();
    }
}