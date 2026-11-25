package br.senai.sp.controleepi;

import android.os.Bundle;
import android.view.View;
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

import br.senai.sp.controleepi.adapter.EpiAdapter;
import br.senai.sp.controleepi.model.Epi;
import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.MensagensFirebase;

public class EpisActivity extends AppCompatActivity {

    private RecyclerView lista;
    private ProgressBar progresso;
    private View blocoVazio;
    private TextView txtVazioTitulo;
    private TextView txtVazioDica;

    private final List<Epi> epis = new ArrayList<>();
    private EpiAdapter adapter;
    private ListenerRegistration registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epis);
        setTitle(R.string.titulo_epis);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lista = findViewById(R.id.listaEpis);
        progresso = findViewById(R.id.progresso);
        blocoVazio = findViewById(R.id.blocoVazio);
        txtVazioTitulo = findViewById(R.id.txtVazioTitulo);
        txtVazioDica = findViewById(R.id.txtVazioDica);

        adapter = new EpiAdapter(epis, null);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registro = FirebaseFirestore.getInstance()
                .collection(Colecoes.EPIS)
                .orderBy("descricao")
                .addSnapshotListener((snapshot, erro) -> {
                    progresso.setVisibility(View.GONE);
                    if (erro != null) {
                        mostrarVazio(getString(R.string.msg_erro_generico), MensagensFirebase.traduzir(erro));
                        return;
                    }
                    epis.clear();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot documento : snapshot) {
                            epis.add(documento.toObject(Epi.class));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (epis.isEmpty()) {
                        mostrarVazio(getString(R.string.vazio_epis_titulo), getString(R.string.vazio_epis_dica));
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

    private void mostrarVazio(String titulo, String dica) {
        txtVazioTitulo.setText(titulo);
        txtVazioDica.setText(dica);
        blocoVazio.setVisibility(View.VISIBLE);
        lista.setVisibility(View.GONE);
    }
}
