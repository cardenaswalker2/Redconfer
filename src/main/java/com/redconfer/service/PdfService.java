package com.redconfer.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.redconfer.model.Quote;
import com.redconfer.model.Invoice;
import com.redconfer.model.Settings;
import com.redconfer.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final SettingsRepository settingsRepository;

    public byte[] generateQuotePdf(Quote quote) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // A4 page with clean margins
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            Settings settings = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
            String siteName = settings != null ? settings.getSiteName() : "REDCONFER";
            String phone = settings != null ? settings.getPhone() : "+57 323 357 0996";
            String email = settings != null ? settings.getEmail() : "contacto@redconfer.com";
            String address = settings != null ? settings.getAddress() : "Cartagena, Colombia";

            PdfWriter.getInstance(document, out);
            document.open();

            // Color Palette
            java.awt.Color primaryRed = new java.awt.Color(198, 26, 34);     // #C61A22
            java.awt.Color darkSlate = new java.awt.Color(15, 23, 42);       // #0F172A
            java.awt.Color lightGrayBg = new java.awt.Color(248, 250, 252);  // #F8FAFC
            java.awt.Color borderGray = new java.awt.Color(226, 232, 240);   // #E2E8F0
            java.awt.Color textDark = new java.awt.Color(51, 65, 85);        // #334155
            java.awt.Color textSubdued = new java.awt.Color(100, 116, 139);  // #64748B

            // Font definitions
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, primaryRed);
            Font docTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, darkSlate);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, darkSlate);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, textDark);
            Font regularSubduedFont = FontFactory.getFont(FontFactory.HELVETICA, 8, textSubdued);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textDark);
            Font boldRedFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, primaryRed);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE);

            // Branded Top Bar Accent
            PdfPTable topBar = new PdfPTable(1);
            topBar.setWidthPercentage(100);
            PdfPCell barCell = new PdfPCell();
            barCell.setFixedHeight(4f);
            barCell.setBackgroundColor(primaryRed);
            barCell.setBorder(Rectangle.NO_BORDER);
            topBar.addCell(barCell);
            document.add(topBar);
            document.add(new Paragraph(" ")); // Spacer

            // Header Table (Logo vs Quote Info)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{55, 45});

            // Logo and tagline
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            Paragraph brandNameParagraph = new Paragraph(siteName, brandFont);
            brandNameParagraph.setSpacingAfter(2f);
            Paragraph brandTagline = new Paragraph("INTEGRATED SECURITY & TECHNOLOGY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, textSubdued));
            logoCell.addElement(brandNameParagraph);
            logoCell.addElement(brandTagline);
            headerTable.addCell(logoCell);

            // Document Metadata
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            Paragraph docTitle = new Paragraph("COTIZACIÓN COMERCIAL", docTitleFont);
            docTitle.setAlignment(Element.ALIGN_RIGHT);
            docTitle.setSpacingAfter(4f);
            
            Paragraph numParagraph = new Paragraph("N°: " + quote.getQuoteNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, primaryRed));
            numParagraph.setAlignment(Element.ALIGN_RIGHT);
            numParagraph.setSpacingAfter(2f);
            
            Paragraph dateParagraph = new Paragraph("Fecha: " + quote.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), regularFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            
            Paragraph statusParagraph = new Paragraph("Estado: " + (quote.getStatus() != null ? quote.getStatus().name() : "PENDIENTE"), regularSubduedFont);
            statusParagraph.setAlignment(Element.ALIGN_RIGHT);
            
            infoCell.addElement(docTitle);
            infoCell.addElement(numParagraph);
            infoCell.addElement(dateParagraph);
            infoCell.addElement(statusParagraph);
            headerTable.addCell(infoCell);

            document.add(headerTable);
            document.add(new Paragraph(" ")); // Spacer

            // Company & Client Details (Cards styled with light background)
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{48, 48});
            detailsTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Provider Info Card
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBackgroundColor(lightGrayBg);
            companyCell.setBorderColor(borderGray);
            companyCell.setBorderWidth(1f);
            companyCell.setPadding(12f);
            
            Paragraph provHeader = new Paragraph("PROVEEDOR", sectionHeaderFont);
            provHeader.setSpacingAfter(6f);
            companyCell.addElement(provHeader);
            
            companyCell.addElement(new Paragraph(siteName + " Comprehensive Services", boldFont));
            companyCell.addElement(new Paragraph("Tel: " + phone, regularFont));
            companyCell.addElement(new Paragraph("Email: " + email, regularFont));
            companyCell.addElement(new Paragraph("Dirección: " + address, regularFont));
            detailsTable.addCell(companyCell);

            // Empty Spacer Cell between cards
            PdfPCell spaceCell = new PdfPCell();
            spaceCell.setBorder(Rectangle.NO_BORDER);

            // Client Info Card
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBackgroundColor(lightGrayBg);
            clientCell.setBorderColor(borderGray);
            clientCell.setBorderWidth(1f);
            clientCell.setPadding(12f);
            
            Paragraph clientHeader = new Paragraph("CLIENTE", sectionHeaderFont);
            clientHeader.setSpacingAfter(6f);
            clientCell.addElement(clientHeader);
            
            clientCell.addElement(new Paragraph(quote.getClientName(), boldFont));
            if (quote.getClientCompany() != null && !quote.getClientCompany().isEmpty()) {
                clientCell.addElement(new Paragraph("Empresa: " + quote.getClientCompany(), regularFont));
            } else {
                clientCell.addElement(new Paragraph("Empresa: Personal/Particular", regularFont));
            }
            clientCell.addElement(new Paragraph("Tel: " + quote.getClientPhone(), regularFont));
            clientCell.addElement(new Paragraph("Dirección: " + (quote.getClientAddress() != null && !quote.getClientAddress().isEmpty() ? quote.getClientAddress() : "No especificada"), regularFont));
            detailsTable.addCell(clientCell);

            document.add(detailsTable);
            document.add(new Paragraph(" ")); // Spacer

            // Items Table
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{55, 10, 15, 20});
            itemsTable.setKeepTogether(true);

            // Table Headers
            String[] headers = {"Descripción del Servicio / Equipo", "Cant.", "Precio Unit.", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, tableHeaderFont));
                cell.setBackgroundColor(darkSlate);
                cell.setPadding(8f);
                cell.setBorderColor(borderGray);
                cell.setHorizontalAlignment(header.equals("Descripción del Servicio / Equipo") ? Element.ALIGN_LEFT : (header.equals("Cant.") ? Element.ALIGN_CENTER : Element.ALIGN_RIGHT));
                itemsTable.addCell(cell);
            }

            // Populate Items with Zebra Striping and Clean Borders
            boolean isEven = false;
            if (quote.getItems() != null && !quote.getItems().isEmpty()) {
                for (Quote.QuoteItem item : quote.getItems()) {
                    java.awt.Color rowBg = isEven ? lightGrayBg : java.awt.Color.WHITE;
                    
                    PdfPCell descCell = new PdfPCell(new Paragraph(item.getDescription(), regularFont));
                    descCell.setBackgroundColor(rowBg);
                    descCell.setBorderColor(borderGray);
                    descCell.setPadding(8f);
                    itemsTable.addCell(descCell);

                    PdfPCell qtyCell = new PdfPCell(new Paragraph(String.valueOf(item.getQuantity()), regularFont));
                    qtyCell.setBackgroundColor(rowBg);
                    qtyCell.setBorderColor(borderGray);
                    qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    qtyCell.setPadding(8f);
                    itemsTable.addCell(qtyCell);

                    PdfPCell priceCell = new PdfPCell(new Paragraph(String.format("$%,.2f", item.getUnitPrice()), regularFont));
                    priceCell.setBackgroundColor(rowBg);
                    priceCell.setBorderColor(borderGray);
                    priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    priceCell.setPadding(8f);
                    itemsTable.addCell(priceCell);

                    PdfPCell totalCell = new PdfPCell(new Paragraph(String.format("$%,.2f", item.getTotal()), boldFont));
                    totalCell.setBackgroundColor(rowBg);
                    totalCell.setBorderColor(borderGray);
                    totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalCell.setPadding(8f);
                    itemsTable.addCell(totalCell);
                    
                    isEven = !isEven;
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Paragraph("No se han agregado ítems detallados. Revisión técnica pendiente.", regularFont));
                emptyCell.setColspan(4);
                emptyCell.setPadding(12f);
                emptyCell.setBorderColor(borderGray);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(emptyCell);
            }

            document.add(itemsTable);
            document.add(new Paragraph(" ")); // Spacer

            // Totals and Observations
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{55, 45});
            totalsTable.setKeepTogether(true);

            // Left column: Observations Card
            PdfPCell obsCell = new PdfPCell();
            obsCell.setBorderColor(borderGray);
            obsCell.setBackgroundColor(lightGrayBg);
            obsCell.setPadding(12f);
            
            Paragraph obsTitle = new Paragraph("Términos & Observaciones", sectionHeaderFont);
            obsTitle.setSpacingAfter(6f);
            obsCell.addElement(obsTitle);
            
            String obs = quote.getAdminObservations() != null ? quote.getAdminObservations() : "Válida por 15 días. Incluye soporte y garantía del fabricante.";
            Paragraph obsContent = new Paragraph(obs, regularFont);
            obsContent.setLeading(12f);
            obsCell.addElement(obsContent);
            
            totalsTable.addCell(obsCell);

            // Right column: Totals Summary
            PdfPCell valCell = new PdfPCell();
            valCell.setBorder(Rectangle.NO_BORDER);
            valCell.setPaddingLeft(15f);
            
            PdfPTable rightTable = new PdfPTable(2);
            rightTable.setWidthPercentage(100);
            rightTable.setWidths(new float[]{50, 50});
            
            // Subtotal
            PdfPCell subLabelCell = new PdfPCell(new Paragraph("Subtotal:", regularFont));
            subLabelCell.setBorder(Rectangle.BOTTOM);
            subLabelCell.setBorderColor(borderGray);
            subLabelCell.setPadding(6f);
            rightTable.addCell(subLabelCell);
            
            PdfPCell subValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", quote.getSubtotal()), regularFont));
            subValCell.setBorder(Rectangle.BOTTOM);
            subValCell.setBorderColor(borderGray);
            subValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subValCell.setPadding(6f);
            rightTable.addCell(subValCell);
            
            // Tax
            PdfPCell taxLabelCell = new PdfPCell(new Paragraph("IVA (" + quote.getTaxRate() + "%):", regularFont));
            taxLabelCell.setBorder(Rectangle.BOTTOM);
            taxLabelCell.setBorderColor(borderGray);
            taxLabelCell.setPadding(6f);
            rightTable.addCell(taxLabelCell);
            
            PdfPCell taxValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", quote.getTaxAmount()), regularFont));
            taxValCell.setBorder(Rectangle.BOTTOM);
            taxValCell.setBorderColor(borderGray);
            taxValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            taxValCell.setPadding(6f);
            rightTable.addCell(taxValCell);
            
            // Discount (if any)
            if (quote.getDiscountAmount() > 0) {
                PdfPCell discLabelCell = new PdfPCell(new Paragraph("Descuento:", regularFont));
                discLabelCell.setBorder(Rectangle.BOTTOM);
                discLabelCell.setBorderColor(borderGray);
                discLabelCell.setPadding(6f);
                rightTable.addCell(discLabelCell);
                
                PdfPCell discValCell = new PdfPCell(new Paragraph(String.format("-$%,.2f", quote.getDiscountAmount()), regularFont));
                discValCell.setBorder(Rectangle.BOTTOM);
                discValCell.setBorderColor(borderGray);
                discValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                discValCell.setPadding(6f);
                rightTable.addCell(discValCell);
            }
            
            // Grand Total
            PdfPCell totalLabelCell = new PdfPCell(new Paragraph("Total General:", boldRedFont));
            totalLabelCell.setBackgroundColor(lightGrayBg);
            totalLabelCell.setBorder(Rectangle.BOX);
            totalLabelCell.setBorderColor(borderGray);
            totalLabelCell.setPadding(8f);
            rightTable.addCell(totalLabelCell);
            
            PdfPCell totalValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", quote.getTotal()), boldRedFont));
            totalValCell.setBackgroundColor(lightGrayBg);
            totalValCell.setBorder(Rectangle.BOX);
            totalValCell.setBorderColor(borderGray);
            totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValCell.setPadding(8f);
            rightTable.addCell(totalValCell);
            
            valCell.addElement(rightTable);
            totalsTable.addCell(valCell);

            document.add(totalsTable);
            
            // Bottom Branded Footer / Signature Placeholder
            document.add(new Paragraph(" "));
            Paragraph signatureText = new Paragraph("Gracias por confiar en nuestros servicios.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, textSubdued));
            signatureText.setAlignment(Element.ALIGN_CENTER);
            document.add(signatureText);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            Settings settings = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
            String siteName = settings != null ? settings.getSiteName() : "REDCONFER";
            String phone = settings != null ? settings.getPhone() : "+57 323 357 0996";
            String email = settings != null ? settings.getEmail() : "contacto@redconfer.com";
            String address = settings != null ? settings.getAddress() : "Cartagena, Colombia";

            PdfWriter.getInstance(document, out);
            document.open();

            // Color Palette
            java.awt.Color primaryRed = new java.awt.Color(198, 26, 34);     // #C61A22
            java.awt.Color darkSlate = new java.awt.Color(15, 23, 42);       // #0F172A
            java.awt.Color lightGrayBg = new java.awt.Color(248, 250, 252);  // #F8FAFC
            java.awt.Color borderGray = new java.awt.Color(226, 232, 240);   // #E2E8F0
            java.awt.Color textDark = new java.awt.Color(51, 65, 85);        // #334155
            java.awt.Color textSubdued = new java.awt.Color(100, 116, 139);  // #64748B

            // Font definitions
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, primaryRed);
            Font docTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, darkSlate);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, darkSlate);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 8, textDark);
            Font regularSubduedFont = FontFactory.getFont(FontFactory.HELVETICA, 7, textSubdued);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, textDark);
            Font boldRedFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, primaryRed);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, java.awt.Color.WHITE);

            // Branded Top Bar Accent
            PdfPTable topBar = new PdfPTable(1);
            topBar.setWidthPercentage(100);
            PdfPCell barCell = new PdfPCell();
            barCell.setFixedHeight(4f);
            barCell.setBackgroundColor(primaryRed);
            barCell.setBorder(Rectangle.NO_BORDER);
            topBar.addCell(barCell);
            document.add(topBar);
            document.add(new Paragraph(" ")); // Spacer

            // Header Table (Logo vs Invoice Info)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{55, 45});

            // Logo and tagline
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            Paragraph brandNameParagraph = new Paragraph(siteName, brandFont);
            brandNameParagraph.setSpacingAfter(2f);
            Paragraph brandTagline = new Paragraph("INTEGRATED SECURITY & TECHNOLOGY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, textSubdued));
            logoCell.addElement(brandNameParagraph);
            logoCell.addElement(brandTagline);
            headerTable.addCell(logoCell);

            // Document Metadata
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            Paragraph docTitle = new Paragraph("FACTURA DE VENTA", docTitleFont);
            docTitle.setAlignment(Element.ALIGN_RIGHT);
            docTitle.setSpacingAfter(4f);
            
            Paragraph numParagraph = new Paragraph("N°: " + invoice.getInvoiceNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, primaryRed));
            numParagraph.setAlignment(Element.ALIGN_RIGHT);
            numParagraph.setSpacingAfter(2f);
            
            Paragraph dateParagraph = new Paragraph("Fecha Emisión: " + invoice.getIssueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), regularFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            
            Paragraph dueDateParagraph = new Paragraph("Fecha Vence: " + invoice.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), regularFont);
            dueDateParagraph.setAlignment(Element.ALIGN_RIGHT);
            
            Paragraph statusParagraph = new Paragraph("Estado: " + (invoice.getStatus() != null ? invoice.getStatus().name() : "PENDIENTE"), regularSubduedFont);
            statusParagraph.setAlignment(Element.ALIGN_RIGHT);
            
            infoCell.addElement(docTitle);
            infoCell.addElement(numParagraph);
            infoCell.addElement(dateParagraph);
            infoCell.addElement(dueDateParagraph);
            infoCell.addElement(statusParagraph);
            headerTable.addCell(infoCell);

            document.add(headerTable);
            document.add(new Paragraph(" ")); // Spacer

            // Company & Client Details (Cards styled with light background)
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{48, 48});
            detailsTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Provider Info Card
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBackgroundColor(lightGrayBg);
            companyCell.setBorderColor(borderGray);
            companyCell.setBorderWidth(1f);
            companyCell.setPadding(10f);
            
            Paragraph provHeader = new Paragraph("EMISOR", sectionHeaderFont);
            provHeader.setSpacingAfter(4f);
            companyCell.addElement(provHeader);
            
            companyCell.addElement(new Paragraph(siteName + " Comprehensive Services", boldFont));
            companyCell.addElement(new Paragraph("Tel: " + phone, regularFont));
            companyCell.addElement(new Paragraph("Email: " + email, regularFont));
            companyCell.addElement(new Paragraph("Dirección: " + address, regularFont));
            detailsTable.addCell(companyCell);

            // Client Info Card
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBackgroundColor(lightGrayBg);
            clientCell.setBorderColor(borderGray);
            clientCell.setBorderWidth(1f);
            clientCell.setPadding(10f);
            
            Paragraph clientHeader = new Paragraph("CLIENTE", sectionHeaderFont);
            clientHeader.setSpacingAfter(4f);
            clientCell.addElement(clientHeader);
            
            clientCell.addElement(new Paragraph(invoice.getClientName(), boldFont));
            if (invoice.getClientNit() != null && !invoice.getClientNit().isEmpty()) {
                clientCell.addElement(new Paragraph("NIT/C.C.: " + invoice.getClientNit(), regularFont));
            }
            clientCell.addElement(new Paragraph("Tel: " + invoice.getClientPhone(), regularFont));
            clientCell.addElement(new Paragraph("Email: " + invoice.getClientEmail(), regularFont));
            clientCell.addElement(new Paragraph("Dirección: " + (invoice.getClientAddress() != null && !invoice.getClientAddress().isEmpty() ? invoice.getClientAddress() : "No especificada") + ", " + (invoice.getClientCity() != null ? invoice.getClientCity() : ""), regularFont));
            detailsTable.addCell(clientCell);

            document.add(detailsTable);
            document.add(new Paragraph(" ")); // Spacer

            // Metadata row for Invoice (Payment Method, payment form, seller, currency)
            PdfPTable metaRow = new PdfPTable(4);
            metaRow.setWidthPercentage(100);
            metaRow.setWidths(new float[]{25, 25, 25, 25});
            
            String[][] metaFields = {
                {"Forma de Pago:", invoice.getPaymentForm() != null ? invoice.getPaymentForm() : "Contado"},
                {"Método de Pago:", invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "Efectivo"},
                {"Vendedor:", invoice.getSeller() != null && !invoice.getSeller().isEmpty() ? invoice.getSeller() : "N/A"},
                {"Moneda:", invoice.getCurrency() != null ? invoice.getCurrency() : "COP"}
            };
            
            for (String[] field : metaFields) {
                PdfPCell cell = new PdfPCell();
                cell.setBorderColor(borderGray);
                cell.setBackgroundColor(lightGrayBg);
                cell.setPadding(6f);
                cell.addElement(new Paragraph(field[0], regularSubduedFont));
                cell.addElement(new Paragraph(field[1], boldFont));
                metaRow.addCell(cell);
            }
            document.add(metaRow);
            document.add(new Paragraph(" ")); // Spacer

            // Items Table
            PdfPTable itemsTable = new PdfPTable(8);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{10, 25, 23, 7, 7, 10, 8, 10});
            itemsTable.setKeepTogether(true);

            // Table Headers
            String[] headers = {"Código", "Producto/Servicio", "Descripción", "Cant.", "Unidad", "Precio Unit.", "IVA", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, tableHeaderFont));
                cell.setBackgroundColor(darkSlate);
                cell.setPadding(6f);
                cell.setBorderColor(borderGray);
                cell.setHorizontalAlignment(
                    header.equals("Cant.") || header.equals("Unidad") || header.equals("IVA") ? Element.ALIGN_CENTER : 
                    header.equals("Precio Unit.") || header.equals("Total") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT
                );
                itemsTable.addCell(cell);
            }

            // Populate Items with Zebra Striping
            boolean isEven = false;
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                for (Invoice.InvoiceItem item : invoice.getItems()) {
                    java.awt.Color rowBg = isEven ? lightGrayBg : java.awt.Color.WHITE;
                    
                    PdfPCell codeCell = new PdfPCell(new Paragraph(item.getCode() != null ? item.getCode() : "", regularFont));
                    codeCell.setBackgroundColor(rowBg);
                    codeCell.setBorderColor(borderGray);
                    codeCell.setPadding(6f);
                    itemsTable.addCell(codeCell);

                    PdfPCell nameCell = new PdfPCell(new Paragraph(item.getName() != null ? item.getName() : "", regularFont));
                    nameCell.setBackgroundColor(rowBg);
                    nameCell.setBorderColor(borderGray);
                    nameCell.setPadding(6f);
                    itemsTable.addCell(nameCell);

                    PdfPCell descCell = new PdfPCell(new Paragraph(item.getDescription() != null ? item.getDescription() : "", regularFont));
                    descCell.setBackgroundColor(rowBg);
                    descCell.setBorderColor(borderGray);
                    descCell.setPadding(6f);
                    itemsTable.addCell(descCell);

                    PdfPCell qtyCell = new PdfPCell(new Paragraph(String.valueOf(item.getQuantity()), regularFont));
                    qtyCell.setBackgroundColor(rowBg);
                    qtyCell.setBorderColor(borderGray);
                    qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    qtyCell.setPadding(6f);
                    itemsTable.addCell(qtyCell);

                    PdfPCell unitCell = new PdfPCell(new Paragraph(item.getUnit() != null ? item.getUnit() : "Und", regularFont));
                    unitCell.setBackgroundColor(rowBg);
                    unitCell.setBorderColor(borderGray);
                    unitCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    unitCell.setPadding(6f);
                    itemsTable.addCell(unitCell);

                    PdfPCell priceCell = new PdfPCell(new Paragraph(String.format("$%,.2f", item.getUnitPrice()), regularFont));
                    priceCell.setBackgroundColor(rowBg);
                    priceCell.setBorderColor(borderGray);
                    priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    priceCell.setPadding(6f);
                    itemsTable.addCell(priceCell);

                    PdfPCell taxCell = new PdfPCell(new Paragraph(String.format("%.0f%%", item.getTaxRate()), regularFont));
                    taxCell.setBackgroundColor(rowBg);
                    taxCell.setBorderColor(borderGray);
                    taxCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    taxCell.setPadding(6f);
                    itemsTable.addCell(taxCell);

                    PdfPCell totalCell = new PdfPCell(new Paragraph(String.format("$%,.2f", item.getSubtotal()), boldFont));
                    totalCell.setBackgroundColor(rowBg);
                    totalCell.setBorderColor(borderGray);
                    totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalCell.setPadding(6f);
                    itemsTable.addCell(totalCell);
                    
                    isEven = !isEven;
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Paragraph("No se han agregado ítems detallados.", regularFont));
                emptyCell.setColspan(8);
                emptyCell.setPadding(12f);
                emptyCell.setBorderColor(borderGray);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(emptyCell);
            }

            document.add(itemsTable);
            document.add(new Paragraph(" ")); // Spacer

            // Totals and Observations
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{55, 45});
            totalsTable.setKeepTogether(true);

            // Left column: Observations Card & Total in Words
            PdfPCell obsCell = new PdfPCell();
            obsCell.setBorderColor(borderGray);
            obsCell.setBackgroundColor(lightGrayBg);
            obsCell.setPadding(10f);
            
            Paragraph obsTitle = new Paragraph("Términos & Observaciones", sectionHeaderFont);
            obsTitle.setSpacingAfter(4f);
            obsCell.addElement(obsTitle);
            
            String obs = invoice.getObservations() != null && !invoice.getObservations().isEmpty() ? invoice.getObservations() : "Esta factura de venta se asimila en todos sus efectos legales a una letra de cambio según el artículo 774 del código de comercio.";
            Paragraph obsContent = new Paragraph(obs, regularFont);
            obsContent.setLeading(10f);
            obsCell.addElement(obsContent);
            
            obsCell.addElement(new Paragraph(" "));
            Paragraph wordsTitle = new Paragraph("VALOR TOTAL EN LETRAS (" + invoice.getCurrency() + "):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, textSubdued));
            wordsTitle.setSpacingAfter(2f);
            obsCell.addElement(wordsTitle);
            Paragraph wordsContent = new Paragraph(invoice.getTotalInWords() != null ? invoice.getTotalInWords() : "", boldFont);
            obsCell.addElement(wordsContent);
            
            totalsTable.addCell(obsCell);

            // Right column: Totals Summary
            PdfPCell valCell = new PdfPCell();
            valCell.setBorder(Rectangle.NO_BORDER);
            valCell.setPaddingLeft(15f);
            
            PdfPTable rightTable = new PdfPTable(2);
            rightTable.setWidthPercentage(100);
            rightTable.setWidths(new float[]{55, 45});
            
            // Subtotal
            PdfPCell subLabelCell = new PdfPCell(new Paragraph("Subtotal:", regularFont));
            subLabelCell.setBorder(Rectangle.BOTTOM);
            subLabelCell.setBorderColor(borderGray);
            subLabelCell.setPadding(5f);
            rightTable.addCell(subLabelCell);
            
            PdfPCell subValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", invoice.getSubtotal()), regularFont));
            subValCell.setBorder(Rectangle.BOTTOM);
            subValCell.setBorderColor(borderGray);
            subValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subValCell.setPadding(5f);
            rightTable.addCell(subValCell);
            
            // Discount
            if (invoice.getDiscountAmount() > 0) {
                PdfPCell discLabelCell = new PdfPCell(new Paragraph("Descuento:", regularFont));
                discLabelCell.setBorder(Rectangle.BOTTOM);
                discLabelCell.setBorderColor(borderGray);
                discLabelCell.setPadding(5f);
                rightTable.addCell(discLabelCell);
                
                PdfPCell discValCell = new PdfPCell(new Paragraph(String.format("-$%,.2f", invoice.getDiscountAmount()), regularFont));
                discValCell.setBorder(Rectangle.BOTTOM);
                discValCell.setBorderColor(borderGray);
                discValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                discValCell.setPadding(5f);
                rightTable.addCell(discValCell);
            }
            
            // IVA
            PdfPCell taxLabelCell = new PdfPCell(new Paragraph("IVA:", regularFont));
            taxLabelCell.setBorder(Rectangle.BOTTOM);
            taxLabelCell.setBorderColor(borderGray);
            taxLabelCell.setPadding(5f);
            rightTable.addCell(taxLabelCell);
            
            PdfPCell taxValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", invoice.getTaxAmount()), regularFont));
            taxValCell.setBorder(Rectangle.BOTTOM);
            taxValCell.setBorderColor(borderGray);
            taxValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            taxValCell.setPadding(5f);
            rightTable.addCell(taxValCell);
            
            // Grand Total
            PdfPCell totalLabelCell = new PdfPCell(new Paragraph("Total:", boldRedFont));
            totalLabelCell.setBackgroundColor(lightGrayBg);
            totalLabelCell.setBorder(Rectangle.BOX);
            totalLabelCell.setBorderColor(borderGray);
            totalLabelCell.setPadding(6f);
            rightTable.addCell(totalLabelCell);
            
            PdfPCell totalValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", invoice.getTotal()), boldRedFont));
            totalValCell.setBackgroundColor(lightGrayBg);
            totalValCell.setBorder(Rectangle.BOX);
            totalValCell.setBorderColor(borderGray);
            totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValCell.setPadding(6f);
            rightTable.addCell(totalValCell);
            
            // Paid Amount
            PdfPCell paidLabelCell = new PdfPCell(new Paragraph("Valor Pagado:", regularFont));
            paidLabelCell.setBorder(Rectangle.BOTTOM);
            paidLabelCell.setBorderColor(borderGray);
            paidLabelCell.setPadding(5f);
            rightTable.addCell(paidLabelCell);
            
            PdfPCell paidValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", invoice.getPaidAmount()), regularFont));
            paidValCell.setBorder(Rectangle.BOTTOM);
            paidValCell.setBorderColor(borderGray);
            paidValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidValCell.setPadding(5f);
            rightTable.addCell(paidValCell);
            
            // Balance Due
            PdfPCell dueLabelCell = new PdfPCell(new Paragraph("Saldo Pendiente:", boldFont));
            dueLabelCell.setBorder(Rectangle.BOTTOM);
            dueLabelCell.setBorderColor(borderGray);
            dueLabelCell.setPadding(5f);
            rightTable.addCell(dueLabelCell);
            
            PdfPCell dueValCell = new PdfPCell(new Paragraph(String.format("$%,.2f", invoice.getDueAmount()), boldFont));
            dueValCell.setBorder(Rectangle.BOTTOM);
            dueValCell.setBorderColor(borderGray);
            dueValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dueValCell.setPadding(5f);
            rightTable.addCell(dueValCell);
            
            valCell.addElement(rightTable);
            totalsTable.addCell(valCell);

            document.add(totalsTable);
            
            // Space for signatures
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setWidths(new float[]{50, 50});
            signatureTable.setKeepTogether(true);
            
            PdfPCell sig1 = new PdfPCell(new Paragraph("\n\n___________________________________\nFirma Autorizada REDCONFER", regularFont));
            sig1.setBorder(Rectangle.NO_BORDER);
            sig1.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            PdfPCell sig2 = new PdfPCell(new Paragraph("\n\n___________________________________\nRecibí Conforme (Cliente)", regularFont));
            sig2.setBorder(Rectangle.NO_BORDER);
            sig2.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            signatureTable.addCell(sig1);
            signatureTable.addCell(sig2);
            document.add(signatureTable);
            
            // Bottom Branded Footer
            document.add(new Paragraph(" "));
            Paragraph signatureText = new Paragraph("Factura generada electrónicamente por REDCONFER. ¡Gracias por su compra!", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, textSubdued));
            signatureText.setAlignment(Element.ALIGN_CENTER);
            document.add(signatureText);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
