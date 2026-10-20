package com.gestao.agro.util;

import com.gestao.agro.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExportacaoService {

    private static final NumberFormat formatadorMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // ==========================================
    //            EXPORTAÇÃO PARA EXCEL
    // ==========================================
    public static void exportarPlanoAcaoExcel(File arquivo, List<PlanoAcao5W2H> planos) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Plano de Ação 5W2H");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            String[] colunas = {"ID", "O Quê (What)", "Por Quê (Why)", "Onde (Where)", "Quando (When)", "Quem (Who)", "Como (How)", "Quanto (How Much)", "Status"};
            
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            if (planos != null) {
                for (PlanoAcao5W2H p : planos) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(p.getId() != null ? p.getId() : 0);
                    row.createCell(1).setCellValue(p.getWhatAcao() != null ? p.getWhatAcao() : "");
                    row.createCell(2).setCellValue(p.getWhyMotivo() != null ? p.getWhyMotivo() : "");
                    row.createCell(3).setCellValue(p.getWhereLocal() != null ? p.getWhereLocal() : "");
                    row.createCell(4).setCellValue(p.getWhenPrazo() != null ? p.getWhenPrazo() : "");
                    row.createCell(5).setCellValue(p.getWhoResponsavel() != null ? p.getWhoResponsavel() : "");
                    row.createCell(6).setCellValue(p.getHowMetodo() != null ? p.getHowMetodo() : "");
                    row.createCell(7).setCellValue(p.getHowMuchCusto() != null ? p.getHowMuchCusto() : 0.0);
                    row.createCell(8).setCellValue(p.getStatus() != null ? p.getStatus() : "");
                }
            }

            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(arquivo)) {
                workbook.write(fileOut);
            }
        }
    }

    // ==========================================
    //     EXPORTAÇÃO PARA WORD (DOSSIÊ COMPLETO)
    // ==========================================
    public static void exportarDossieWord(File arquivo, String nomeEntidade, boolean incImo, 
                                          DiagnosticoVersao versao,
                                          List<FatorPestel> pestel, List<FatorSwot> swotFatores, 
                                          List<CruzamentoSwot> tows, List<ItemGut> gut, 
                                          List<PlanoAcao5W2H> planos, File imagemGrafico) throws Exception {
        
        try (XWPFDocument document = new XWPFDocument()) {
            
            // --- TÍTULO PRINCIPAL ---
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("DOSSIÊ DE DIAGNÓSTICO E PLANEAMENTO ESTRATÉGICO");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            XWPFParagraph subTitle = document.createParagraph();
            subTitle.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun subTitleRun = subTitle.createRun();
            subTitleRun.setText("Organização / Produtor: " + nomeEntidade);
            subTitleRun.setFontSize(12);
            subTitleRun.addBreak();
            subTitleRun.addBreak();

            // --- SECÇÃO IMO-AF ---
            if (incImo) {
                criarCabecalhoSecao(document, "Módulo 2: Diagnóstico IMO-AF (Perfil de Maturidade)");
                criarParagrafoSimples(document, "A maturidade organizacional foi avaliada de acordo com as 15 dimensões do Índice de Maturidade Organizacional da Agricultura Familiar (IMO-AF). Abaixo encontra-se o perfil gráfico e a respetiva tabela de detalhe por dimensão:");
                
                if (imagemGrafico != null && imagemGrafico.exists()) {
                    XWPFParagraph pImage = document.createParagraph();
                    pImage.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun rImage = pImage.createRun();
                    
                    try (FileInputStream is = new FileInputStream(imagemGrafico)) {
                        rImage.addPicture(is, Document.PICTURE_TYPE_PNG, imagemGrafico.getName(), Units.toEMU(450), Units.toEMU(220)); 
                    } catch (Exception e) {
                        System.err.println("Erro ao anexar imagem do gráfico no Word: " + e.getMessage());
                    }
                    rImage.addBreak();
                }

                // ADICIONA A TABELA DE LEGENDA / DETALHE DAS 15 DIMENSÕES
                if (versao != null && versao.getMediasPorDimensao() != null && !versao.getMediasPorDimensao().isEmpty()) {
                    criarSubTitulo(document, "Detalhe das Dimensões (Média e Nível):");
                    
                    XWPFTable tabelaImo = document.createTable();
                    tabelaImo.setCellMargins(100, 150, 100, 150);
                    
                    XWPFTableRow linhaHeader = tabelaImo.getRow(0);
                    linhaHeader.getCell(0).setText("Dimensão Avaliada");
                    linhaHeader.createCell().setText("Média");
                    linhaHeader.createCell().setText("Nível de Maturidade");
                    
                    int contador = 1;
                    for (DimensaoDiagnostico dim : DimensaoDiagnostico.values()) {
                        Double nota = versao.getMediasPorDimensao().get(dim.getTitulo());
                        double valor = (nota != null) ? nota : 0.0;
                        String nivel = DiagnosticoVersao.classificarNivelDimensao(valor);
                        
                        String nomeLimpo = dim.getTitulo();
                        if (nomeLimpo.contains(".")) {
                            nomeLimpo = nomeLimpo.substring(nomeLimpo.indexOf('.') + 1).trim();
                        }

                        XWPFTableRow linha = tabelaImo.createRow();
                        linha.getCell(0).setText("D" + contador + " - " + nomeLimpo);
                        linha.getCell(1).setText(String.format(Locale.US, "%.2f", valor));
                        linha.getCell(2).setText(nivel);
                        contador++;
                    }
                }
            }

            // --- SECÇÃO PESTEL ---
            if (pestel != null) {
                criarCabecalhoSecao(document, "Módulo 3: Matriz PESTEL (Ambiente Externo)");
                if (pestel.isEmpty()) {
                    criarParagrafoSimples(document, "Nenhum fator PESTEL registado.");
                } else {
                    for (FatorPestel p : pestel) {
                        String texto = String.format("[%s - %s] %s (Impacto: %d, Probabilidade: %d). Ação sugerida: %s", 
                            p.getDimensao(), p.getTipo(), p.getDescricao(), p.getImpacto(), p.getProbabilidade(), 
                            (p.getAcaoSugerida() != null && !p.getAcaoSugerida().isBlank()) ? p.getAcaoSugerida() : "N/A");
                        criarParagrafoLista(document, texto);
                    }
                }
            }

            // --- SECÇÃO FOFA E TOWS ---
            if (swotFatores != null || tows != null) {
                criarCabecalhoSecao(document, "Módulo 4: Matriz FOFA / TOWS");
                
                if (swotFatores != null && !swotFatores.isEmpty()) {
                    criarSubTitulo(document, "Fatores Levantados (FOFA):");
                    for (FatorSwot s : swotFatores) {
                        criarParagrafoLista(document, "[" + s.getTipo() + " - Int: " + s.getIntensidade() + "] " + s.getDescricao());
                    }
                }
                
                if (tows != null && !tows.isEmpty()) {
                    criarSubTitulo(document, "Estratégias Cruzadas (TOWS):");
                    for (CruzamentoSwot c : tows) {
                        criarParagrafoLista(document, "[" + c.getTipoEstrategia() + "] " + c.getEstrategiaProposta());
                    }
                }
            }

            // --- SECÇÃO GUT ---
            if (gut != null) {
                criarCabecalhoSecao(document, "Módulo 5: Matriz GUT (Priorização)");
                if (gut.isEmpty()) {
                    criarParagrafoSimples(document, "Nenhum item priorizado na Matriz GUT.");
                } else {
                    for (ItemGut g : gut) {
                        String texto = String.format("[%s - Score: %d] %s (G:%d U:%d T:%d)", 
                            g.getNivelPrioridade(), g.getScoreGut(), g.getProblemaAcao(), g.getGravidade(), g.getUrgencia(), g.getTendencia());
                        criarParagrafoLista(document, texto);
                    }
                }
            }

            // --- SECÇÃO 5W2H ---
            if (planos != null) {
                criarCabecalhoSecao(document, "Módulo 6: Plano de Ação Estratégico (5W2H)");
                if (planos.isEmpty()) {
                    criarParagrafoSimples(document, "Nenhum plano de ação registado.");
                } else {
                    double custoTotal = 0.0;
                    for (PlanoAcao5W2H p : planos) {
                        XWPFParagraph pItem = document.createParagraph();
                        XWPFRun runTitle = pItem.createRun();
                        runTitle.setBold(true);
                        runTitle.setText("AÇÃO: " + (p.getWhatAcao() != null ? p.getWhatAcao() : "Não definida"));
                        runTitle.addBreak();
                        
                        XWPFRun runDetails = pItem.createRun();
                        runDetails.setText("   • Porquê (Why): " + (p.getWhyMotivo() != null ? p.getWhyMotivo() : "-"));
                        runDetails.addBreak();
                        runDetails.setText("   • Onde (Where): " + (p.getWhereLocal() != null ? p.getWhereLocal() : "-"));
                        runDetails.addBreak();
                        runDetails.setText("   • Quando (When): " + (p.getWhenPrazo() != null ? p.getWhenPrazo() : "-"));
                        runDetails.addBreak();
                        runDetails.setText("   • Quem (Who): " + (p.getWhoResponsavel() != null ? p.getWhoResponsavel() : "-"));
                        runDetails.addBreak();
                        runDetails.setText("   • Como (How): " + (p.getHowMetodo() != null ? p.getHowMetodo() : "-"));
                        runDetails.addBreak();
                        
                        String custoFormatado = (p.getHowMuchCusto() != null) ? formatadorMoeda.format(p.getHowMuchCusto()) : formatadorMoeda.format(0);
                        if (p.getHowMuchCusto() != null && !p.getStatus().equalsIgnoreCase("CANCELADO")) {
                            custoTotal += p.getHowMuchCusto();
                        }
                        runDetails.setText("   • Quanto Custa (How Much): " + custoFormatado);
                        runDetails.addBreak();
                        runDetails.setText("   • Status: " + p.getStatus());
                        runDetails.addBreak();
                    }
                    
                    XWPFParagraph pTotal = document.createParagraph();
                    pTotal.setAlignment(ParagraphAlignment.RIGHT);
                    XWPFRun runTotal = pTotal.createRun();
                    runTotal.setBold(true);
                    runTotal.setText("INVESTIMENTO TOTAL ESTIMADO: " + formatadorMoeda.format(custoTotal));
                }
            }

            try (FileOutputStream out = new FileOutputStream(arquivo)) {
                document.write(out);
            }
        }
    }

    private static void criarCabecalhoSecao(XWPFDocument doc, String titulo) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(400);
        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setFontSize(14);
        run.setColor("1e3a2f");
        run.setText(titulo);
    }

    private static void criarSubTitulo(XWPFDocument doc, String subtitulo) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setItalic(true);
        run.setText(subtitulo);
    }

    private static void criarParagrafoSimples(XWPFDocument doc, String texto) {
        XWPFParagraph p = doc.createParagraph();
        p.createRun().setText(texto);
    }

    private static void criarParagrafoLista(XWPFDocument doc, String texto) {
        XWPFParagraph p = doc.createParagraph();
        p.setIndentationLeft(360);
        XWPFRun run = p.createRun();
        run.setText("• " + texto);
    }
}