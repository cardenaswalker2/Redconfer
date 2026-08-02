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
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);

        try {
            Settings settings = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
            String siteName = settings != null ? settings.getSiteName() : "REDCONFER";
            String phone = settings != null ? settings.getPhone() : "+57 323 357 0996";
            String email = settings != null ? settings.getEmail() : "contacto@redconfer.com";
            String address = settings != null ? settings.getAddress() : "Calle 100 #15-30, Cartagena, Colombia";

            PdfWriter.getInstance(document, out);
            document.open();

            // Colors
            java.awt.Color primaryRed = new java.awt.Color(198, 26, 34);     // #C61A22
            java.awt.Color darkSlate = new java.awt.Color(15, 23, 42);       // #0F172A
            java.awt.Color lightGrayBg = new java.awt.Color(248, 250, 252);  // #F8FAFC
            java.awt.Color borderGray = new java.awt.Color(226, 232, 240);   // #E2E8F0
            java.awt.Color textDark = new java.awt.Color(51, 65, 85);        // #334155
            java.awt.Color textSubdued = new java.awt.Color(100, 116, 139);  // #64748B
            java.awt.Color badgeGreen = new java.awt.Color(16, 185, 129);    // #10B981
            java.awt.Color badgeYellow = new java.awt.Color(245, 158, 11);   // #F59E0B
            java.awt.Color badgeRed = new java.awt.Color(239, 68, 68);       // #EF4444

            // Fonts
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, primaryRed);
            Font docTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, darkSlate);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, darkSlate);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, textDark);
            Font regularSubduedFont = FontFactory.getFont(FontFactory.HELVETICA, 6.5f, textSubdued);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, textDark);
            Font boldRedFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, primaryRed);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, java.awt.Color.WHITE);

            // Branded Top Bar
            PdfPTable topBar = new PdfPTable(1);
            topBar.setWidthPercentage(100);
            PdfPCell barCell = new PdfPCell();
            barCell.setFixedHeight(4f);
            barCell.setBackgroundColor(primaryRed);
            barCell.setBorder(Rectangle.NO_BORDER);
            topBar.addCell(barCell);
            document.add(topBar);
            document.add(new Paragraph(" "));

            // Main Header Table
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{45, 38, 17});

            // Column 1: Company Info
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            Paragraph brandNameParagraph = new Paragraph(siteName, brandFont);
            brandNameParagraph.setSpacingAfter(1f);
            companyCell.addElement(brandNameParagraph);
            companyCell.addElement(new Paragraph("INTEGRATED SECURITY & TECHNOLOGY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, textSubdued)));
            companyCell.addElement(new Paragraph("NIT: 900.123.456-7", boldFont));
            companyCell.addElement(new Paragraph("Dirección: " + address, regularFont));
            companyCell.addElement(new Paragraph("Tel: " + phone, regularFont));
            companyCell.addElement(new Paragraph("Email: " + email, regularFont));
            companyCell.addElement(new Paragraph("Web: www.redconfer.com", regularFont));
            headerTable.addCell(companyCell);

            // Column 2: Invoice Metadata & Badge
            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.NO_BORDER);
            metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            Paragraph titlePara = new Paragraph("FACTURA DE VENTA", docTitleFont);
            titlePara.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(titlePara);
            
            Paragraph numberPara = new Paragraph("N°: " + invoice.getInvoiceNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, primaryRed));
            numberPara.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(numberPara);
            
            metaCell.addElement(new Paragraph("Fecha Emisión: " + invoice.getIssueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), regularFont));
            metaCell.addElement(new Paragraph("Fecha Vencimiento: " + invoice.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), regularFont));
            
            // Status Badge
            String status = invoice.getStatus() != null ? invoice.getStatus().name() : "PENDIENTE";
            java.awt.Color badgeColor = badgeYellow;
            if (status.equals("PAGADA")) badgeColor = badgeGreen;
            else if (status.equals("VENCIDA")) badgeColor = badgeRed;
            else if (status.equals("ANULADA")) badgeColor = textSubdued;

            PdfPTable badgeTable = new PdfPTable(1);
            badgeTable.setWidthPercentage(40);
            badgeTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell badgeCell = new PdfPCell(new Paragraph(status, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, java.awt.Color.WHITE)));
            badgeCell.setBackgroundColor(badgeColor);
            badgeCell.setBorder(Rectangle.NO_BORDER);
            badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            badgeCell.setPadding(3f);
            badgeTable.addCell(badgeCell);
            
            metaCell.addElement(new Paragraph(" "));
            metaCell.addElement(badgeTable);
            
            // References
            if (invoice.getWorkOrderNumber() != null && !invoice.getWorkOrderNumber().isEmpty()) {
                metaCell.addElement(new Paragraph("Orden: " + invoice.getWorkOrderNumber(), regularSubduedFont));
            }
            if (invoice.getQuoteNumber() != null && !invoice.getQuoteNumber().isEmpty()) {
                metaCell.addElement(new Paragraph("Cotización: " + invoice.getQuoteNumber(), regularSubduedFont));
            }
            if (invoice.getProjectCode() != null && !invoice.getProjectCode().isEmpty()) {
                metaCell.addElement(new Paragraph("Proyecto: " + invoice.getProjectCode(), regularSubduedFont));
            }
            headerTable.addCell(metaCell);

            // Column 3: QR Code
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            try {
                String verificationUrl = "https://redconfer.onrender.com/verificar/" + invoice.getInvoiceNumber();
                String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=" + java.net.URLEncoder.encode(verificationUrl, "UTF-8");
                Image qrImage = Image.getInstance(qrApiUrl);
                qrImage.scaleToFit(65, 65);
                qrImage.setAlignment(Element.ALIGN_RIGHT);
                qrCell.addElement(qrImage);
                Paragraph qrLabel = new Paragraph("Verificar Factura", FontFactory.getFont(FontFactory.HELVETICA, 6, textSubdued));
                qrLabel.setAlignment(Element.ALIGN_RIGHT);
                qrCell.addElement(qrLabel);
            } catch (Exception e) {
                // Skip if network fails
            }
            headerTable.addCell(qrCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // Client Info Card (Full Width block)
            PdfPTable clientTable = new PdfPTable(1);
            clientTable.setWidthPercentage(100);
            PdfPCell clientOuterCell = new PdfPCell();
            clientOuterCell.setBackgroundColor(lightGrayBg);
            clientOuterCell.setBorderColor(borderGray);
            clientOuterCell.setPadding(8f);
            
            clientOuterCell.addElement(new Paragraph("CLIENTE", sectionHeaderFont));
            clientOuterCell.addElement(new Paragraph(invoice.getClientName(), boldFont));
            clientOuterCell.addElement(new Paragraph("NIT/C.C.: " + (invoice.getClientNit() != null ? invoice.getClientNit() : "N/A"), regularFont));
            clientOuterCell.addElement(new Paragraph("Teléfono: " + invoice.getClientPhone() + " | Correo: " + invoice.getClientEmail(), regularFont));
            clientOuterCell.addElement(new Paragraph("Dirección: " + invoice.getClientAddress() + ", " + (invoice.getClientCity() != null ? invoice.getClientCity() : ""), regularFont));
            
            clientTable.addCell(clientOuterCell);
            document.add(clientTable);
            document.add(new Paragraph(" "));

            // Payment Metadata row
            PdfPTable metaRow = new PdfPTable(4);
            metaRow.setWidthPercentage(100);
            metaRow.setWidths(new float[]{25, 25, 25, 25});
            String[][] metaFields = {
                {"Forma de Pago:", invoice.getPaymentForm() != null ? invoice.getPaymentForm() : "Contado"},
                {"Método de Pago:", invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "Efectivo"},
                {"Vendedor:", invoice.getSeller() != null && !invoice.getSeller().isEmpty() ? invoice.getSeller() : "Asesor REDCONFER"},
                {"Moneda:", invoice.getCurrency() != null ? invoice.getCurrency() : "COP"}
            };
            for (String[] field : metaFields) {
                PdfPCell cell = new PdfPCell();
                cell.setBorderColor(borderGray);
                cell.setBackgroundColor(lightGrayBg);
                cell.setPadding(5f);
                cell.addElement(new Paragraph(field[0], regularSubduedFont));
                cell.addElement(new Paragraph(field[1], boldFont));
                metaRow.addCell(cell);
            }
            document.add(metaRow);
            document.add(new Paragraph(" "));

            // Items Table
            PdfPTable itemsTable = new PdfPTable(8);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{8, 26, 26, 6, 6, 9, 8, 11});
            itemsTable.setKeepTogether(true);

            String[] headers = {"Código", "Producto/Servicio", "Descripción", "Cant.", "Und", "P. Unitario", "Dcto %", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, tableHeaderFont));
                cell.setBackgroundColor(darkSlate);
                cell.setPadding(5f);
                cell.setBorderColor(borderGray);
                cell.setHorizontalAlignment(
                    header.equals("Cant.") || header.equals("Und") || header.equals("Dcto %") ? Element.ALIGN_CENTER : 
                    header.equals("P. Unitario") || header.equals("Total") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT
                );
                itemsTable.addCell(cell);
            }

            boolean isEven = false;
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                for (Invoice.InvoiceItem item : invoice.getItems()) {
                    java.awt.Color rowBg = isEven ? lightGrayBg : java.awt.Color.WHITE;
                    
                    PdfPCell codeCell = new PdfPCell(new Paragraph(item.getCode() != null ? item.getCode() : "", regularFont));
                    codeCell.setBackgroundColor(rowBg);
                    codeCell.setBorderColor(borderGray);
                    codeCell.setPadding(5f);
                    itemsTable.addCell(codeCell);

                    PdfPCell nameCell = new PdfPCell(new Paragraph(item.getName() != null ? item.getName() : "", regularFont));
                    nameCell.setBackgroundColor(rowBg);
                    nameCell.setBorderColor(borderGray);
                    nameCell.setPadding(5f);
                    itemsTable.addCell(nameCell);

                    PdfPCell descCell = new PdfPCell(new Paragraph(item.getDescription() != null ? item.getDescription() : "", regularFont));
                    descCell.setBackgroundColor(rowBg);
                    descCell.setBorderColor(borderGray);
                    descCell.setPadding(5f);
                    itemsTable.addCell(descCell);

                    PdfPCell qtyCell = new PdfPCell(new Paragraph(String.valueOf(item.getQuantity()), regularFont));
                    qtyCell.setBackgroundColor(rowBg);
                    qtyCell.setBorderColor(borderGray);
                    qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    qtyCell.setPadding(5f);
                    itemsTable.addCell(qtyCell);

                    PdfPCell unitCell = new PdfPCell(new Paragraph(item.getUnit() != null ? item.getUnit() : "Und", regularFont));
                    unitCell.setBackgroundColor(rowBg);
                    unitCell.setBorderColor(borderGray);
                    unitCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    unitCell.setPadding(5f);
                    itemsTable.addCell(unitCell);

                    PdfPCell priceCell = new PdfPCell(new Paragraph(String.format("$%,.2f", item.getUnitPrice()), regularFont));
                    priceCell.setBackgroundColor(rowBg);
                    priceCell.setBorderColor(borderGray);
                    priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    priceCell.setPadding(5f);
                    itemsTable.addCell(priceCell);

                    PdfPCell discCell = new PdfPCell(new Paragraph(String.format("%.1f%%", item.getDiscountPercent()), regularFont));
                    discCell.setBackgroundColor(rowBg);
                    discCell.setBorderColor(borderGray);
                    discCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    discCell.setPadding(5f);
                    itemsTable.addCell(discCell);

                    PdfPCell totalCell = new PdfPCell(new Paragraph(String.format("$%,.2f", item.getSubtotal()), boldFont));
                    totalCell.setBackgroundColor(rowBg);
                    totalCell.setBorderColor(borderGray);
                    totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalCell.setPadding(5f);
                    itemsTable.addCell(totalCell);
                    
                    isEven = !isEven;
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Paragraph("No se han agregado ítems detallados.", regularFont));
                emptyCell.setColspan(8);
                emptyCell.setPadding(10f);
                emptyCell.setBorderColor(borderGray);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(emptyCell);
            }
            document.add(itemsTable);
            document.add(new Paragraph(" "));

            // Split Grid: Left (Project info & terms) & Right (Totals & payment details)
            PdfPTable splitGrid = new PdfPTable(2);
            splitGrid.setWidthPercentage(100);
            splitGrid.setWidths(new float[]{50, 50});
            splitGrid.setKeepTogether(true);

            // Left side
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPaddingRight(8f);

            // Project Info
            PdfPTable projTable = new PdfPTable(1);
            projTable.setWidthPercentage(100);
            PdfPCell projCell = new PdfPCell();
            projCell.setBackgroundColor(lightGrayBg);
            projCell.setBorderColor(borderGray);
            projCell.setPadding(8f);
            projCell.addElement(new Paragraph("INFORMACIÓN DEL PROYECTO", sectionHeaderFont));
            projCell.addElement(new Paragraph("Proyecto: " + (invoice.getProjectName() != null ? invoice.getProjectName() : "N/A"), boldFont));
            projCell.addElement(new Paragraph("Técnico Encargado: " + (invoice.getTechnicianName() != null ? invoice.getTechnicianName() : "Asignado General"), regularFont));
            projCell.addElement(new Paragraph("Supervisor: " + (invoice.getSupervisorName() != null ? invoice.getSupervisorName() : "Asignador Técnico"), regularFont));
            projCell.addElement(new Paragraph("Garantía: " + (invoice.getWarrantyTerm() != null ? invoice.getWarrantyTerm() : "12 Meses"), regularFont));
            projTable.addCell(projCell);
            leftCell.addElement(projTable);
            leftCell.addElement(new Paragraph(" "));

            // Terms
            PdfPTable termsTable = new PdfPTable(1);
            termsTable.setWidthPercentage(100);
            PdfPCell termsCell = new PdfPCell();
            termsCell.setBackgroundColor(lightGrayBg);
            termsCell.setBorderColor(borderGray);
            termsCell.setPadding(8f);
            termsCell.addElement(new Paragraph("TÉRMINOS Y CONDICIONES", sectionHeaderFont));
            String terms = invoice.getObservations() != null && !invoice.getObservations().isEmpty() ? invoice.getObservations() : "Esta factura de venta se asimila a una letra de cambio según art. 774 de código de comercio. Garantía de 12 meses en equipos.";
            termsCell.addElement(new Paragraph(terms, regularSubduedFont));
            termsTable.addCell(termsCell);
            leftCell.addElement(termsTable);
            
            leftCell.addElement(new Paragraph(" "));
            leftCell.addElement(new Paragraph("VALOR TOTAL EN LETRAS (" + invoice.getCurrency() + "):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, textSubdued)));
            leftCell.addElement(new Paragraph(invoice.getTotalInWords() != null ? invoice.getTotalInWords() : "", boldFont));

            splitGrid.addCell(leftCell);

            // Right side
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPaddingLeft(8f);

            // Totals
            PdfPTable rightTotals = new PdfPTable(2);
            rightTotals.setWidthPercentage(100);
            rightTotals.setWidths(new float[]{60, 40});

            String[][] totalsFields = {
                {"Subtotal:", String.format("$%,.2f", invoice.getSubtotal())},
                {"Descuentos:", String.format("-$%,.2f", invoice.getDiscountAmount())},
                {"IVA (19%):", String.format("$%,.2f", invoice.getTaxAmount())},
                {"Monto Pagado:", String.format("$%,.2f", invoice.getPaidAmount())}
            };
            for (String[] tf : totalsFields) {
                PdfPCell lbl = new PdfPCell(new Paragraph(tf[0], regularFont));
                lbl.setBorder(Rectangle.BOTTOM);
                lbl.setBorderColor(borderGray);
                lbl.setPadding(4f);
                rightTotals.addCell(lbl);

                PdfPCell val = new PdfPCell(new Paragraph(tf[1], regularFont));
                val.setBorder(Rectangle.BOTTOM);
                val.setBorderColor(borderGray);
                val.setHorizontalAlignment(Element.ALIGN_RIGHT);
                val.setPadding(4f);
                rightTotals.addCell(val);
            }
            // Total
            PdfPCell totLbl = new PdfPCell(new Paragraph("TOTAL GENERAL:", boldRedFont));
            totLbl.setBackgroundColor(lightGrayBg);
            totLbl.setBorder(Rectangle.BOX);
            totLbl.setBorderColor(borderGray);
            totLbl.setPadding(6f);
            rightTotals.addCell(totLbl);

            PdfPCell totVal = new PdfPCell(new Paragraph(String.format("$%,.2f", invoice.getTotal()), boldRedFont));
            totVal.setBackgroundColor(lightGrayBg);
            totVal.setBorder(Rectangle.BOX);
            totVal.setBorderColor(borderGray);
            totVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totVal.setPadding(6f);
            rightTotals.addCell(totVal);

            // Due
            PdfPCell dueLbl = new PdfPCell(new Paragraph("SALDO PENDIENTE:", boldFont));
            dueLbl.setBorder(Rectangle.BOTTOM);
            dueLbl.setBorderColor(borderGray);
            dueLbl.setPadding(4f);
            rightTotals.addCell(dueLbl);

            PdfPCell dueVal = new PdfPCell(new Paragraph(String.format("$%,.2f", invoice.getDueAmount()), boldFont));
            dueVal.setBorder(Rectangle.BOTTOM);
            dueVal.setBorderColor(borderGray);
            dueVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dueVal.setPadding(4f);
            rightTotals.addCell(dueVal);

            rightCell.addElement(rightTotals);
            rightCell.addElement(new Paragraph(" "));

            // Payment Instructions
            PdfPTable payTable = new PdfPTable(1);
            payTable.setWidthPercentage(100);
            PdfPCell payCell = new PdfPCell();
            payCell.setBackgroundColor(lightGrayBg);
            payCell.setBorderColor(borderGray);
            payCell.setPadding(8f);
            payCell.addElement(new Paragraph("DATOS PARA PAGO", sectionHeaderFont));
            payCell.addElement(new Paragraph("Bancolombia - Cuenta Corriente", boldFont));
            payCell.addElement(new Paragraph("N° Cuenta: 12345678910", regularFont));
            payCell.addElement(new Paragraph("Titular: REDCONFER Comprehensive Services S.A.S.", regularFont));
            payCell.addElement(new Paragraph("NIT: 900.123.456-7", regularFont));
            payTable.addCell(payCell);
            rightCell.addElement(payTable);

            splitGrid.addCell(rightCell);
            document.add(splitGrid);
            document.add(new Paragraph(" "));

            // Branded Footer
            Paragraph signatureText = new Paragraph("Factura generada electrónicamente por REDCONFER. Verifique su autenticidad escaneando el código QR.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, textSubdued));
            signatureText.setAlignment(Element.ALIGN_CENTER);
            document.add(signatureText);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
