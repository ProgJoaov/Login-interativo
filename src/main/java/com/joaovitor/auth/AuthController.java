package com.joaovitor.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (request.getNome() == null || request.getEmail() == null || request.getSenha() == null) {
            return ResponseEntity.badRequest().body(message("Preencha todos os campos."));
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(message("E-mail já cadastrado."));
        }

        User user = new User(request.getNome(), request.getEmail(), passwordEncoder.encode(request.getSenha()));
        userRepository.save(user);
        return ResponseEntity.ok(message("Usuário cadastrado com sucesso."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(401).body(message("E-mail ou senha inválidos."));
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            return ResponseEntity.status(401).body(message("E-mail ou senha inválidos."));
        }

        String token = jwtService.gerarToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getNome(), user.getEmail()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(message("Token não informado."));
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.validar(token);

        if (email == null) {
            return ResponseEntity.status(401).body(message("Token inválido ou expirado."));
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(404).body(message("Usuário não encontrado."));
        }

        User user = optionalUser.get();

        Map<String, String> response = new HashMap<>();
        response.put("nome", user.getNome());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    private Map<String, String> message(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
