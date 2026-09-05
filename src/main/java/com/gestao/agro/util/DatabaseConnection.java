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
            -- 1. Organização (Associação / Cooperativa)
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
            
            -- 2. Produtores Rurais (com destino da produção e bloqueio exclusivo)
                CREATE TABLE IF NOT EXISTS produtor (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    organizacao_id INTEGER NOT NULL,
                    nome TEXT NOT NULL,
                    cpf TEXT,
                    comunidade TEXT,
                    possui_caf INTEGER,
                    principais_culturas TEXT,
                    area_propriedade REAL,
                    destino_producao TEXT NOT NULL DEFAULT 'COOPERATIVA', -- 'COOPERATIVA', 'VENDA_DIRETA', 'CONSUMO_PROPRIO'
                    consultor_bloqueio TEXT,
                    sync_status TEXT DEFAULT 'PENDENTE',
                    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id) ON DELETE CASCADE
                );

            -- 3. Histórico / Versões de Diagnósticos aplicados
            CREATE TABLE IF NOT EXISTS diagnostico_versao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER NOT NULL,
                tipo_entidade TEXT NOT NULL, -- 'ASSOCIACAO' ou 'PRODUTOR'
                entidade_id INTEGER NOT NULL,
                versao_numero INTEGER DEFAULT 1,
                data_aplicacao TEXT NOT NULL,
                consultor_responsavel TEXT,
                resumo_maturidade REAL,
                status TEXT DEFAULT 'EM_ANDAMENTO',
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id) ON DELETE CASCADE
            );

            -- 4. Respostas das 10 Dimensões do Diagnóstico
            CREATE TABLE IF NOT EXISTS diagnostico_resposta (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diagnostico_versao_id INTEGER NOT NULL,
                dimensao TEXT NOT NULL,
                pergunta TEXT NOT NULL,
                resposta_opcao TEXT NOT NULL,
                pontuacao INTEGER NOT NULL,
                FOREIGN KEY (diagnostico_versao_id) REFERENCES diagnostico_versao(id) ON DELETE CASCADE
            );

            -- 5. Fatores PESTEL
            CREATE TABLE IF NOT EXISTS pestel_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diagnostico_versao_id INTEGER NOT NULL,
                dimensao TEXT NOT NULL,
                descricao TEXT NOT NULL,
                tipo TEXT NOT NULL, -- 'OPORTUNIDADE' ou 'AMEACA'
                grau_impacto INTEGER NOT NULL, -- 1 a 5
                FOREIGN KEY (diagnostico_versao_id) REFERENCES diagnostico_versao(id) ON DELETE CASCADE
            );

            -- 6. Itens da Matriz FOFA / SWOT e Cruzamentos
            CREATE TABLE IF NOT EXISTS fofa_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diagnostico_versao_id INTEGER NOT NULL,
                tipo TEXT NOT NULL, -- 'FORCA', 'FRAQUEZA', 'OPORTUNIDADE', 'AMEACA'
                descricao TEXT NOT NULL,
                selecionado_trabalho INTEGER DEFAULT 1,
                FOREIGN KEY (diagnostico_versao_id) REFERENCES diagnostico_versao(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS fofa_cruzamentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diagnostico_versao_id INTEGER NOT NULL,
                tipo_cruzamento TEXT NOT NULL, -- 'FO', 'FA', 'DO', 'DA'
                estrategia_sugerida TEXT NOT NULL,
                origem_ia INTEGER DEFAULT 1,
                FOREIGN KEY (diagnostico_versao_id) REFERENCES diagnostico_versao(id) ON DELETE CASCADE
            );

            -- 7. Itens da Matriz GUT e Critérios Adicionais
            CREATE TABLE IF NOT EXISTS gut_itens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diagnostico_versao_id INTEGER NOT NULL,
                origem_fofa_id INTEGER,
                problema TEXT NOT NULL,
                gravidade INTEGER NOT NULL,
                urgencia INTEGER NOT NULL,
                tendencia INTEGER NOT NULL,
                score_gut INTEGER NOT NULL,
                impacto_esperado INTEGER DEFAULT 3,
                esforco_custo INTEGER DEFAULT 3,
                viabilidade INTEGER DEFAULT 3,
                prazo_dias INTEGER DEFAULT 30,
                FOREIGN KEY (diagnostico_versao_id) REFERENCES diagnostico_versao(id) ON DELETE CASCADE
            );

            -- 8. Plano de Ação 5W2H
            CREATE TABLE IF NOT EXISTS plano_5w2h (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diagnostico_versao_id INTEGER NOT NULL,
                gut_item_id INTEGER,
                o_que TEXT NOT NULL,
                por_que TEXT,
                onde TEXT,
                quando TEXT,
                quem TEXT,
                como TEXT,
                quanto REAL,
                status_execucao TEXT DEFAULT 'NAO_INICIADO',
                FOREIGN KEY (diagnostico_versao_id) REFERENCES diagnostico_versao(id) ON DELETE CASCADE,
                FOREIGN KEY (gut_item_id) REFERENCES gut_itens(id) ON DELETE SET NULL
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : sqlTabelas.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
            System.out.println("Schema do banco SQLite atualizado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar/atualizar SQLite: " + e.getMessage());
        }
    }
}