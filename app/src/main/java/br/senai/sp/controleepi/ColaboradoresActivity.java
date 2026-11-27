package br.senai.sp.controleepi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.senai.sp.controleepi.adapter.ColaboradorAdapter;
import br.senai.sp.controleepi.model.Colaborador;
import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.MensagensFirebase;

public class ColaboradoresActivity extends AppCompatActivity {

    private RecyclerView lista;
    private ProgressBar progresso;
    private View blocoVazio;
    private TextView txtVazioTitulo;
    private TextView txtVazioDica;
    private EditText editBusca;

    private final List<Colaborador> todos = new ArrayList<>();
    private final List<Colaborador> exibidos = new ArrayList<>();
    private ColaboradorAdapter adapter;
    private ListenerRegistration registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colaboradores);
        setTitle(R.string.titulo_colaboradores);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lista = findViewById(R.id.listaColaboradores);
        progresso = findViewById(R.id.progresso);
        blocoVazio = findViewById(R.id.blocoVazio);
        txtVazioTitulo = findViewById(R.id.txtVazioTitulo);
        txtVazioDica = findViewById(R.id.txtVazioDica);
        editBusca = findViewById(R.id.editBusca);

        adapter = new ColaboradorAdapter(exibidos, this::abrirFicha);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        findViewById(R.id.btnNovoColaborador).setOnClickListener(v ->
                startActivity(new Intent(this, ColaboradorFormActivity.class)));

        editBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filtrar();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registro = FirebaseFirestore.getInstance()
                .collection(Colecoes.COLABORADORES)
                .orderBy("nome")
                .addSnapshotListener((snapshot, erro) -> {
                    progresso.setVisibility(View.GONE);
                    if (erro != null) {
                        todos.clear();
                        exibidos.clear();
                        adapter.notifyDataSetChanged();
                        mostrarVazio(getString(R.string.msg_erro_generico), MensagensFirebase.traduzir(erro));
                        return;
                    }
                    todos.clear();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot documento : snapshot) {
                            todos.add(documento.toObject(Colaborador.class));
                        }
                    }
                    filtrar();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registro != null) {
            registro.remove();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void filtrar() {
        String termo = editBusca.getText() == null ? "" : editBusca.getText().toString().trim().toLowerCase(Locale.ROOT);
        exibidos.clear();
        for (Colaborador colaborador : todos) {
            if (termo.isEmpty() || corresponde(colaborador, termo)) {
                exibidos.add(colaborador);
            }
        }
        adapter.notifyDataSetChanged();

        if (!exibidos.isEmpty()) {
            blocoVazio.setVisibility(View.GONE);
            lista.setVisibility(View.VISIBLE);
        } else if (todos.isEmpty()) {
            mostrarVazio(getString(R.string.vazio_colaboradores_titulo), getString(R.string.vazio_colaboradores_dica));
        } else {
            mostrarVazio(getString(R.string.vazio_busca_titulo), getString(R.string.vazio_busca_dica));
        }
    }

    private boolean corresponde(Colaborador colaborador, String termo) {
        String matricula = colaborador.getMatricula() == null ? "" : colaborador.getMatricula().toLowerCase(Locale.ROOT);
        String nome = colaborador.getNome() == null ? "" : colaborador.getNome().toLowerCase(Locale.ROOT);
        return matricula.startsWith(termo) || nome.contains(termo);
    }

    private void abrirFicha(Colaborador colaborador) {
        Intent intent = new Intent(this, FichaColaboradorActivity.class);
        intent.putExtra(FichaColaboradorActivity.EXTRA_COLABORADOR_UID, colaborador.getUid());
        startActivity(intent);
    }

    private void mostrarVazio(String titulo, String dica) {
        txtVazioTitulo.setText(titulo);
        txtVazioDica.setText(dica);
        blocoVazio.setVisibility(View.VISIBLE);
        lista.setVisibility(View.GONE);
    }
}
