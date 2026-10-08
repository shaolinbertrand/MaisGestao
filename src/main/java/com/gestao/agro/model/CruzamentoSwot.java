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

public class CruzamentoSwot {

    private Integer id;
    private Integer organizacaoId;
    private Integer produtorId;
    private String tipoEstrategia;     // FO (Alavancagem), FA (Proteção), WO (Recuperação), WA (Sobrevivência)
    private Integer fatorInternoId;    // ID de uma Força ou Fraqueza
    private Integer fatorExternoId;    // ID de uma Oportunidade ou Ameaça
    private String descricaoFatorInterno;
    private String descricaoFatorExterno;
    private String estrategiaProposta;
    private LocalDate dataRegistro;

    public CruzamentoSwot() {
        this.dataRegistro = LocalDate.now();
    }

    public CruzamentoSwot(Integer organizacaoId, Integer produtorId, String tipoEstrategia, 
                          Integer fatorInternoId, Integer fatorExternoId, 
                          String descricaoFatorInterno, String descricaoFatorExterno, 
                          String estrategiaProposta) {
        this.organizacaoId = organizacaoId;
        this.produtorId = produtorId;
        this.tipoEstrategia = tipoEstrategia;
        this.fatorInternoId = fatorInternoId;
        this.fatorExternoId = fatorExternoId;
        this.descricaoFatorInterno = descricaoFatorInterno;
        this.descricaoFatorExterno = descricaoFatorExterno;
        this.estrategiaProposta = estrategiaProposta;
        this.dataRegistro = LocalDate.now();
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Integer organizacaoId) { this.organizacaoId = organizacaoId; }

    public Integer getProdutorId() { return produtorId; }
    public void setProdutorId(Integer produtorId) { this.produtorId = produtorId; }

    public String getTipoEstrategia() { return tipoEstrategia; }
    public void setTipoEstrategia(String tipoEstrategia) { this.tipoEstrategia = tipoEstrategia; }

    public Integer getFatorInternoId() { return fatorInternoId; }
    public void setFatorInternoId(Integer fatorInternoId) { this.fatorInternoId = fatorInternoId; }

    public Integer getFatorExternoId() { return fatorExternoId; }
    public void setFatorExternoId(Integer fatorExternoId) { this.fatorExternoId = fatorExternoId; }

    public String getDescricaoFatorInterno() { return descricaoFatorInterno; }
    public void setDescricaoFatorInterno(String descricaoFatorInterno) { this.descricaoFatorInterno = descricaoFatorInterno; }

    public String getDescricaoFatorExterno() { return descricaoFatorExterno; }
    public void setDescricaoFatorExterno(String descricaoFatorExterno) { this.descricaoFatorExterno = descricaoFatorExterno; }

    public String getEstrategiaProposta() { return estrategiaProposta; }
    public void setEstrategiaProposta(String estrategiaProposta) { this.estrategiaProposta = estrategiaProposta; }

    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }
}
