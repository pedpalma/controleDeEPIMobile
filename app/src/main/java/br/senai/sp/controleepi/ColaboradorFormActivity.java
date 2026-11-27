package br.senai.sp.controleepi;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.MensagensFirebase;
import br.senai.sp.controleepi.util.Validacao;

public class ColaboradorFormActivity extends AppCompatActivity {

    private TextInputLayout campoNome;
    private TextInputLayout campoMatricula;
    private TextInputLayout campoSetor;
    private TextInputLayout campoEmail;
    private Button btnSalvar;
    private ProgressBar progresso;

    private final FirebaseFirestore banco = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colaborador_form);
        setTitle(R.string.titulo_novo_colaborador);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        campoNome = findViewById(R.id.campoNome);
        campoMatricula = findViewById(R.id.campoMatricula);
        campoSetor = findViewById(R.id.campoSetor);
        campoEmail = findViewById(R.id.campoEmail);
        btnSalvar = findViewById(R.id.btnSalvar);
        progresso = findViewById(R.id.progresso);

        AutoCompleteTextView editSetor = findViewById(R.id.editSetor);
        editSetor.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.setores)));

        btnSalvar.setOnClickListener(v -> salvar());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean formularioValido() {
        boolean nomeOk = Validacao.minimo(campoNome, 3, "Informe o nome do colaborador");
        boolean matriculaOk = Validacao.obrigatorio(campoMatricula, "Informe a matrícula");
        boolean setorOk = Validacao.obrigatorio(campoSetor, "Informe o setor");
        boolean emailOk = Validacao.email(campoEmail);
        return nomeOk && matriculaOk && setorOk && emailOk;
    }

    private void salvar() {
        if (!formularioValido()) {
            return;
        }
        carregando(true);
        String email = Validacao.texto(campoEmail).toLowerCase(Locale.ROOT);
        banco.collection(Colecoes.USUARIOS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(resultado -> {
                    if (resultado.isEmpty()) {
                        carregando(false);
                        campoEmail.setError("Nenhuma conta encontrada com este e-mail. O colaborador precisa criar a conta no app primeiro.");
                        return;
                    }
                    DocumentSnapshot usuario = resultado.getDocuments().get(0);
                    if (!Colecoes.PERFIL_COLABORADOR.equals(usuario.getString("perfil"))) {
                        carregando(false);
                        campoEmail.setError("Esta conta não tem o perfil colaborador. Defina o perfil no console do Firebase.");
                        return;
                    }
                    verificarMatricula(usuario.getId(), email);
                })
                .addOnFailureListener(this::mostrarErro);
    }

    private void verificarMatricula(String uid, String email) {
        String matricula = Validacao.texto(campoMatricula);
        banco.collection(Colecoes.COLABORADORES)
                .whereEqualTo("matricula", matricula)
                .get()
                .addOnSuccessListener(resultado -> {
                    for (DocumentSnapshot documento : resultado.getDocuments()) {
                        if (!documento.getId().equals(uid)) {
                            carregando(false);
                            campoMatricula.setError("Matrícula já cadastrada para " + documento.getString("nome"));
                            return;
                        }
                    }
                    gravar(uid, email, matricula);
                })
                .addOnFailureListener(this::mostrarErro);
    }

    private void gravar(String uid, String email, String matricula) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", Validacao.texto(campoNome));
        dados.put("matricula", matricula);
        dados.put("setor", Validacao.texto(campoSetor));
        dados.put("email", email);

        banco.collection(Colecoes.COLABORADORES)
                .document(uid)
                .set(dados)
                .addOnSuccessListener(nada -> {
                    Toast.makeText(this, "Colaborador cadastrado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(this::mostrarErro);
    }

    private void mostrarErro(Exception erro) {
        carregando(false);
        Toast.makeText(this, MensagensFirebase.traduzir(erro), Toast.LENGTH_LONG).show();
    }

    private void carregando(boolean ativo) {
        progresso.setVisibility(ativo ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!ativo);
    }
}
