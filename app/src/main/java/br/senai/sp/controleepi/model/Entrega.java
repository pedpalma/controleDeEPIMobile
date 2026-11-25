package br.senai.sp.controleepi.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Entrega {

    @DocumentId
    private String id;
    private String colaboradorUid;
    private String colaboradorNome;
    private String epiId;
    private String epiDescricao;
    private String epiCa;
    private long quantidade;
    private Timestamp data;
    private String motivo;
    private boolean confirmado;
    private Timestamp confirmadoEm;
    private String tecnicoUid;

    public Entrega() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColaboradorUid() {
        return colaboradorUid;
    }

    public void setColaboradorUid(String colaboradorUid) {
        this.colaboradorUid = colaboradorUid;
    }

    public String getColaboradorNome() {
        return colaboradorNome;
    }

    public void setColaboradorNome(String colaboradorNome) {
        this.colaboradorNome = colaboradorNome;
    }

    public String getEpiId() {
        return epiId;
    }

    public void setEpiId(String epiId) {
        this.epiId = epiId;
    }

    public String getEpiDescricao() {
        return epiDescricao;
    }

    public void setEpiDescricao(String epiDescricao) {
        this.epiDescricao = epiDescricao;
    }

    public String getEpiCa() {
        return epiCa;
    }

    public void setEpiCa(String epiCa) {
        this.epiCa = epiCa;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }

    public Timestamp getConfirmadoEm() {
        return confirmadoEm;
    }

    public void setConfirmadoEm(Timestamp confirmadoEm) {
        this.confirmadoEm = confirmadoEm;
    }

    public String getTecnicoUid() {
        return tecnicoUid;
    }

    public void setTecnicoUid(String tecnicoUid) {
        this.tecnicoUid = tecnicoUid;
    }
}
