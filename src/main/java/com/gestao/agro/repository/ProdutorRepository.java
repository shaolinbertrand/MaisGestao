package com.gestao.agro.repository;

import com.gestao.agro.model.Produtor;
import com.gestao.agro.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutorRepository {

    public Produtor salvar(Produtor produtor) throws SQLException {
        String sql = """
            INSERT INTO produtor (
                organizacao_id, nome, cpf, comunidade, possui_caf,
                principais_culturas, area_propriedade, destino_producao,
                consultor_bloqueio, sync_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, produtor.getOrganizacaoId());
            stmt.setString(2, produtor.getNome());
            stmt.setString(3, produtor.getCpf());
            stmt.setString(4, produtor.getComunidade());
            stmt.setInt(5, produtor.isPossuiCaf() ? 1 : 0);
            stmt.setString(6, produtor.getPrincipaisCulturas());
            stmt.setObject(7, produtor.getAreaPropriedade());
            stmt.setString(8, produtor.getDestinoProducao() != null ? produtor.getDestinoProducao() : "COOPERATIVA");
            stmt.setString(9, produtor.getConsultorBloqueio());
            stmt.setString(10, "PENDENTE");

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produtor.setId(rs.getInt(1));
                }
            }
        }
        return produtor;
    }

    public List<Produtor> listarPorOrganizacao(int organizacaoId) throws SQLException {
        List<Produtor> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtor WHERE organizacao_id = ? ORDER BY nome ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, organizacaoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produtor p = new Produtor();
                    p.setId(rs.getInt("id"));
                    p.setOrganizacaoId(rs.getInt("organizacao_id"));
                    p.setNome(rs.getString("nome"));
                    p.setCpf(rs.getString("cpf"));
                    p.setComunidade(rs.getString("comunidade"));
                    p.setPossuiCaf(rs.getInt("possui_caf") == 1);
                    p.setPrincipaisCulturas(rs.getString("principais_culturas"));
                    p.setAreaPropriedade(rs.getDouble("area_propriedade"));
                    p.setDestinoProducao(rs.getString("destino_producao"));
                    p.setConsultorBloqueio(rs.getString("consultor_bloqueio"));
                    p.setSyncStatus(rs.getString("sync_status"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    public void atualizarBloqueioConsultor(int produtorId, String nomeConsultor) throws SQLException {
        String sql = "UPDATE produtor SET consultor_bloqueio = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeConsultor);
            stmt.setInt(2, produtorId);
            stmt.executeUpdate();
        }
    }
}