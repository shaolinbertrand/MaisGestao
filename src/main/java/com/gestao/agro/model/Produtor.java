package com.gestao.agro.model;
import java.time.LocalDate;
import java.time.Period;
public class Produtor {

    private Integer id;
    private Integer organizacaoId;
    private String nome;
    private String cpf;
    private String comunidade;
    private boolean possuiCaf;
    private LocalDate dataNascimento;
    private String genero;
    private String principaisCulturas;
    private Double areaPropriedade;
    private String destinoProducao; // "COOPERATIVA", "VENDA_DIRETA", "CONSUMO_PROPRIO"
    private String consultorBloqueio;
    private String syncStatus;

    public Produtor() {
        this.destinoProducao = "COOPERATIVA";
        this.syncStatus = "PENDENTE";
    }
    // Calcula a idade dinamicamente
    public Integer getIdade() {
        if (this.dataNascimento == null) {
            return null;
        }
        return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    public Produtor(Integer organizacaoId, String nome, String cpf) {
        this.organizacaoId = organizacaoId;
        this.nome = nome;
        this.cpf = cpf;
        this.destinoProducao = "COOPERATIVA";
        this.syncStatus = "PENDENTE";
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }
    public LocalDate getDataNascimento(){ 
        return dataNascimento; 
    }
    public void setDataNascimento(LocalDate dataNascimento){
        this.dataNascimento = dataNascimento;
    }

    public String getGenero(){
        return genero;
    }
    public void setGenero(String genero){
        this.genero = genero;
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getComunidade() {
        return comunidade;
    }

    public void setComunidade(String comunidade) {
        this.comunidade = comunidade;
    }

    public boolean isPossuiCaf() {
        return possuiCaf;
    }

    public void setPossuiCaf(boolean possuiCaf) {
        this.possuiCaf = possuiCaf;
    }

    public String getPrincipaisCulturas() {
        return principaisCulturas;
    }

    public void setPrincipaisCulturas(String principaisCulturas) {
        this.principaisCulturas = principaisCulturas;
    }

    public Double getAreaPropriedade() {
        return areaPropriedade;
    }

    public void setAreaPropriedade(Double areaPropriedade) {
        this.areaPropriedade = areaPropriedade;
    }

    public String getDestinoProducao() {
        return destinoProducao;
    }

    public void setDestinoProducao(String destinoProducao) {
        this.destinoProducao = destinoProducao;
    }

    public String getConsultorBloqueio() {
        return consultorBloqueio;
    }

    public void setConsultorBloqueio(String consultorBloqueio) {
        this.consultorBloqueio = consultorBloqueio;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
}