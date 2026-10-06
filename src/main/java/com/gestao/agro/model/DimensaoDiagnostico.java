package com.gestao.agro.model;

import java.util.Arrays;
import java.util.List;

public enum DimensaoDiagnostico {

    ESTRATEGIA_GOVERNANCA(
        "1. Estratégia, Governança e Gestão Coletiva",
        Arrays.asList(
            "A organização possui objetivos e prioridades definidos para os próximos anos?",
            "Os objetivos da organização são conhecidos pelos associados/cooperados?",
            "A organização realiza planejamento periódico de suas atividades?",
            "As decisões importantes são discutidas de forma participativa?",
            "A organização acompanha a execução de seus planos e objetivos?"
        )
    ),
    LIDERANCA_PARTICIPACAO(
        "2. Liderança e Participação",
        Arrays.asList(
            "A diretoria exerce suas funções de forma organizada e ativa?",
            "Os associados/cooperados participam regularmente das assembleias e reuniões?",
            "Os associados/cooperados participam das decisões relacionadas às atividades da organização?",
            "A diretoria mantém comunicação regular com os associados/cooperados?",
            "A organização estimula a formação e renovação de lideranças?"
        )
    ),
    PROCESSOS_GESTAO(
        "3. Processos Organizacionais e Gestão Administrativa",
        Arrays.asList(
            "Os principais processos administrativos da organização estão definidos?",
            "Existem responsáveis definidos para as principais atividades?",
            "Os documentos administrativos, fiscais e contábeis são organizados e mantidos atualizados?",
            "A organização mantém registros das suas atividades produtivas e comerciais?",
            "Os processos internos são avaliados periodicamente para identificar melhorias?"
        )
    ),
    PESSOAS_COMPETENCIAS(
        "4. Gestão de Pessoas e Competências",
        Arrays.asList(
            "Os dirigentes recebem capacitação para exercer suas funções?",
            "Os associados/cooperados participam de capacitações relacionadas às atividades da organização?",
            "A organização identifica as principais necessidades de capacitação de seus membros?",
            "Existe compartilhamento de conhecimentos e experiências entre os associados/cooperados?",
            "A organização estimula a participação de mulheres e jovens na gestão e nas atividades?"
        )
    ),
    GESTAO_FINANCEIRA(
        "5. Gestão Financeira e Econômica",
        Arrays.asList(
            "A organização possui controle sistemático de receitas e despesas?",
            "A organização acompanha regularmente seu fluxo de caixa?",
            "Os custos das atividades são conhecidos e monitorados?",
            "A organização realiza prestação de contas aos associados/cooperados de forma transparente?",
            "As informações financeiras são utilizadas para apoiar decisões?"
        )
    ),
    MERCADO_COMERCIALIZACAO(
        "6. Mercado e Comercialização",
        Arrays.asList(
            "A organização conhece os principais mercados para seus produtos?",
            "A organização possui estratégias para comercialização da produção?",
            "A organização acompanha preços e condições de mercado?",
            "A organização possui mais de um canal de comercialização?",
            "A organização mantém relacionamento regular com compradores e parceiros comerciais?"
        )
    ),
    TECNOLOGIA_DIGITAL(
        "7. Tecnologia e Transformação Digital",
        Arrays.asList(
            "A organização utiliza ferramentas digitais para sua gestão?",
            "Utiliza planilhas ou sistemas para controlar produção, vendas ou finanças?",
            "Utiliza ferramentas digitais para comunicação com associados/cooperados?",
            "Utiliza redes sociais ou outros canais digitais para divulgação e comercialização?",
            "A organização busca incorporar novas tecnologias para melhorar sua gestão?"
        )
    ),
    INOVACAO_VALOR(
        "8. Inovação e Agregação de Valor",
        Arrays.asList(
            "A organização estimula os associados/cooperados a apresentar novas ideias?",
            "A organização desenvolve ou busca novos produtos ou formas de comercialização?",
            "Busca melhorar continuamente a qualidade dos produtos?",
            "Desenvolve estratégias para agregar valor à produção?",
            "Avalia os resultados das inovações implementadas?"
        )
    ),
    GESTAO_INFORMACAO(
        "9. Gestão da Informação e Dados",
        Arrays.asList(
            "A organização mantém informações atualizadas sobre seus associados/cooperados?",
            "Mantém informações sobre produção e capacidade produtiva?",
            "Registra informações sobre vendas e comercialização?",
            "Utiliza informações de mercado para planejar suas atividades?",
            "Utiliza dados e indicadores para apoiar decisões?"
        )
    ),
    RISCOS_REGULARIDADE(
        "10. Gestão de Riscos e Regularidade",
        Arrays.asList(
            "A organização mantém sua documentação institucional atualizada?",
            "A organização acompanha suas obrigações legais, fiscais e administrativas?",
            "Identifica os principais riscos relacionados à produção e comercialização?",
            "Possui procedimentos para enfrentar perdas de produção ou problemas de mercado?",
            "Quando ocorre um problema, são adotadas medidas corretivas?"
        )
    ),
    SUSTENTABILIDADE_TERRITORIO(
        "11. Sustentabilidade e Desenvolvimento Territorial",
        Arrays.asList(
            "A organização incentiva práticas produtivas sustentáveis?",
            "Busca reduzir desperdícios e perdas na produção?",
            "Incentiva a conservação do solo, da água e dos recursos naturais?",
            "Valoriza produtos, conhecimentos e características do território?",
            "A organização contribui para a geração de renda e permanência das famílias no campo?"
        )
    ),
    APRENDIZAGEM_ASSISTENCIA(
        "12. Aprendizagem Organizacional e Assistência Técnica",
        Arrays.asList(
            "A organização avalia suas experiências para identificar o que pode ser melhorado?",
            "Os problemas enfrentados são discutidos coletivamente?",
            "Os conhecimentos adquiridos são compartilhados entre os associados/cooperados?",
            "A organização busca assistência técnica quando identifica necessidades?",
            "Os conhecimentos adquiridos em cursos, oficinas e capacitações são aplicados na organização?"
        )
    ),
    RESULTADOS_DESEMPENHO(
        "13. Resultados e Desempenho",
        Arrays.asList(
            "A organização acompanha o volume de produção comercializado?",
            "Acompanha sua situação financeira ao longo do tempo?",
            "Acompanha o número de associados/cooperados ativos?",
            "Avalia os resultados das vendas e contratos realizados?",
            "Avalia os benefícios gerados para seus associados/cooperados?"
        )
    ),
    CULTURA_COOPERACAO(
        "14. Cultura Organizacional, Cooperação e Associativismo",
        Arrays.asList(
            "Existe confiança entre os associados/cooperados e a direção?",
            "Os membros demonstram disposição para trabalhar coletivamente?",
            "Existe cooperação entre os associados/cooperados para resolver problemas comuns?",
            "Existe transparência na relação entre direção e associados/cooperados?",
            "Os associados/cooperados demonstram sentimento de pertencimento à organização?"
        )
    ),
    POLITICAS_REDES(
        "15. Políticas Públicas, Parcerias e Redes",
        Arrays.asList(
            "A organização conhece as principais políticas públicas destinadas à agricultura familiar?",
            "A organização mantém atualizada a documentação necessária para acessar essas políticas?",
            "A organização consegue acessar programas de comercialização institucional, quando aplicáveis?",
            "A organização possui parcerias com instituições públicas, privadas, universidades, órgãos de pesquisa ou assistência técnica?",
            "A organização participa de redes, fóruns, feiras ou espaços de articulação da agricultura familiar?"
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