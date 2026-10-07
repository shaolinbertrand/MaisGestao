package com.gestao.agro.model;

public class ItemPerfilMaturidade {

    private String dimensao;
    private double media;
    private String nivel;

    public ItemPerfilMaturidade(String dimensao, double media, String nivel) {
        this.dimensao = dimensao;
        this.media = media;
        this.nivel = nivel;
    }

    public String getDimensao() {
        return dimensao;
    }

    public void setDimensao(String dimensao) {
        this.dimensao = dimensao;
    }

    public double getMedia() {
        return media;
    }

    public void setMedia(double media) {
        this.media = media;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}