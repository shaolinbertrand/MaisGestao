package com.gestao.agro.model;

import java.util.Arrays;
import java.util.List;

public enum DimensaoDiagnostico {

    GOVERNANCA(
        "1. Governança e Gestão Estratégica",
        Arrays.asList(
            "Estrutura organizacional, papéis claros e processos de tomada de decisão",
            "Planejamento estratégico formalizado e monitoramento de metas",
            "Transparência, prestação de contas e comunicação interna"
        )
    ),
    GESTAO_FINANCEIRA(
        "2. Gestão Financeira e Contábil",
        Arrays.asList(
            "Controle de fluxo de caixa, custos de produção e demonstrativos",
            "Separação das finanças pessoais e do negócio / cooperativa",
            "Planejamento orçamentário e gestão de capital de giro"
        )
    ),
    PRODUCAO_OPERACAO(
        "3. Produção e Operações",
        Arrays.asList(
            "Padronização de processos produtivos e controle de perdas",
            "Adoção de boas práticas agropecuárias (BPA) e rastreabilidade",
            "Capacidade instalada e planejamento da colheita/safra"
        )
    ),
    MERCADO_COMERCIALIZACAO(
        "4. Mercado e Comercialização",
        Arrays.asList(
            "Diversificação de canais de venda (atacado, varejo, editais públicos)",
            "Acesso e participação em programas governamentais (PAA / PNAE)",
            "Estratégia de precificação e conhecimento do perfil do cliente"
        )
    ),
    SUSTENTABILIDADE_MEIO_AMBIENTE(
        "5. Sustentabilidade e Meio Ambiente",
        Arrays.asList(
            "Conformidade ambiental e situação do CAR (Cadastro Ambiental Rural)",
            "Manejo e conservação do solo, água e resíduos sólidos",
            "Uso de insumos biológicos ou práticas de baixo carbono"
        )
    ),
    PESSOAS_RECURSOS_HUMANOS(
        "6. Pessoas e Relações de Trabalho",
        Arrays.asList(
            "Capacitação contínua de colaboradores e cooperados",
            "Conformidade trabalhista e segurança e saúde no trabalho rural (NR-31)",
            "Sucessão familiar e atratividade para jovens na atividade"
        )
    ),
    INOVACAO_TECNOLOGIA(
        "7. Inovação e Tecnologia Agropecuária",
        Arrays.asList(
            "Utilização de ferramentas digitais e conectividade no campo",
            "Adoção de tecnologias para aumento de produtividade e redução de custos",
            "Acesso à assistência técnica e extensão rural (ATER) regular"
        )
    ),
    QUALIDADE_CONFORMIDADE(
        "8. Qualidade e Certificações",
        Arrays.asList(
            "Controle de qualidade e conformidade com padrões sanitários",
            "Existência de selos, certificações ou conformidade CAF/DAP",
            "Processos de melhoria contínua e tratamento de não conformidades"
        )
    ),
    LOGISTICA_INFRAESTRUTURA(
        "9. Infraestrutura e Logística",
        Arrays.asList(
            "Adequação de galpões, armazéns, estufas e veículos de transporte",
            "Conservação de estradas vicinais e escoamento da produção",
            "Acesso estável a energia elétrica, água e telecomunicações"
        )
    ),
    PARCERIAS_INTEGRACAO(
        "10. Parcerias e Integração Territorial",
        Arrays.asList(
            "Relação com órgãos de fomento, bancos de crédito rural e pesquisa (ex: Embrapa)",
            "Articulação interinstitucional e redes de cooperação local",
            "Acesso a linhas de crédito produtivo (Pronaf / Plano Safra)"
        )
    );

    private final String titulo;
    private final List<String> perguntas;

    DimensaoDiagnostico(String titulo, List<String> perguntas) {
        this.titulo = titulo;
        this.perguntas = perguntas;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<String> getPerguntas() {
        return perguntas;
    }
}