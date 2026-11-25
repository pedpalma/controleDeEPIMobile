package br.senai.sp.controleepi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import br.senai.sp.controleepi.util.MensagensFirebase;
import br.senai.sp.controleepi.util.Sessao;
import br.senai.sp.controleepi.util.Validacao;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout campoEmail;
    private TextInputLayout campoSenha;
    private Button btnEntrar;
    private Button btnIrCadastro;
    private ProgressBar progresso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnIrCadastro = findViewById(R.id.btnIrCadastro);
        progresso = findViewById(R.id.progresso);

        btnEntrar.setOnClickListener(v -> entrar());
        btnIrCadastro.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroContaActivity.class)));

        if (Sessao.logado()) {
            carregando(true);
            Sessao.entrar(this, this::falhaAoCarregarPerfil);
        }
    }

    private void entrar() {
        boolean emailOk = Validacao.email(campoEmail);
        boolean senhaOk = Validacao.obrigatorio(campoSenha, "Informe a senha");
        if (!emailOk || !senhaOk) {
            return;
        }

        carregando(true);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(Validacao.texto(campoEmail), Validacao.texto(campoSenha))
                .addOnSuccessListener(resultado -> Sessao.entrar(this, this::falhaAoCarregarPerfil))
                .addOnFailureListener(e -> {
                    carregando(false);
                    Toast.makeText(this, MensagensFirebase.traduzir(e), Toast.LENGTH_LONG).show();
                });
    }

    private void falhaAoCarregarPerfil() {
        carregando(false);
        Toast.makeText(this, R.string.msg_sem_internet, Toast.LENGTH_LONG).show();
    }

    private void carregando(boolean ativo) {
        progresso.setVisibility(ativo ? View.VISIBLE : View.GONE);
        btnEntrar.setEnabled(!ativo);
        btnIrCadastro.setEnabled(!ativo);
    }
}
