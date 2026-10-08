/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gestao.agro.model;

/**
 *
 * @author Jean
 */
import java.time.LocalDate;

public class FatorSwot {

    private Integer id;
    private Integer organizacaoId;
    private Integer produtorId;
    private String tipo;           // FORCA, FRAQUEZA, OPORTUNIDADE, AMEACA
    private String descricao;
    private int intensidade;       // 1 (Baixa) a 5 (Muito Alta)
    private String origem;         // IMO-AF, PESTEL, MANUAL
    private LocalDate dataRegistro;

    public FatorSwot() {
        this.dataRegistro = LocalDate.now();
        this.intensidade = 3;
        this.origem = "MANUAL";
    }

    public FatorSwot(Integer organizacaoId, Integer produtorId, String tipo, 
                     String descricao, int intensidade, String origem) {
        this.organizacaoId = organizacaoId;
        this.produtorId = produtorId;
        this.tipo = tipo;
        this.descricao = descricao;
        this.intensidade = intensidade;
        this.origem = origem;
        this.dataRegistro = LocalDate.now();
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Integer organizacaoId) { this.organizacaoId = organizacaoId; }

    public Integer getProdutorId() { return produtorId; }
    public void setProdutorId(Integer produtorId) { this.produtorId = produtorId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getIntensidade() { return intensidade; }
    public void setIntensidade(int intensidade) { this.intensidade = intensidade; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }

    @Override
    public String toString() {
        return "[" + tipo + "] " + descricao;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FatorSwot fatorSwot = (FatorSwot) o;
        return id != null && id.equals(fatorSwot.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}