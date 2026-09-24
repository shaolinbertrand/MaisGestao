package com.gestao.agro.repository;

import com.gestao.agro.model.Organizacao;
import com.gestao.agro.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrganizacaoRepository {

    public Organizacao salvar(Organizacao org) throws SQLException {
        String sql = """
            INSERT INTO organizacao (
                nome, cnpj, municipio_comunidade, numero_associados,
                principais_produtos, canais_comercializacao, possui_caf,
                situacao_documental, infraestrutura, faturamento_estimado,
                mercados_atendidos, participacao_paa_pnae, principais_parceiros,
                dificuldades, sync_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, org.getNome());
            stmt.setString(2, org.getCnpj());
            stmt.setString(3, org.getMunicipioComunidade());
            stmt.setObject(4, org.getNumeroAssociados());
            stmt.setString(5, org.getPrincipaisProdutos());
            stmt.setString(6, org.getCanaisComercializacao());
            stmt.setInt(7, org.isPossuiCaf() ? 1 : 0);
            stmt.setString(8, org.getSituacaoDocumental());
            stmt.setString(9, org.getInfraestrutura());
            stmt.setObject(10, org.getFaturamentoEstimado());
            stmt.setString(11, org.getMercadosAtendidos());
            stmt.setInt(12, org.isParticipacaoPaaPnae() ? 1 : 0);
            stmt.setString(13, org.getPrincipaisParceiros());
            stmt.setString(14, org.getDificuldades());
            stmt.setString(15, "PENDENTE");

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    org.setId(rs.getInt(1));
                }
            }
        }
        return org;
    }

    public void atualizar(Organizacao org) throws SQLException {
        String sql = """
            UPDATE organizacao SET
                nome = ?,
                cnpj = ?,
                municipio_comunidade = ?,
                numero_associados = ?,
                principais_produtos = ?,
                canais_comercializacao = ?,
                possui_caf = ?,
                situacao_documental = ?,
                infraestrutura = ?,
                faturamento_estimado = ?,
                mercados_atendidos = ?,
                participacao_paa_pnae = ?,
                principais_parceiros = ?,
                dificuldades = ?,
                sync_status = 'PENDENTE'
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, org.getNome());
            stmt.setString(2, org.getCnpj());
            stmt.setString(3, org.getMunicipioComunidade());
            stmt.setObject(4, org.getNumeroAssociados());
            stmt.setString(5, org.getPrincipaisProdutos());
            stmt.setString(6, org.getCanaisComercializacao());
            stmt.setInt(7, org.isPossuiCaf() ? 1 : 0);
            stmt.setString(8, org.getSituacaoDocumental());
            stmt.setString(9, org.getInfraestrutura());
            stmt.setObject(10, org.getFaturamentoEstimado());
            stmt.setString(11, org.getMercadosAtendidos());
            stmt.setInt(12, org.isParticipacaoPaaPnae() ? 1 : 0);
            stmt.setString(13, org.getPrincipaisParceiros());
            stmt.setString(14, org.getDificuldades());
            stmt.setInt(15, org.getId());

            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM organizacao WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Organizacao buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM organizacao WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairOrganizacao(rs);
                }
            }
        }
        return null;
    }

    public List<Organizacao> listarTodas() throws SQLException {
        List<Organizacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM organizacao ORDER BY nome ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(extrairOrganizacao(rs));
            }
        }
        return lista;
    }

    private Organizacao extrairOrganizacao(ResultSet rs) throws SQLException {
        Organizacao org = new Organizacao();
        org.setId(rs.getInt("id"));
        org.setNome(rs.getString("nome"));
        org.setCnpj(rs.getString("cnpj"));
        org.setMunicipioComunidade(rs.getString("municipio_comunidade"));
        org.setNumeroAssociados(rs.getInt("numero_associados"));
        org.setPrincipaisProdutos(rs.getString("principais_produtos"));
        org.setCanaisComercializacao(rs.getString("canais_comercializacao"));
        org.setPossuiCaf(rs.getInt("possui_caf") == 1);
        org.setSituacaoDocumental(rs.getString("situacao_documental"));
        org.setInfraestrutura(rs.getString("infraestrutura"));
        org.setFaturamentoEstimado(rs.getDouble("faturamento_estimado"));
        org.setMercadosAtendidos(rs.getString("mercados_atendidos"));
        org.setParticipacaoPaaPnae(rs.getInt("participacao_paa_pnae") == 1);
        org.setPrincipaisParceiros(rs.getString("principais_parceiros"));
        org.setDificuldades(rs.getString("dificuldades"));
        org.setSyncStatus(rs.getString("sync_status"));
        return org;
    }
}