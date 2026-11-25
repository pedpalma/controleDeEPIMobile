package br.senai.sp.controleepi.util;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestoreException;

public final class MensagensFirebase {

    private MensagensFirebase() {
    }

    public static String traduzir(Exception erro) {
        if (erro instanceof FirebaseNetworkException) {
            return "Sem conexão com a internet. Tente de novo.";
        }
        if (erro instanceof FirebaseAuthWeakPasswordException) {
            return "Senha fraca. Use pelo menos 6 caracteres.";
        }
        if (erro instanceof FirebaseAuthUserCollisionException) {
            return "Já existe uma conta com este e-mail.";
        }
        if (erro instanceof FirebaseAuthInvalidUserException
                || erro instanceof FirebaseAuthInvalidCredentialsException) {
            return "E-mail ou senha incorretos.";
        }
        if (erro instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code codigo = ((FirebaseFirestoreException) erro).getCode();
            if (codigo == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return "Você não tem permissão para esta ação.";
            }
            if (codigo == FirebaseFirestoreException.Code.UNAVAILABLE) {
                return "Sem conexão com o servidor. Tente de novo.";
            }
        }
        return "Algo deu errado. Tente de novo.";
    }
}
