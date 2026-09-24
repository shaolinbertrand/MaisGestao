package com.gestao.agro.util;

import java.io.File;
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
        System.out.println("Localizacao do banco SQLite: " + new File("gestao_cooperativas.db").getAbsolutePath());

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Organização
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS organizacao (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    cnpj TEXT,
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
            """);

            // 2. Produtor
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS produtor (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    organizacao_id INTEGER NOT NULL,
                    nome TEXT NOT NULL,
                    cpf TEXT,
                    comunidade TEXT,
                    possui_caf INTEGER,
                    principais_culturas TEXT,
                    area_propriedade REAL,
                    destino_producao TEXT NOT NULL DEFAULT 'COOPERATIVA',
                    data_nascimento TEXT,
                    genero TEXT,     
                    consultor_bloqueio TEXT,
                    sync_status TEXT DEFAULT 'PENDENTE',
                    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id) ON DELETE CASCADE
                );
            """);

            // Migração da tabela diagnostico_versao para corrigir restrições antigas
            boolean precisaRecriar = false;
            try {
                // Se a coluna antiga 'tipo_entidade' existir, a tabela precisa ser recriada
                stmt.executeQuery("SELECT tipo_entidade FROM diagnostico_versao LIMIT 1");
                precisaRecriar = true;
            } catch (SQLException e) {
                precisaRecriar = false;
            }

            if (precisaRecriar) {
                stmt.execute("DROP TABLE IF EXISTS diagnostico_resposta;");
                stmt.execute("DROP TABLE IF EXISTS diagnostico_versao;");
                System.out.println("Tabelas antigas de diagnostico recriadas para o novo esquema.");
            }

            // 3. Cabeçalho de Versões do Diagnóstico
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS diagnostico_versao (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    organizacao_id INTEGER,
                    produtor_id INTEGER,
                    numero_versao INTEGER DEFAULT 1,
                    data_aplicacao TEXT NOT NULL,
                    consultor_responsavel TEXT,
                    status TEXT DEFAULT 'EM_ANDAMENTO',
                    sync_status TEXT DEFAULT 'PENDENTE',
                    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id) ON DELETE CASCADE,
                    FOREIGN KEY (produtor_id) REFERENCES produtor(id) ON DELETE CASCADE
                );
            """);

            // 4. Respostas das 10 Dimensões
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS diagnostico_resposta (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    diagnostico_versao_id INTEGER NOT NULL,
                    dimensao TEXT NOT NULL,
                    subdimensao TEXT NOT NULL,
                    pontuacao INTEGER NOT NULL,
                    observacoes_evidencias TEXT,
                    plano_acao_recomendado TEXT,
                    FOREIGN KEY (diagnostico_versao_id) REFERENCES diagnostico_versao(id) ON DELETE CASCADE
                );
            """);

            // Migrações adicionais de campos do Produtor
            executarMigracao(stmt, "ALTER TABLE produtor ADD COLUMN destino_producao TEXT NOT NULL DEFAULT 'COOPERATIVA';");
            executarMigracao(stmt, "ALTER TABLE produtor ADD COLUMN consultor_bloqueio TEXT;");
            executarMigracao(stmt, "ALTER TABLE produtor ADD COLUMN data_nascimento TEXT;");
            executarMigracao(stmt, "ALTER TABLE produtor ADD COLUMN genero TEXT;");

            System.out.println("Banco SQLite sincronizado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar SQLite: " + e.getMessage());
        }
    }

    private static void executarMigracao(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException ignored) {}
    }
}