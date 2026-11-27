package br.senai.sp.controleepi.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.senai.sp.controleepi.R;
import br.senai.sp.controleepi.model.Entrega;
import br.senai.sp.controleepi.util.Datas;

public class EntregaAdapter extends RecyclerView.Adapter<EntregaAdapter.EntregaViewHolder> {

    public interface AoConfirmar {
        void aoConfirmar(Entrega entrega);
    }

    private final List<Entrega> lista;
    private final AoConfirmar aoConfirmar;

    public EntregaAdapter(List<Entrega> lista, AoConfirmar aoConfirmar) {
        this.lista = lista;
        this.aoConfirmar = aoConfirmar;
    }

    public static class EntregaViewHolder extends RecyclerView.ViewHolder {
        TextView txtEpi, txtStatus, txtDetalhes, txtData;
        Button btnConfirmar;

        public EntregaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEpi = itemView.findViewById(R.id.txtEpi);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDetalhes = itemView.findViewById(R.id.txtDetalhes);
            txtData = itemView.findViewById(R.id.txtData);
            btnConfirmar = itemView.findViewById(R.id.btnConfirmar);
        }
    }

    @NonNull
    @Override
    public EntregaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrega, parent, false);
        return new EntregaViewHolder(card);
    }

    @Override
    public void onBindViewHolder(@NonNull EntregaViewHolder holder, int position) {
        Entrega entrega = lista.get(position);

        holder.txtEpi.setText(entrega.getQuantidade() + " × " + entrega.getEpiDescricao());
        holder.txtDetalhes.setText("CA " + entrega.getEpiCa() + " · " + entrega.getMotivo());

        String data = "Entregue em " + Datas.formatar(entrega.getData());
        if (entrega.isConfirmado() && entrega.getConfirmadoEm() != null) {
            data += "\nConfirmado em " + Datas.formatarComHora(entrega.getConfirmadoEm());
        }
        holder.txtData.setText(data);

        int cor = entrega.isConfirmado() ? R.color.situacao_ok : R.color.situacao_alerta;
        holder.txtStatus.setText(entrega.isConfirmado() ? R.string.status_confirmado : R.string.status_pendente);
        holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), cor)));

        if (aoConfirmar != null && !entrega.isConfirmado()) {
            holder.btnConfirmar.setVisibility(View.VISIBLE);
            holder.btnConfirmar.setOnClickListener(v -> aoConfirmar.aoConfirmar(entrega));
        } else {
            holder.btnConfirmar.setVisibility(View.GONE);
            holder.btnConfirmar.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
