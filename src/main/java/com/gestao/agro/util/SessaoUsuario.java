package com.gestao.agro.util;

public class SessaoUsuario {

    private static String nomeConsultor;
    private static String matriculaOuEmail;

    public static void login(String nome, String identificador) {
        nomeConsultor = nome;
        matriculaOuEmail = identificador;
    }

    public static void logout() {
        nomeConsultor = null;
        matriculaOuEmail = null;
    }

    public static String getNomeConsultor() {
        return (nomeConsultor == null || nomeConsultor.isBlank()) ? "Consultor de Campo" : nomeConsultor;
    }

    public static String getMatriculaOuEmail() {
        return matriculaOuEmail;
    }

    public static boolean isAutenticado() {
        return nomeConsultor != null && !nomeConsultor.isBlank();
    }
}