package com.gestao.agro.util;

import javafx.application.Platform;
import javafx.scene.control.TextField;

public class MascaraUtils {

    public static void aplicarMascaraCpf(TextField textField) {
        textField.textProperty().addListener((obs, valorAntigo, valorNovo) -> {
            if (valorNovo == null) return;

            // Mantém apenas os dígitos numéricos
            String apenasDigitos = valorNovo.replaceAll("\\D", "");

            // Limita a 11 dígitos
            if (apenasDigitos.length() > 11) {
                apenasDigitos = apenasDigitos.substring(0, 11);
            }

            StringBuilder formatado = new StringBuilder();
            int tamanho = apenasDigitos.length();

            for (int i = 0; i < tamanho; i++) {
                if (i == 3 || i == 6) {
                    formatado.append(".");
                } else if (i == 9) {
                    formatado.append("-");
                }
                formatado.append(apenasDigitos.charAt(i));
            }

            String resultadoFinal = formatado.toString();
            if (!resultadoFinal.equals(valorNovo)) {
                Platform.runLater(() -> {
                    textField.setText(resultadoFinal);
                    textField.positionCaret(resultadoFinal.length());
                });
            }
        });
    }
}