package br.senai.sp.controleepi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.MensagensFirebase;
import br.senai.sp.controleepi.util.Sessao;
import br.senai.sp.controleepi.util.Validacao;

public class CadastroContaActivity extends AppCompatActivity {

    private TextInputLayout campoNome;
    private TextInputLayout campoEmail;
    private TextInputLayout campoSenha;
    private TextInputLayout campoConfirmarSenha;
    private Button btnCriarConta;
    private ProgressBar progresso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_conta);
        setTitle(R.string.titulo_cadastro_conta);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        campoNome = findViewById(R.id.campoNome);
        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        campoConfirmarSenha = findViewById(R.id.campoConfirmarSenha);
        btnCriarConta = findViewById(R.id.btnCriarConta);
        progresso = findViewById(R.id.progresso);

        btnCriarConta.setOnClickListener(v -> criarConta());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean formularioValido() {
        boolean nomeOk = Validacao.minimo(campoNome, 3, "Informe o nome completo");
        boolean emailOk = Validacao.email(campoEmail);
        boolean senhaOk = Validacao.minimo(campoSenha, 6, "A senha precisa ter pelo menos 6 caracteres");
        boolean confirmacaoOk = true;
        if (!Validacao.texto(campoSenha).equals(Validacao.texto(campoConfirmarSenha))) {
            campoConfirmarSenha.setError("As senhas não são iguais");
            confirmacaoOk = false;
        } else {
            campoConfirmarSenha.setError(null);
        }
        return nomeOk && emailOk && senhaOk && confirmacaoOk;
    }

    private void criarConta() {
        if (!formularioValido()) {
            return;
        }

        String nome = Validacao.texto(campoNome);
        String email = Validacao.texto(campoEmail).toLowerCase(Locale.ROOT);
        String senha = Validacao.texto(campoSenha);

        carregando(true);
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(resultado -> salvarUsuario(resultado.getUser().getUid(), nome, email))
                .addOnFailureListener(this::mostrarErro);
    }

    private void salvarUsuario(String uid, String nome, String email) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", nome);
        usuario.put("email", email);

        FirebaseFirestore.getInstance()
                .collection(Colecoes.USUARIOS)
                .document(uid)
                .set(usuario)
                .addOnSuccessListener(nada -> {
                    Toast.makeText(this, "Conta criada com sucesso", Toast.LENGTH_SHORT).show();
                    Sessao.entrar(this, () -> mostrarErro(null));
                })
                .addOnFailureListener(this::mostrarErro);
    }

    private void mostrarErro(Exception erro) {
        carregando(false);
        String mensagem = erro == null ? getString(R.string.msg_sem_internet) : MensagensFirebase.traduzir(erro);
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    private void carregando(boolean ativo) {
        progresso.setVisibility(ativo ? View.VISIBLE : View.GONE);
        btnCriarConta.setEnabled(!ativo);
    }
}
