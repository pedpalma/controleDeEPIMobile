package br.senai.sp.controleepi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import br.senai.sp.controleepi.model.Usuario;
import br.senai.sp.controleepi.util.Colecoes;
import br.senai.sp.controleepi.util.Sessao;

public class MenuTecnicoActivity extends AppCompatActivity {

    private TextView txtSaudacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_tecnico);
        setTitle(R.string.titulo_menu_tecnico);

        txtSaudacao = findViewById(R.id.txtSaudacao);

        findViewById(R.id.cardEpis).setOnClickListener(v ->
                startActivity(new Intent(this, EpisActivity.class)));

        carregarNome();
    }

    private void carregarNome() {
        String uid = Sessao.uid();
        if (uid == null) {
            Sessao.sair(this);
            return;
        }
        FirebaseFirestore.getInstance()
                .collection(Colecoes.USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {
                    Usuario usuario = documento.toObject(Usuario.class);
                    if (usuario != null && usuario.getNome() != null) {
                        txtSaudacao.setText("Olá, " + primeiroNome(usuario.getNome()) + "!");
                    }
                });
    }

    private String primeiroNome(String nome) {
        String[] partes = nome.trim().split("\\s+");
        return partes[0];
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
