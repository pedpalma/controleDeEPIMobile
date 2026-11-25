package br.senai.sp.controleepi.util;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import br.senai.sp.controleepi.AguardandoPerfilActivity;
import br.senai.sp.controleepi.LoginActivity;
import br.senai.sp.controleepi.MenuTecnicoActivity;
import br.senai.sp.controleepi.MinhasEntregasActivity;

public final class Sessao {

    private Sessao() {
    }

    public static boolean logado() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static String uid() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        return usuario == null ? null : usuario.getUid();
    }

    public static void entrar(Activity origem, Runnable aoFalhar) {
        String uid = uid();
        if (uid == null) {
            abrir(origem, LoginActivity.class);
            return;
        }
        FirebaseFirestore.getInstance()
                .collection(Colecoes.USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {
                    String perfil = documento.getString("perfil");
                    if (Colecoes.PERFIL_TECNICO.equals(perfil)) {
                        abrir(origem, MenuTecnicoActivity.class);
                    } else if (Colecoes.PERFIL_COLABORADOR.equals(perfil)) {
                        abrir(origem, MinhasEntregasActivity.class);
                    } else {
                        abrir(origem, AguardandoPerfilActivity.class);
                    }
                })
                .addOnFailureListener(e -> aoFalhar.run());
    }

    public static void sair(Activity origem) {
        FirebaseAuth.getInstance().signOut();
        abrir(origem, LoginActivity.class);
    }

    private static void abrir(Activity origem, Class<?> destino) {
        Intent intent = new Intent(origem, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        origem.startActivity(intent);
        origem.finish();
    }
}
