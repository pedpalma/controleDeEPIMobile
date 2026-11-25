package br.senai.sp.controleepi.util;

import android.util.Patterns;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public final class Validacao {

    private Validacao() {
    }

    public static String texto(TextInputLayout campo) {
        EditText editText = campo.getEditText();
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    public static boolean obrigatorio(TextInputLayout campo, String mensagem) {
        if (texto(campo).isEmpty()) {
            campo.setError(mensagem);
            return false;
        }
        campo.setError(null);
        return true;
    }

    public static boolean email(TextInputLayout campo) {
        String valor = texto(campo);
        if (valor.isEmpty()) {
            campo.setError("Informe o e-mail");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(valor).matches()) {
            campo.setError("E-mail inválido. Exemplo: nome@empresa.com");
            return false;
        }
        campo.setError(null);
        return true;
    }

    public static boolean minimo(TextInputLayout campo, int tamanho, String mensagem) {
        if (texto(campo).length() < tamanho) {
            campo.setError(mensagem);
            return false;
        }
        campo.setError(null);
        return true;
    }

    public static Integer inteiroPositivo(TextInputLayout campo, String nomeCampo) {
        String valor = texto(campo);
        if (valor.isEmpty()) {
            campo.setError("Informe " + nomeCampo);
            return null;
        }
        try {
            int numero = Integer.parseInt(valor);
            if (numero <= 0) {
                campo.setError("A quantidade deve ser maior que zero");
                return null;
            }
            campo.setError(null);
            return numero;
        } catch (NumberFormatException e) {
            campo.setError("Número inválido");
            return null;
        }
    }
}
