package br.senai.sp.controleepi;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import br.senai.sp.controleepi.adapter.EntregaAdapter;
import br.senai.sp.controleepi.model.Colaborador;
import br.senai.sp.controleepi.model.Entrega;
import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.Datas;
import br.senai.sp.controleepi.util.MensagensFirebase;
import br.senai.sp.controleepi.util.Sessao;

public class MinhasEntregasActivity extends AppCompatActivity {

    private RecyclerView lista;
    private ProgressBar progresso;
    private View blocoVazio;
    private TextView txtVazioTitulo;
    private TextView txtVazioDica;
    private TextView txtNomeColaborador;
    private TextView txtDadosColaborador;
    private TextView txtPendentes;

    private final FirebaseFirestore banco = FirebaseFirestore.getInstance();
    private final List<Entrega> entregas = new ArrayList<>();
    private EntregaAdapter adapter;
    private ListenerRegistration registro;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_entregas);
        setTitle(R.string.titulo_minhas_entregas);

        uid = Sessao.uid();
        if (uid == null) {
            Sessao.sair(this);
            return;
        }

        lista = findViewById(R.id.listaEntregas);
        progresso = findViewById(R.id.progresso);
        blocoVazio = findViewById(R.id.blocoVazio);
        txtVazioTitulo = findViewById(R.id.txtVazioTitulo);
        txtVazioDica = findViewById(R.id.txtVazioDica);
        txtNomeColaborador = findViewById(R.id.txtNomeColaborador);
        txtDadosColaborador = findViewById(R.id.txtDadosColaborador);
        txtPendentes = findViewById(R.id.txtPendentes);

        adapter = new EntregaAdapter(entregas, this::perguntarConfirmacao);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        carregarCadastro();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid == null) {
            return;
        }
        registro = banco.collection(Colecoes.ENTREGAS)
                .whereEqualTo("colaboradorUid", uid)
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
                    atualizarPendentes();
                    if (entregas.isEmpty()) {
                        mostrarVazio(getString(R.string.vazio_minhas_entregas_titulo),
                                getString(R.string.vazio_minhas_entregas_dica));
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

    private void carregarCadastro() {
        banco.collection(Colecoes.COLABORADORES)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {
                    Colaborador colaborador = documento.toObject(Colaborador.class);
                    if (colaborador == null) {
                        txtDadosColaborador.setText(R.string.cadastro_pendente);
                        return;
                    }
                    txtNomeColaborador.setText(colaborador.getNome());
                    txtDadosColaborador.setText("Matrícula " + colaborador.getMatricula() + " · " + colaborador.getSetor());
                });
    }

    private void atualizarPendentes() {
        int pendentes = 0;
        for (Entrega entrega : entregas) {
            if (!entrega.isConfirmado()) {
                pendentes++;
            }
        }
        if (pendentes == 0) {
            txtPendentes.setText(entregas.isEmpty() ? "" : "Todas as entregas confirmadas");
        } else if (pendentes == 1) {
            txtPendentes.setText("1 entrega aguardando sua confirmação");
        } else {
            txtPendentes.setText(pendentes + " entregas aguardando sua confirmação");
        }
    }

    private void perguntarConfirmacao(Entrega entrega) {
        String mensagem = "Você confirma que recebeu " + entrega.getQuantidade() + " × "
                + entrega.getEpiDescricao() + " (CA " + entrega.getEpiCa() + ") em "
                + Datas.formatar(entrega.getData()) + "?";
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_dialogo_confirmar)
                .setMessage(mensagem)
                .setPositiveButton(R.string.btn_confirmar, (dialogo, botao) -> confirmar(entrega))
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    private void confirmar(Entrega entrega) {
        banco.collection(Colecoes.ENTREGAS)
                .document(entrega.getId())
                .update("confirmado", true, "confirmadoEm", FieldValue.serverTimestamp())
                .addOnSuccessListener(nada ->
                        Toast.makeText(this, "Recebimento confirmado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(erro ->
                        Toast.makeText(this, MensagensFirebase.traduzir(erro), Toast.LENGTH_LONG).show());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.acao_sair) {
            Sessao.sair(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
