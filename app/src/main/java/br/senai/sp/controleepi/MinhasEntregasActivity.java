package br.senai.sp.controleepi;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.senai.sp.controleepi.util.Sessao;

public class MinhasEntregasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_entregas);
        setTitle(R.string.titulo_minhas_entregas);

        findViewById(R.id.progresso).setVisibility(View.GONE);
        findViewById(R.id.blocoVazio).setVisibility(View.VISIBLE);
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
