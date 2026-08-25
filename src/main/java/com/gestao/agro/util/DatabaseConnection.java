package com.gestao.agro.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:gestao_cooperativas.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String sqlTabelas = """
            CREATE TABLE IF NOT EXISTS organizacao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                municipio_comunidade TEXT,
                numero_associados INTEGER,
                principais_produtos TEXT,
                canais_comercializacao TEXT,
                possui_caf INTEGER,
                situacao_documental TEXT,
                infraestrutura TEXT,
                faturamento_estimado REAL,
                mercados_atendidos TEXT,
                participacao_paa_pnae INTEGER,
                principais_parceiros TEXT,
                dificuldades TEXT,
                sync_status TEXT DEFAULT 'PENDENTE'
            );

            CREATE TABLE IF NOT EXISTS pestel_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                dimensao TEXT NOT NULL,
                descricao TEXT NOT NULL,
                tipo TEXT NOT NULL,
                grau_impacto INTEGER,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
            );

            CREATE TABLE IF NOT EXISTS fofa_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                tipo TEXT NOT NULL,
                descricao TEXT NOT NULL,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
            );

            CREATE TABLE IF NOT EXISTS gut_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                problema TEXT NOT NULL,
                gravidade INTEGER NOT NULL,
                urgencia INTEGER NOT NULL,
                tendencia INTEGER NOT NULL,
                score_gut INTEGER NOT NULL,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
            );

            CREATE TABLE IF NOT EXISTS plano_5w2h (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                o_que TEXT NOT NULL,
                por_que TEXT,
                onde TEXT,
                quando TEXT,
                quem TEXT,
                como TEXT,
                quanto REAL,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : sqlTabelas.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
            System.out.println("Banco SQLite inicializado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar SQLite: " + e.getMessage());
        }
    }
}