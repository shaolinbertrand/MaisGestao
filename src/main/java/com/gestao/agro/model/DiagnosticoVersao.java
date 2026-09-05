/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gestao.agro.model;

/**
 *
 * @author Jean
 */
public class DiagnosticoVersao {
    private Integer id;
    private Integer organizacaoId;
    private String tipoEntidade; // "ASSOCIACAO" ou "PRODUTOR"
    private Integer entidadeId;
    private Integer versaoNumero;
    private String dataAplicacao;
    private String consultorResponsavel;
    private Double resumoMaturidade;
    private String status;

    public DiagnosticoVersao() {
        this.versaoNumero = 1;
        this.status = "EM_ANDAMENTO";
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(Integer organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public String getTipoEntidade() {
        return tipoEntidade;
    }

    public void setTipoEntidade(String tipoEntidade) {
        this.tipoEntidade = tipoEntidade;
    }

    public Integer getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(Integer entidadeId) {
        this.entidadeId = entidadeId;
    }

    public Integer getVersaoNumero() {
        return versaoNumero;
    }

    public void setVersaoNumero(Integer versaoNumero) {
        this.versaoNumero = versaoNumero;
    }

    public String getDataAplicacao() {
        return dataAplicacao;
    }

    public void setDataAplicacao(String dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public String getConsultorResponsavel() {
        return consultorResponsavel;
    }

    public void setConsultorResponsavel(String consultorResponsavel) {
        this.consultorResponsavel = consultorResponsavel;
    }

    public Double getResumoMaturidade() {
        return resumoMaturidade;
    }

    public void setResumoMaturidade(Double resumoMaturidade) {
        this.resumoMaturidade = resumoMaturidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
