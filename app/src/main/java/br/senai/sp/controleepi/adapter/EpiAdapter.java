package br.senai.sp.controleepi.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.senai.sp.controleepi.R;
import br.senai.sp.controleepi.model.Epi;
import br.senai.sp.controleepi.util.Datas;

public class EpiAdapter extends RecyclerView.Adapter<EpiAdapter.EpiViewHolder> {

    public interface AoClicar {
        void aoClicar(Epi epi);
    }

    private final List<Epi> lista;
    private final AoClicar aoClicar;

    public EpiAdapter(List<Epi> lista, AoClicar aoClicar) {
        this.lista = lista;
        this.aoClicar = aoClicar;
    }

    public static class EpiViewHolder extends RecyclerView.ViewHolder {
        TextView txtDescricao, txtCa, txtSituacao;

        public EpiViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtCa = itemView.findViewById(R.id.txtCa);
            txtSituacao = itemView.findViewById(R.id.txtSituacao);
        }
    }

    @NonNull
    @Override
    public EpiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_epi, parent, false);
        return new EpiViewHolder(card);
    }

    @Override
    public void onBindViewHolder(@NonNull EpiViewHolder holder, int position) {
        Epi epi = lista.get(position);

        holder.txtDescricao.setText(epi.getDescricao());
        holder.txtCa.setText("CA " + epi.getCa() + " · validade " + Datas.formatar(epi.getValidadeCa()));

        holder.txtSituacao.setText(Datas.situacaoCa(epi.getValidadeCa()));
        holder.txtSituacao.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), corDaSituacao(epi))));

        if (aoClicar != null) {
            holder.itemView.setOnClickListener(v -> aoClicar.aoClicar(epi));
        }
    }

    private int corDaSituacao(Epi epi) {
        long dias = Datas.diasAte(epi.getValidadeCa());
        if (epi.getValidadeCa() == null || dias < 0) {
            return R.color.situacao_vencido;
        }
        if (dias <= Datas.DIAS_ALERTA_CA) {
            return R.color.situacao_alerta;
        }
        return R.color.situacao_ok;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
