package com.gestao.agro.model;

public class Organizacao {

    private Integer id;
    private String nome;
    private String cnpj;
    private String municipioComunidade;
    private Integer numeroAssociados;
    private String principaisProdutos;
    private String canaisComercializacao;
    private boolean possuiCaf;
    private String situacaoDocumental;
    private String infraestrutura;
    private Double faturamentoEstimado;
    private String mercadosAtendidos;
    private boolean participacaoPaaPnae;
    private String principaisParceiros;
    private String dificuldades;
    private String syncStatus;

    public Organizacao() {
        this.syncStatus = "PENDENTE";
    }

    public Organizacao(String nome, String cnpj, String municipioComunidade, Integer numeroAssociados) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.municipioComunidade = municipioComunidade;
        this.numeroAssociados = numeroAssociados;
        this.syncStatus = "PENDENTE";
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getMunicipioComunidade() {
        return municipioComunidade;
    }

    public void setMunicipioComunidade(String municipioComunidade) {
        this.municipioComunidade = municipioComunidade;
    }

    public Integer getNumeroAssociados() {
        return numeroAssociados;
    }

    public void setNumeroAssociados(Integer numeroAssociados) {
        this.numeroAssociados = numeroAssociados;
    }

    public String getPrincipaisProdutos() {
        return principaisProdutos;
    }

    public void setPrincipaisProdutos(String principaisProdutos) {
        this.principaisProdutos = principaisProdutos;
    }

    public String getCanaisComercializacao() {
        return canaisComercializacao;
    }

    public void setCanaisComercializacao(String canaisComercializacao) {
        this.canaisComercializacao = canaisComercializacao;
    }

    public boolean isPossuiCaf() {
        return possuiCaf;
    }

    public void setPossuiCaf(boolean possuiCaf) {
        this.possuiCaf = possuiCaf;
    }

    public String getSituacaoDocumental() {
        return situacaoDocumental;
    }

    public void setSituacaoDocumental(String situacaoDocumental) {
        this.situacaoDocumental = situacaoDocumental;
    }

    public String getInfraestrutura() {
        return infraestrutura;
    }

    public void setInfraestrutura(String infraestrutura) {
        this.infraestrutura = infraestrutura;
    }

    public Double getFaturamentoEstimado() {
        return faturamentoEstimado;
    }

    public void setFaturamentoEstimado(Double faturamentoEstimado) {
        this.faturamentoEstimado = faturamentoEstimado;
    }

    public String getMercadosAtendidos() {
        return mercadosAtendidos;
    }

    public void setMercadosAtendidos(String mercadosAtendidos) {
        this.mercadosAtendidos = mercadosAtendidos;
    }

    public boolean isParticipacaoPaaPnae() {
        return participacaoPaaPnae;
    }

    public void setParticipacaoPaaPnae(boolean participacaoPaaPnae) {
        this.participacaoPaaPnae = participacaoPaaPnae;
    }

    public String getPrincipaisParceiros() {
        return principaisParceiros;
    }

    public void setPrincipaisParceiros(String principaisParceiros) {
        this.principaisParceiros = principaisParceiros;
    }

    public String getDificuldades() {
        return dificuldades;
    }

    public void setDificuldades(String dificuldades) {
        this.dificuldades = dificuldades;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
}