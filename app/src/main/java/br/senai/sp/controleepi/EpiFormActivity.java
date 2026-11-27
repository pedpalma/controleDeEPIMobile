package br.senai.sp.controleepi;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import br.senai.sp.controleepi.model.Epi;
import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.Datas;
import br.senai.sp.controleepi.util.MensagensFirebase;
import br.senai.sp.controleepi.util.Validacao;

public class EpiFormActivity extends AppCompatActivity {

    public static final String EXTRA_EPI_ID = "epiId";
    private static final String CHAVE_VALIDADE = "validade";

    private TextInputLayout campoDescricao;
    private TextInputLayout campoEstoque;
    private TextInputLayout campoCa;
    private TextInputLayout campoValidade;
    private EditText editValidade;
    private Button btnSalvar;
    private ProgressBar progresso;

    private String epiId;
    private LocalDate validade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epi_form);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        campoDescricao = findViewById(R.id.campoDescricao);
        campoEstoque = findViewById(R.id.campoEstoque);
        campoCa = findViewById(R.id.campoCa);
        campoValidade = findViewById(R.id.campoValidade);
        editValidade = findViewById(R.id.editValidade);
        btnSalvar = findViewById(R.id.btnSalvar);
        progresso = findViewById(R.id.progresso);

        epiId = getIntent().getStringExtra(EXTRA_EPI_ID);
        setTitle(epiId == null ? R.string.titulo_novo_epi : R.string.titulo_editar_epi);

        editValidade.setOnClickListener(v -> escolherValidade());
        btnSalvar.setOnClickListener(v -> salvar());

        if (savedInstanceState != null) {
            validade = Datas.deTexto(savedInstanceState.getString(CHAVE_VALIDADE));
            mostrarValidade();
        } else if (epiId != null) {
            carregarEpi();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CHAVE_VALIDADE, Datas.paraTexto(validade));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregarEpi() {
        carregando(true);
        FirebaseFirestore.getInstance()
                .collection(Colecoes.EPIS)
                .document(epiId)
                .get()
                .addOnSuccessListener(documento -> {
                    carregando(false);
                    Epi epi = documento.toObject(Epi.class);
                    if (epi == null) {
                        Toast.makeText(this, "EPI não encontrado", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    preencher(campoDescricao, epi.getDescricao());
                    preencher(campoEstoque, String.valueOf(epi.getEstoque()));
                    preencher(campoCa, epi.getCa());
                    validade = Datas.paraLocalDate(epi.getValidadeCa());
                    mostrarValidade();
                })
                .addOnFailureListener(this::mostrarErro);
    }

    private void preencher(TextInputLayout campo, String valor) {
        if (campo.getEditText() != null) {
            campo.getEditText().setText(valor);
        }
    }

    private void escolherValidade() {
        LocalDate inicial = validade != null ? validade : LocalDate.now().plusYears(1);
        DatePickerDialog dialogo = new DatePickerDialog(this,
                (picker, ano, mes, dia) -> {
                    validade = LocalDate.of(ano, mes + 1, dia);
                    mostrarValidade();
                },
                inicial.getYear(), inicial.getMonthValue() - 1, inicial.getDayOfMonth());
        dialogo.show();
    }

    private void mostrarValidade() {
        editValidade.setText(Datas.formatar(validade));
        if (validade != null) {
            campoValidade.setError(null);
        }
    }

    private boolean formularioValido() {
        boolean descricaoOk = Validacao.minimo(campoDescricao, 3, "Informe a descrição do EPI");
        Integer estoque = Validacao.inteiroNaoNegativo(campoEstoque, "o estoque");
        boolean caOk = Validacao.obrigatorio(campoCa, "Informe o número do CA");
        boolean validadeOk = validade != null;
        if (!validadeOk) {
            campoValidade.setError("Escolha a data de validade do CA");
        }
        return descricaoOk && estoque != null && caOk && validadeOk;
    }

    private void salvar() {
        if (!formularioValido()) {
            return;
        }

        Map<String, Object> dados = new HashMap<>();
        dados.put("descricao", Validacao.texto(campoDescricao));
        dados.put("estoque", Integer.parseInt(Validacao.texto(campoEstoque)));
        dados.put("ca", Validacao.texto(campoCa));
        dados.put("validadeCa", Datas.paraTimestamp(validade));

        carregando(true);
        FirebaseFirestore banco = FirebaseFirestore.getInstance();
        Task<?> tarefa = epiId == null
                ? banco.collection(Colecoes.EPIS).add(dados)
                : banco.collection(Colecoes.EPIS).document(epiId).update(dados);

        tarefa.addOnSuccessListener(resultado -> {
                    Toast.makeText(this, "EPI salvo", Toast.LENGTH_SHORT).show();
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
