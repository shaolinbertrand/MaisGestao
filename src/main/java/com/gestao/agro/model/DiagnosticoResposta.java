package com.gestao.agro.model;

public class DiagnosticoResposta {
    private Integer id;
    private int diagnosticoVersaoId;
    private String dimensao;       // Ex: "1. GOVERNANÇA", "2. GESTÃO FINANCEIRA", etc.
    private String subdimensao;    // Item ou pergunta avaliada
    private int pontuacao;         // 1 a 5
    private String observacoesEvidencias;
    private String planoAcaoRecomendado;

    public DiagnosticoResposta() {}

    public DiagnosticoResposta(int diagnosticoVersaoId, String dimensao, String subdimensao, int pontuacao, String observacoesEvidencias, String planoAcaoRecomendado) {
        this.diagnosticoVersaoId = diagnosticoVersaoId;
        this.dimensao = dimensao;
        this.subdimensao = subdimensao;
        this.pontuacao = pontuacao;
        this.observacoesEvidencias = observacoesEvidencias;
        this.planoAcaoRecomendado = planoAcaoRecomendado;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getDiagnosticoVersaoId() { return diagnosticoVersaoId; }
    public void setDiagnosticoVersaoId(int diagnosticoVersaoId) { this.diagnosticoVersaoId = diagnosticoVersaoId; }

    public String getDimensao() { return dimensao; }
    public void setDimensao(String dimensao) { this.dimensao = dimensao; }

    public String getSubdimensao() { return subdimensao; }
    public void setSubdimensao(String subdimensao) { this.subdimensao = subdimensao; }

    public int getPontuacao() { return pontuacao; }
    public void setPontuacao(int pontuacao) { this.pontuacao = pontuacao; }

    public String getObservacoesEvidencias() { return observacoesEvidencias; }
    public void setObservacoesEvidencias(String observacoesEvidencias) { this.observacoesEvidencias = observacoesEvidencias; }

    public String getPlanoAcaoRecomendado() { return planoAcaoRecomendado; }
    public void setPlanoAcaoRecomendado(String planoAcaoRecomendado) { this.planoAcaoRecomendado = planoAcaoRecomendado; }
}