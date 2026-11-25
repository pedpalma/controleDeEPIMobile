package br.senai.sp.controleepi;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.senai.sp.controleepi.util.Sessao;

public class AguardandoPerfilActivity extends AppCompatActivity {

    private Button btnVerificar;
    private ProgressBar progresso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aguardando_perfil);

        TextView txtEmailConta = findViewById(R.id.txtEmailConta);
        btnVerificar = findViewById(R.id.btnVerificar);
        progresso = findViewById(R.id.progresso);

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null) {
            txtEmailConta.setText(usuario.getEmail());
        }

        btnVerificar.setOnClickListener(v -> {
            carregando(true);
            Sessao.entrar(this, () -> {
                carregando(false);
                Toast.makeText(this, R.string.msg_sem_internet, Toast.LENGTH_LONG).show();
            });
        });
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

    private void carregando(boolean ativo) {
        progresso.setVisibility(ativo ? View.VISIBLE : View.GONE);
        btnVerificar.setEnabled(!ativo);
    }
}
