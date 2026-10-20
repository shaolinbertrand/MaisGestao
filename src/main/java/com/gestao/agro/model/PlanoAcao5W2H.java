package com.gestao.agro.model;

import java.time.LocalDate;
import java.util.Objects;

public class PlanoAcao5W2H {

    private Integer id;
    private Integer organizacaoId;
    private Integer produtorId;
    private Integer itemGutId;    // Referência ao item priorizado na Matriz GUT (Opcional)

    private String whatAcao;      // What (O quê será feito)
    private String whyMotivo;     // Why (Por que será feito)
    private String whereLocal;    // Where (Onde será feito)
    private String whenPrazo;     // When (Quando será feito / Prazo limite)
    private String whoResponsavel;// Who (Quem será o responsável)
    private String howMetodo;     // How (Como será feito / Etapas)
    private Double howMuchCusto;  // How Much (Quanto vai custar)

    private String status;        // NÃO INICIADO, EM ANDAMENTO, CONCLUÍDO, CANCELADO
    private LocalDate dataRegistro;

    public PlanoAcao5W2H() {
        this.howMuchCusto = 0.0;
        this.status = "NÃO INICIADO";
        this.dataRegistro = LocalDate.now();
    }

    public PlanoAcao5W2H(Integer organizacaoId, Integer produtorId, String whatAcao, 
                         String whyMotivo, String whereLocal, String whenPrazo, 
                         String whoResponsavel, String howMetodo, Double howMuchCusto) {
        this.organizacaoId = organizacaoId;
        this.produtorId = produtorId;
        this.whatAcao = whatAcao;
        this.whyMotivo = whyMotivo;
        this.whereLocal = whereLocal;
        this.whenPrazo = whenPrazo;
        this.whoResponsavel = whoResponsavel;
        this.howMetodo = howMetodo;
        this.howMuchCusto = (howMuchCusto != null) ? howMuchCusto : 0.0;
        this.status = "NÃO INICIADO";
        this.dataRegistro = LocalDate.now();
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Integer organizacaoId) { this.organizacaoId = organizacaoId; }

    public Integer getProdutorId() { return produtorId; }
    public void setProdutorId(Integer produtorId) { this.produtorId = produtorId; }

    public Integer getItemGutId() { return itemGutId; }
    public void setItemGutId(Integer itemGutId) { this.itemGutId = itemGutId; }

    public String getWhatAcao() { return whatAcao; }
    public void setWhatAcao(String whatAcao) { this.whatAcao = whatAcao; }

    public String getWhyMotivo() { return whyMotivo; }
    public void setWhyMotivo(String whyMotivo) { this.whyMotivo = whyMotivo; }

    public String getWhereLocal() { return whereLocal; }
    public void setWhereLocal(String whereLocal) { this.whereLocal = whereLocal; }

    public String getWhenPrazo() { return whenPrazo; }
    public void setWhenPrazo(String whenPrazo) { this.whenPrazo = whenPrazo; }

    public String getWhoResponsavel() { return whoResponsavel; }
    public void setWhoResponsavel(String whoResponsavel) { this.whoResponsavel = whoResponsavel; }

    public String getHowMetodo() { return howMetodo; }
    public void setHowMetodo(String howMetodo) { this.howMetodo = howMetodo; }

    public Double getHowMuchCusto() { return howMuchCusto; }
    public void setHowMuchCusto(Double howMuchCusto) { this.howMuchCusto = howMuchCusto; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }

    // equals e hashCode garantem estabilidade visual nas TableViews (como aprendemos na FOFA/GUT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanoAcao5W2H that = (PlanoAcao5W2H) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "[5W2H] " + whatAcao + " (" + status + ")";
    }
}