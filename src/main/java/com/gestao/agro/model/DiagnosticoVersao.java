package com.gestao.agro.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiagnosticoVersao {
    private Integer id;
    private Integer organizacaoId;
    private Integer produtorId;
    private int numeroVersao;
    private LocalDate dataAplicacao;
    private String consultorResponsavel;
    private String status;
    private String syncStatus;

    // Mapa auxiliar com a média consolidada de cada uma das 10 dimensões
    private final Map<String, Double> mediasPorDimensao = new LinkedHashMap<>();

    public DiagnosticoVersao() {
        this.dataAplicacao = LocalDate.now();
        this.status = "EM_ANDAMENTO";
        this.syncStatus = "PENDENTE";
        this.numeroVersao = 1;
    }

    // --- Métodos de Análise de Maturidade ---

    public void adicionarMediaDimensao(String dimensao, double media) {
        mediasPorDimensao.put(dimensao, Math.round(media * 100.0) / 100.0);
    }

    public Map<String, Double> getMediasPorDimensao() {
        return mediasPorDimensao;
    }

    public double getMediaGeral() {
        if (mediasPorDimensao.isEmpty()) return 0.0;
        double soma = 0.0;
        for (double valor : mediasPorDimensao.values()) {
            soma += valor;
        }
        return Math.round((soma / mediasPorDimensao.size()) * 100.0) / 100.0;
    }

    public String getNivelMaturidade() {
        double geral = getMediaGeral();
        if (geral < 2.5) return "Crítico (Atenção Imediata)";
        if (geral < 3.5) return "Básico (Em Estruturação)";
        if (geral < 4.5) return "Intermediário (Estável)";
        return "Consolidado (Excelência)";
    }

    public String getCorMaturidadeHex() {
        double geral = getMediaGeral();
        if (geral < 2.5) return "#D32F2F"; // Vermelho
        if (geral < 3.5) return "#F57C00"; // Laranja
        if (geral < 4.5) return "#1976D2"; // Azul
        return "#2E7D32";                 // Verde
    }

    // --- Getters e Setters Originais ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Integer organizacaoId) { this.organizacaoId = organizacaoId; }

    public Integer getProdutorId() { return produtorId; }
    public void setProdutorId(Integer produtorId) { this.produtorId = produtorId; }

    public int getNumeroVersao() { return numeroVersao; }
    public void setNumeroVersao(int numeroVersao) { this.numeroVersao = numeroVersao; }

    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }

    public String getConsultorResponsavel() { return consultorResponsavel; }
    public void setConsultorResponsavel(String consultorResponsavel) { this.consultorResponsavel = consultorResponsavel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}