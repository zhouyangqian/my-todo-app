package com.example.finance.export;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PdfExporter {

    private static final String FONT_PATH = "STSong-Light";
    private static final String FONT_ENCODING = "UniGB-UTF16-H";

    private PdfFont font;

    public PdfExporter() {
        try {
            font = PdfFontFactory.createFont(FONT_PATH, FONT_ENCODING);
        } catch (Exception e) {
            log.warn("无法加载中文字体，使用默认字体: {}", e.getMessage());
            font = null;
        }
    }

    public void export(OutputStream out, String title, LinkedHashMap<String, String> headers, List<Map<String, Object>> data) {
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            Paragraph titlePara = new Paragraph(title)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            if (font != null) {
                titlePara.setFont(font);
            }
            document.add(titlePara);

            // Table
            float[] columnWidths = new float[headers.size()];
            for (int i = 0; i < headers.size(); i++) {
                columnWidths[i] = 100f / headers.size();
            }
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            // Header row
            for (String headerLabel : headers.values()) {
                Cell cell = new Cell().add(createParagraph(headerLabel, true));
                table.addHeaderCell(cell);
            }

            // Data rows
            for (Map<String, Object> row : data) {
                for (String key : headers.keySet()) {
                    Object value = row.get(key);
                    String text = value != null ? String.valueOf(value) : "";
                    Cell cell = new Cell().add(createParagraph(text, false));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            log.info("PDF导出完成: {}, 行数: {}", title, data.size());
        } catch (Exception e) {
            log.error("PDF导出失败: {}", title, e);
            throw new RuntimeException("PDF导出失败: " + e.getMessage(), e);
        }
    }

    private Paragraph createParagraph(String text, boolean bold) {
        Text textElement = new Text(text);
        if (bold) textElement.setBold();
        if (font != null) textElement.setFont(font);
        textElement.setFontSize(10);
        return new Paragraph(textElement);
    }
}
