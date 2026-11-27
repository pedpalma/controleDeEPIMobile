package br.senai.sp.controleepi;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.senai.sp.controleepi.model.Colaborador;
import br.senai.sp.controleepi.model.Epi;
import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.Datas;
import br.senai.sp.controleepi.util.MensagensFirebase;
import br.senai.sp.controleepi.util.Sessao;
import br.senai.sp.controleepi.util.Validacao;

public class NovaEntregaActivity extends AppCompatActivity {

    public static final String EXTRA_COLABORADOR_UID = "colaboradorUid";
    private static final String CHAVE_DATA = "data";
    private static final String CHAVE_COLABORADOR = "colaboradorSelecionado";
    private static final String CHAVE_EPI = "epiSelecionado";

    private Spinner spinnerColaborador;
    private Spinner spinnerEpi;
    private Spinner spinnerMotivo;
    private TextInputLayout campoQuantidade;
    private TextInputLayout campoData;
    private EditText editData;
    private Button btnRegistrar;
    private ProgressBar progresso;

    private final FirebaseFirestore banco = FirebaseFirestore.getInstance();
    private final List<Colaborador> colaboradores = new ArrayList<>();
    private final List<Epi> epis = new ArrayList<>();
    private LocalDate dataEntrega = LocalDate.now();
    private String colaboradorPendente;
    private String epiPendente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_entrega);
        setTitle(R.string.titulo_nova_entrega);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerColaborador = findViewById(R.id.spinnerColaborador);
        spinnerEpi = findViewById(R.id.spinnerEpi);
        spinnerMotivo = findViewById(R.id.spinnerMotivo);
        campoQuantidade = findViewById(R.id.campoQuantidade);
        campoData = findViewById(R.id.campoData);
        editData = findViewById(R.id.editData);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progresso = findViewById(R.id.progresso);

        spinnerMotivo.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner,
                getResources().getStringArray(R.array.motivos_entrega)));

        if (savedInstanceState != null) {
            LocalDate salva = Datas.deTexto(savedInstanceState.getString(CHAVE_DATA));
            if (salva != null) {
                dataEntrega = salva;
            }
            colaboradorPendente = savedInstanceState.getString(CHAVE_COLABORADOR);
            epiPendente = savedInstanceState.getString(CHAVE_EPI);
        } else {
            colaboradorPendente = getIntent().getStringExtra(EXTRA_COLABORADOR_UID);
        }
        editData.setText(Datas.formatar(dataEntrega));

        editData.setOnClickListener(v -> escolherData());
        btnRegistrar.setOnClickListener(v -> registrar());

        carregarListas();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CHAVE_DATA, Datas.paraTexto(dataEntrega));
        Colaborador colaborador = colaboradorSelecionado();
        outState.putString(CHAVE_COLABORADOR, colaborador != null ? colaborador.getUid() : colaboradorPendente);
        Epi epi = epiSelecionado();
        outState.putString(CHAVE_EPI, epi != null ? epi.getId() : epiPendente);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregarListas() {
        carregando(true);
        Task<QuerySnapshot> tarefaColaboradores = banco.collection(Colecoes.COLABORADORES).orderBy("nome").get();
        Task<QuerySnapshot> tarefaEpis = banco.collection(Colecoes.EPIS).orderBy("descricao").get();

        Tasks.whenAllSuccess(tarefaColaboradores, tarefaEpis)
                .addOnSuccessListener(resultados -> {
                    carregando(false);
                    colaboradores.clear();
                    colaboradores.addAll(((QuerySnapshot) resultados.get(0)).toObjects(Colaborador.class));
                    epis.clear();
                    epis.addAll(((QuerySnapshot) resultados.get(1)).toObjects(Epi.class));
                    montarSpinners();
                })
                .addOnFailureListener(this::mostrarErro);
    }

    private void montarSpinners() {
        List<String> nomes = new ArrayList<>();
        nomes.add(getString(R.string.selecione_colaborador));
        int posicaoColaborador = 0;
        for (int i = 0; i < colaboradores.size(); i++) {
            Colaborador colaborador = colaboradores.get(i);
            nomes.add(colaborador.getNome() + " (" + colaborador.getMatricula() + ")");
            if (colaborador.getUid().equals(colaboradorPendente)) {
                posicaoColaborador = i + 1;
            }
        }
        spinnerColaborador.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, nomes));
        spinnerColaborador.setSelection(posicaoColaborador);

        List<String> descricoes = new ArrayList<>();
        descricoes.add(getString(R.string.selecione_epi));
        int posicaoEpi = 0;
        for (int i = 0; i < epis.size(); i++) {
            Epi epi = epis.get(i);
            descricoes.add(epi.getDescricao() + " · CA " + epi.getCa());
            if (epi.getId().equals(epiPendente)) {
                posicaoEpi = i + 1;
            }
        }
        spinnerEpi.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, descricoes));
        spinnerEpi.setSelection(posicaoEpi);

        if (colaboradores.isEmpty() || epis.isEmpty()) {
            Toast.makeText(this, "Cadastre pelo menos um colaborador e um EPI antes de registrar entregas.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private Colaborador colaboradorSelecionado() {
        int posicao = spinnerColaborador.getSelectedItemPosition();
        return posicao > 0 && posicao <= colaboradores.size() ? colaboradores.get(posicao - 1) : null;
    }

    private Epi epiSelecionado() {
        int posicao = spinnerEpi.getSelectedItemPosition();
        return posicao > 0 && posicao <= epis.size() ? epis.get(posicao - 1) : null;
    }

    private void escolherData() {
        DatePickerDialog dialogo = new DatePickerDialog(this,
                (picker, ano, mes, dia) -> {
                    dataEntrega = LocalDate.of(ano, mes + 1, dia);
                    editData.setText(Datas.formatar(dataEntrega));
                    campoData.setError(null);
                },
                dataEntrega.getYear(), dataEntrega.getMonthValue() - 1, dataEntrega.getDayOfMonth());
        dialogo.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialogo.show();
    }

    private void registrar() {
        Colaborador colaborador = colaboradorSelecionado();
        Epi epi = epiSelecionado();
        Integer quantidade = Validacao.inteiroPositivo(campoQuantidade, "a quantidade");

        if (colaborador == null) {
            Toast.makeText(this, "Selecione o colaborador", Toast.LENGTH_SHORT).show();
            return;
        }
        if (epi == null) {
            Toast.makeText(this, "Selecione o EPI", Toast.LENGTH_SHORT).show();
            return;
        }
        if (quantidade == null) {
            return;
        }
        if (dataEntrega.isAfter(LocalDate.now())) {
            campoData.setError("A data da entrega não pode ser no futuro");
            return;
        }
        if (Datas.vencido(epi.getValidadeCa())) {
            Toast.makeText(this, "O CA deste EPI está vencido. Atualize o CA antes de entregar.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        carregando(true);
        Map<String, Object> entrega = new HashMap<>();
        entrega.put("colaboradorUid", colaborador.getUid());
        entrega.put("colaboradorNome", colaborador.getNome());
        entrega.put("epiId", epi.getId());
        entrega.put("epiDescricao", epi.getDescricao());
        entrega.put("epiCa", epi.getCa());
        entrega.put("quantidade", quantidade);
        entrega.put("data", Datas.paraTimestamp(dataEntrega));
        entrega.put("motivo", spinnerMotivo.getSelectedItem().toString());
        entrega.put("confirmado", false);
        entrega.put("tecnicoUid", Sessao.uid());

        banco.collection(Colecoes.ENTREGAS)
                .add(entrega)
                .addOnSuccessListener(referencia -> {
                    Toast.makeText(this, "Entrega registrada. Aguardando confirmação do colaborador.",
                            Toast.LENGTH_LONG).show();
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
        btnRegistrar.setEnabled(!ativo);
    }

}
