package com.gestao.agro.model;

import java.time.LocalDate;

public class FatorPestel {

    private Integer id;
    private Integer organizacaoId;
    private Integer produtorId;
    private String dimensao;       // POLITICO, ECONOMICO, SOCIAL, TECNOLOGICO, ECOLOGICO, LEGAL
    private String tipo;           // OPORTUNIDADE ou AMEACA
    private String descricao;
    private int impacto;           // 1 (Muito Baixo) a 5 (Muito Alto)
    private int probabilidade;     // 1 (Muito Baixa) a 5 (Muito Alta)
    private String acaoSugerida;
    private LocalDate dataRegistro;

    public FatorPestel() {
        this.dataRegistro = LocalDate.now();
        this.impacto = 3;
        this.probabilidade = 3;
    }

    public FatorPestel(Integer organizacaoId, Integer produtorId, String dimensao, 
                       String tipo, String descricao, int impacto, int probabilidade, String acaoSugerida) {
        this.organizacaoId = organizacaoId;
        this.produtorId = produtorId;
        this.dimensao = dimensao;
        this.tipo = tipo;
        this.descricao = descricao;
        this.impacto = impacto;
        this.probabilidade = probabilidade;
        this.acaoSugerida = acaoSugerida;
        this.dataRegistro = LocalDate.now();
    }

    // Índice de Criticidade/Relevância: Impacto x Probabilidade (1 a 25)
    public int getCriticidade() {
        return impacto * probabilidade;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Integer organizacaoId) { this.organizacaoId = organizacaoId; }

    public Integer getProdutorId() { return produtorId; }
    public void setProdutorId(Integer produtorId) { this.produtorId = produtorId; }

    public String getDimensao() { return dimensao; }
    public void setDimensao(String dimensao) { this.dimensao = dimensao; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getImpacto() { return impacto; }
    public void setImpacto(int impacto) { this.impacto = impacto; }

    public int getProbabilidade() { return probabilidade; }
    public void setProbabilidade(int probabilidade) { this.probabilidade = probabilidade; }

    public String getAcaoSugerida() { return acaoSugerida; }
    public void setAcaoSugerida(String acaoSugerida) { this.acaoSugerida = acaoSugerida; }

    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }
}