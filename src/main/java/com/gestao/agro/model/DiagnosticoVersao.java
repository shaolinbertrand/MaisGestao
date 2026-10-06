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

    // Mapa com a média consolidada (MD_i) de cada uma das 15 dimensões
    private final Map<String, Double> mediasPorDimensao = new LinkedHashMap<>();

    public DiagnosticoVersao() {
        this.dataAplicacao = LocalDate.now();
        this.status = "EM_ANDAMENTO";
        this.syncStatus = "PENDENTE";
        this.numeroVersao = 1;
    }

    // --- Métodos de Análise e Cálculo IMO-AF ---

    public void adicionarMediaDimensao(String dimensao, double media) {
        mediasPorDimensao.put(dimensao, Math.round(media * 100.0) / 100.0);
    }

    public Map<String, Double> getMediasPorDimensao() {
        return mediasPorDimensao;
    }


    // IMO-AF = (Soma das médias das 15 dimensões) / 15.0
    public double getMediaGeral() {
        double somaMedias = 0.0;
        int dimensoesAvaliadas = 0;

        for (DimensaoDiagnostico dim : DimensaoDiagnostico.values()) {
            Double mediaDim = mediasPorDimensao.get(dim.getTitulo());
            if (mediaDim != null && mediaDim > 0.0) {
                somaMedias += mediaDim;
                dimensoesAvaliadas++;
            }
        }

        if (dimensoesAvaliadas == 0) {
            return 0.0;
        }

        // Se quiser a divisão estrita por 15 (conforme fórmula oficial):
        double resultado = somaMedias / 15.0;

        // Limita defensivamente a 5.0 caso haja qualquer distorção
        if (resultado > 5.0) {
            resultado = 5.0;
        }

        return Math.round(resultado * 100.0) / 100.0;
    }

    // Faixas oficiais de maturidade do IMO-AF
    public String getNivelMaturidade() {
        double geral = getMediaGeral();
        if (geral <= 0.0) return "Aguardando Avaliação";
        if (geral <= 1.80) return "Nível 1 – Inicial";
        if (geral <= 2.60) return "Nível 2 – Emergente";
        if (geral <= 3.40) return "Nível 3 – Estruturado";
        if (geral <= 4.20) return "Nível 4 – Consolidado";
        return "Nível 5 – Avançado";
    }

    // Caracterização qualitativa oficial do nível
    public String getDescricaoMaturidade() {
        double geral = getMediaGeral();
        if (geral <= 0.0) return "Nenhuma resposta registrada ainda.";
        if (geral <= 1.80) return "Práticas inexistentes ou predominantemente informais.";
        if (geral <= 2.60) return "Algumas práticas começam a ser desenvolvidas, mas ainda são pouco sistemáticas.";
        if (geral <= 3.40) return "Existem práticas e processos definidos, embora apresentem lacunas.";
        if (geral <= 4.20) return "A organização possui práticas sistemáticas, acompanhadas e relativamente integradas.";
        return "Práticas consolidadas, integradas, monitoradas e orientadas à melhoria contínua.";
    }

    // Cores de destaque para cada nível
    public String getCorMaturidadeHex() {
        double geral = getMediaGeral();
        if (geral <= 1.80) return "#C62828"; // Vermelho escuro
        if (geral <= 2.60) return "#EF6C00"; // Laranja forte
        if (geral <= 3.40) return "#F9A825"; // Âmbar/Amarelo escuro
        if (geral <= 4.20) return "#2E7D32"; // Verde institucional
        return "#1565C0";                   // Azul consolidação
    }

    // Classificação individual de cada dimensão
    public static String classificarNivelDimensao(double media) {
        if (media <= 0.0) return "Não Avaliado";
        if (media <= 1.80) return "Inicial";
        if (media <= 2.60) return "Emergente";
        if (media <= 3.40) return "Estruturado";
        if (media <= 4.20) return "Consolidado";
        return "Avançado";
    }

    // --- Getters e Setters ---

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