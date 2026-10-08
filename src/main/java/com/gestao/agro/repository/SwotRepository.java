package com.gestao.agro.repository;

import com.gestao.agro.model.CruzamentoSwot;
import com.gestao.agro.model.FatorPestel;
import com.gestao.agro.model.FatorSwot;
import com.gestao.agro.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SwotRepository {

    public SwotRepository() {
        criarTabelasSeNaoExistirem();
    }

    private void criarTabelasSeNaoExistirem() {
        String sqlSwot = """
            CREATE TABLE IF NOT EXISTS matriz_swot (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                produtor_id INTEGER,
                tipo TEXT NOT NULL,
                descricao TEXT NOT NULL,
                intensidade INTEGER DEFAULT 3,
                origem TEXT DEFAULT 'MANUAL',
                data_registro TEXT,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id),
                FOREIGN KEY (produtor_id) REFERENCES produtor(id)
            );
        """;

        String sqlCruzamento = """
            CREATE TABLE IF NOT EXISTS swot_cruzamento (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                produtor_id INTEGER,
                tipo_estrategia TEXT NOT NULL,
                fator_interno_id INTEGER,
                fator_externo_id INTEGER,
                descricao_interno TEXT,
                descricao_externo TEXT,
                estrategia_proposta TEXT NOT NULL,
                data_registro TEXT,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id),
                FOREIGN KEY (produtor_id) REFERENCES produtor(id),
                FOREIGN KEY (fator_interno_id) REFERENCES matriz_swot(id) ON DELETE SET NULL,
                FOREIGN KEY (fator_externo_id) REFERENCES matriz_swot(id) ON DELETE SET NULL
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlSwot);
            stmt.execute(sqlCruzamento);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar tabelas SWOT: " + e.getMessage());
        }
    }

    // ==========================================
    //            OPERAÇÕES: FATOR SWOT
    // ==========================================

    public FatorSwot salvarFator(FatorSwot fator) throws SQLException {
        String sql = """
            INSERT INTO matriz_swot (
                organizacao_id, produtor_id, tipo, descricao, intensidade, origem, data_registro
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (fator.getOrganizacaoId() != null) {
                stmt.setInt(1, fator.getOrganizacaoId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (fator.getProdutorId() != null) {
                stmt.setInt(2, fator.getProdutorId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setString(3, fator.getTipo());
            stmt.setString(4, fator.getDescricao());
            stmt.setInt(5, fator.getIntensidade());
            stmt.setString(6, fator.getOrigem() != null ? fator.getOrigem() : "MANUAL");
            stmt.setString(7, fator.getDataRegistro() != null ? fator.getDataRegistro().toString() : LocalDate.now().toString());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    fator.setId(rs.getInt(1));
                }
            }

            if (fator.getId() == null || fator.getId() == 0) {
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        fator.setId(rs.getInt(1));
                    }
                }
            }
        }
        return fator;
    }

    public void atualizarFator(FatorSwot fator) throws SQLException {
        String sql = """
            UPDATE matriz_swot SET
                tipo = ?,
                descricao = ?,
                intensidade = ?,
                origem = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fator.getTipo());
            stmt.setString(2, fator.getDescricao());
            stmt.setInt(3, fator.getIntensidade());
            stmt.setString(4, fator.getOrigem());
            stmt.setInt(5, fator.getId());

            stmt.executeUpdate();
        }
    }

    public void excluirFator(int id) throws SQLException {
        String sql = "DELETE FROM matriz_swot WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<FatorSwot> listarFatoresPorOrganizacao(int orgId) throws SQLException {
        List<FatorSwot> lista = new ArrayList<>();
        String sql = "SELECT * FROM matriz_swot WHERE organizacao_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairFatorSwot(rs));
                }
            }
        }
        return lista;
    }

    public List<FatorSwot> listarFatoresPorProdutor(int prodId) throws SQLException {
        List<FatorSwot> lista = new ArrayList<>();
        String sql = "SELECT * FROM matriz_swot WHERE produtor_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prodId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairFatorSwot(rs));
                }
            }
        }
        return lista;
    }

    // ==========================================
    //      OPERAÇÕES: CRUZAMENTO ESTRATÉGICO
    // ==========================================

    public CruzamentoSwot salvarCruzamento(CruzamentoSwot c) throws SQLException {
        String sql = """
            INSERT INTO swot_cruzamento (
                organizacao_id, produtor_id, tipo_estrategia, fator_interno_id, fator_externo_id,
                descricao_interno, descricao_externo, estrategia_proposta, data_registro
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (c.getOrganizacaoId() != null) {
                stmt.setInt(1, c.getOrganizacaoId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (c.getProdutorId() != null) {
                stmt.setInt(2, c.getProdutorId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setString(3, c.getTipoEstrategia());

            if (c.getFatorInternoId() != null) {
                stmt.setInt(4, c.getFatorInternoId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (c.getFatorExternoId() != null) {
                stmt.setInt(5, c.getFatorExternoId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, c.getDescricaoFatorInterno());
            stmt.setString(7, c.getDescricaoFatorExterno());
            stmt.setString(8, c.getEstrategiaProposta());
            stmt.setString(9, c.getDataRegistro() != null ? c.getDataRegistro().toString() : LocalDate.now().toString());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        }
        return c;
    }

    public void excluirCruzamento(int id) throws SQLException {
        String sql = "DELETE FROM swot_cruzamento WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<CruzamentoSwot> listarCruzamentosPorOrganizacao(int orgId) throws SQLException {
        List<CruzamentoSwot> lista = new ArrayList<>();
        String sql = "SELECT * FROM swot_cruzamento WHERE organizacao_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairCruzamento(rs));
                }
            }
        }
        return lista;
    }

    public List<CruzamentoSwot> listarCruzamentosPorProdutor(int prodId) throws SQLException {
        List<CruzamentoSwot> lista = new ArrayList<>();
        String sql = "SELECT * FROM swot_cruzamento WHERE produtor_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prodId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairCruzamento(rs));
                }
            }
        }
        return lista;
    }

    // ==========================================
    //     IMPORTAÇÃO AUTOMÁTICA DA PESTEL
    // ==========================================

    public int importarFatoresDaPestel(Integer orgId, Integer prodId) throws SQLException {
        PestelRepository pestelRepo = new PestelRepository();
        List<FatorPestel> fatoresPestel = (orgId != null) 
            ? pestelRepo.listarPorOrganizacao(orgId) 
            : pestelRepo.listarPorProdutor(prodId);

        List<FatorSwot> swotExistentes = (orgId != null)
            ? listarFatoresPorOrganizacao(orgId)
            : listarFatoresPorProdutor(prodId);

        int totalImportados = 0;

        for (FatorPestel p : fatoresPestel) {
            String tipoSwot = "OPORTUNIDADE".equalsIgnoreCase(p.getTipo()) ? "OPORTUNIDADE" : "AMEACA";
            String descricaoFormatada = "[" + p.getDimensao() + "] " + p.getDescricao();

            boolean jaExiste = swotExistentes.stream().anyMatch(s -> 
                s.getTipo().equalsIgnoreCase(tipoSwot) && s.getDescricao().equalsIgnoreCase(descricaoFormatada)
            );

            if (!jaExiste) {
                FatorSwot novo = new FatorSwot();
                novo.setOrganizacaoId(orgId);
                novo.setProdutorId(prodId);
                novo.setTipo(tipoSwot);
                novo.setDescricao(descricaoFormatada);
                novo.setIntensidade(p.getImpacto());
                novo.setOrigem("PESTEL");
                salvarFator(novo);
                totalImportados++;
            }
        }

        return totalImportados;
    }

    // ==========================================
    //            MÉTODOS AUXILIARES
    // ==========================================

    private FatorSwot extrairFatorSwot(ResultSet rs) throws SQLException {
        FatorSwot f = new FatorSwot();
        f.setId(rs.getInt("id"));

        int orgId = rs.getInt("organizacao_id");
        f.setOrganizacaoId(rs.wasNull() ? null : orgId);

        int prodId = rs.getInt("produtor_id");
        f.setProdutorId(rs.wasNull() ? null : prodId);

        f.setTipo(rs.getString("tipo"));
        f.setDescricao(rs.getString("descricao"));
        f.setIntensidade(rs.getInt("intensidade"));
        f.setOrigem(rs.getString("origem"));

        String dtStr = rs.getString("data_registro");
        if (dtStr != null && !dtStr.isBlank()) {
            try {
                f.setDataRegistro(LocalDate.parse(dtStr));
            } catch (Exception ignored) {}
        }
        return f;
    }

    private CruzamentoSwot extrairCruzamento(ResultSet rs) throws SQLException {
        CruzamentoSwot c = new CruzamentoSwot();
        c.setId(rs.getInt("id"));

        int orgId = rs.getInt("organizacao_id");
        c.setOrganizacaoId(rs.wasNull() ? null : orgId);

        int prodId = rs.getInt("produtor_id");
        c.setProdutorId(rs.wasNull() ? null : prodId);

        c.setTipoEstrategia(rs.getString("tipo_estrategia"));

        int fIntId = rs.getInt("fator_interno_id");
        c.setFatorInternoId(rs.wasNull() ? null : fIntId);

        int fExtId = rs.getInt("fator_externo_id");
        c.setFatorExternoId(rs.wasNull() ? null : fExtId);

        c.setDescricaoFatorInterno(rs.getString("descricao_interno"));
        c.setDescricaoFatorExterno(rs.getString("descricao_externo"));
        c.setEstrategiaProposta(rs.getString("estrategia_proposta"));

        String dtStr = rs.getString("data_registro");
        if (dtStr != null && !dtStr.isBlank()) {
            try {
                c.setDataRegistro(LocalDate.parse(dtStr));
            } catch (Exception ignored) {}
        }
        return c;
    }
}