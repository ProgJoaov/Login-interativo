package com.joaovitor.auth;

public class AuthResponse {
    private String token;
    private String nome;
    private String email;

    public AuthResponse(String token, String nome, String email) {
        this.token = token;
        this.nome = nome;
        this.email = email;
    }

    public String getToken() { return token; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
}
