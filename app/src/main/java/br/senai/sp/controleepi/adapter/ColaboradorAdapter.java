package br.senai.sp.controleepi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import br.senai.sp.controleepi.R;
import br.senai.sp.controleepi.model.Colaborador;

public class ColaboradorAdapter extends RecyclerView.Adapter<ColaboradorAdapter.ColaboradorViewHolder> {

    public interface AoClicar {
        void aoClicar(Colaborador colaborador);
    }

    private final List<Colaborador> lista;
    private final AoClicar aoClicar;

    public ColaboradorAdapter(List<Colaborador> lista, AoClicar aoClicar) {
        this.lista = lista;
        this.aoClicar = aoClicar;
    }

    public static class ColaboradorViewHolder extends RecyclerView.ViewHolder {
        TextView txtInicial, txtNome, txtMatriculaSetor;

        public ColaboradorViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInicial = itemView.findViewById(R.id.txtInicial);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtMatriculaSetor = itemView.findViewById(R.id.txtMatriculaSetor);
        }
    }

    @NonNull
    @Override
    public ColaboradorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_colaborador, parent, false);
        return new ColaboradorViewHolder(card);
    }

    @Override
    public void onBindViewHolder(@NonNull ColaboradorViewHolder holder, int position) {
        Colaborador colaborador = lista.get(position);

        String nome = colaborador.getNome() == null ? "" : colaborador.getNome();
        holder.txtInicial.setText(nome.isEmpty() ? "?" : nome.substring(0, 1).toUpperCase(Locale.ROOT));
        holder.txtNome.setText(nome);
        holder.txtMatriculaSetor.setText("Matrícula " + colaborador.getMatricula() + " · " + colaborador.getSetor());

        if (aoClicar != null) {
            holder.itemView.setOnClickListener(v -> aoClicar.aoClicar(colaborador));
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
