package br.senai.sp.controleepi.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Epi {

    @DocumentId
    private String id;
    private String descricao;
    private String ca;
    private Timestamp validadeCa;
    private long estoque;

    public Epi() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCa() {
        return ca;
    }

    public void setCa(String ca) {
        this.ca = ca;
    }

    public Timestamp getValidadeCa() {
        return validadeCa;
    }

    public void setValidadeCa(Timestamp validadeCa) {
        this.validadeCa = validadeCa;
    }

    public long getEstoque() {
        return estoque;
    }

    public void setEstoque(long estoque) {
        this.estoque = estoque;
    }
}
