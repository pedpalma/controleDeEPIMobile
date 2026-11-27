package br.senai.sp.controleepi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import br.senai.sp.controleepi.adapter.EntregaAdapter;
import br.senai.sp.controleepi.model.Colaborador;
import br.senai.sp.controleepi.model.Entrega;
import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.MensagensFirebase;

public class FichaColaboradorActivity extends AppCompatActivity {

    public static final String EXTRA_COLABORADOR_UID = "colaboradorUid";

    private RecyclerView lista;
    private ProgressBar progresso;
    private View blocoVazio;
    private TextView txtVazioTitulo;
    private TextView txtVazioDica;
    private TextView txtNome;
    private TextView txtDados;
    private TextView txtTotal;
    private TextView txtConfirmadas;
    private TextView txtPendentes;

    private final FirebaseFirestore banco = FirebaseFirestore.getInstance();
    private final List<Entrega> entregas = new ArrayList<>();
    private EntregaAdapter adapter;
    private ListenerRegistration registro;
    private String colaboradorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_colaborador);
        setTitle(R.string.titulo_ficha);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        colaboradorUid = getIntent().getStringExtra(EXTRA_COLABORADOR_UID);
        if (colaboradorUid == null) {
            finish();
            return;
        }

        lista = findViewById(R.id.listaEntregas);
        progresso = findViewById(R.id.progresso);
        blocoVazio = findViewById(R.id.blocoVazio);
        txtVazioTitulo = findViewById(R.id.txtVazioTitulo);
        txtVazioDica = findViewById(R.id.txtVazioDica);
        txtNome = findViewById(R.id.txtNome);
        txtDados = findViewById(R.id.txtDados);
        txtTotal = findViewById(R.id.txtTotal);
        txtConfirmadas = findViewById(R.id.txtConfirmadas);
        txtPendentes = findViewById(R.id.txtPendentes);

        adapter = new EntregaAdapter(entregas, null);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        findViewById(R.id.btnNovaEntrega).setOnClickListener(v -> {
            Intent intent = new Intent(this, NovaEntregaActivity.class);
            intent.putExtra(NovaEntregaActivity.EXTRA_COLABORADOR_UID, colaboradorUid);
            startActivity(intent);
        });

        carregarColaborador();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (colaboradorUid == null) {
            return;
        }
        registro = banco.collection(Colecoes.ENTREGAS)
                .whereEqualTo("colaboradorUid", colaboradorUid)
                .addSnapshotListener((snapshot, erro) -> {
                    progresso.setVisibility(View.GONE);
                    if (erro != null) {
                        mostrarVazio(getString(R.string.msg_erro_generico), MensagensFirebase.traduzir(erro));
                        return;
                    }
                    entregas.clear();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot documento : snapshot) {
                            entregas.add(documento.toObject(Entrega.class));
                        }
                    }
                    entregas.sort((a, b) -> compararDatas(b, a));
                    adapter.notifyDataSetChanged();
                    atualizarContadores();
                    if (entregas.isEmpty()) {
                        mostrarVazio(getString(R.string.vazio_ficha_titulo), getString(R.string.vazio_ficha_dica));
                    } else {
                        blocoVazio.setVisibility(View.GONE);
                        lista.setVisibility(View.VISIBLE);
                    }
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

    private void carregarColaborador() {
        banco.collection(Colecoes.COLABORADORES)
                .document(colaboradorUid)
                .get()
                .addOnSuccessListener(documento -> {
                    Colaborador colaborador = documento.toObject(Colaborador.class);
                    if (colaborador == null) {
                        Toast.makeText(this, "Colaborador não encontrado", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    txtNome.setText(colaborador.getNome());
                    txtDados.setText("Matrícula " + colaborador.getMatricula() + " · " + colaborador.getSetor()
                            + "\n" + colaborador.getEmail());
                })
                .addOnFailureListener(erro ->
                        Toast.makeText(this, MensagensFirebase.traduzir(erro), Toast.LENGTH_LONG).show());
    }

    private void atualizarContadores() {
        int confirmadas = 0;
        for (Entrega entrega : entregas) {
            if (entrega.isConfirmado()) {
                confirmadas++;
            }
        }
        txtTotal.setText(String.valueOf(entregas.size()));
        txtConfirmadas.setText(String.valueOf(confirmadas));
        txtPendentes.setText(String.valueOf(entregas.size() - confirmadas));
    }

    private int compararDatas(Entrega a, Entrega b) {
        if (a.getData() == null || b.getData() == null) {
            return 0;
        }
        return a.getData().compareTo(b.getData());
    }

    private void mostrarVazio(String titulo, String dica) {
        txtVazioTitulo.setText(titulo);
        txtVazioDica.setText(dica);
        blocoVazio.setVisibility(View.VISIBLE);
        lista.setVisibility(View.GONE);
    }
}
