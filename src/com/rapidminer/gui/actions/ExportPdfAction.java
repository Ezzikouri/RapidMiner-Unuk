/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.actions;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.new_plotter.templates.PlotterTemplate;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;

/** Action to create a PDF file from the given {@link Component}.
 * 
 * @author Marco Boeck
 *
 */
public class ExportPdfAction extends ResourceAction {

	private PlotterTemplate template;
	private Component component;
	private final String componentName;

	public ExportPdfAction(Component component, String componentName) {
		super(true, "export_pdf", componentName);
		this.component = component;
		this.componentName = componentName;
	}
	
	public ExportPdfAction(PlotterTemplate template) {
		super(true, "export_pdf", template.getChartType());
		this.componentName = template.getChartType();
		this.template = template;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void actionPerformed(ActionEvent e) {
		if (component != null) {
			createPdf(component);
			return;
		}
		if (template != null) {
			createPdf(template);
			return;
		}
	}
	
	/**
	 * Create the PDF from a {@link Component}.
	 * 
	 * @param component
	 */
	private void createPdf(Component component) {
		if (component == null) {
			return;
		}
		
		// prompt user for pdf location
		File file = SwingTools.chooseFile(RapidMinerGUI.getMainFrame(), "export_pdf", null, false, false, new String[] { "pdf" }, new String[] { "PDF" }, false);
		if (file == null) {
			return;
		}
		// prompt for overwrite confirmation
		if (file.exists()) {
			int returnVal = SwingTools.showConfirmDialog("export_pdf", ConfirmDialog.YES_NO_OPTION, file.getName());
			if (returnVal == ConfirmDialog.NO_OPTION) {
				return;
			}
		}
		
		try {
			// create pdf document
			Document document = new Document(PageSize.A4);
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			createPdfViaTemplate(component, document, cb);
			document.close();
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("cannot_export_pdf", e, e.getMessage());
		}
	}
	
	/**
	 * Create the PDF from a {@link PlotterTemplate}.
	 * 
	 * @param template
	 */
	private void createPdf(PlotterTemplate template) {
		if (template == null) {
			return;
		}
		
		// prompt user for pdf location
		File file = SwingTools.chooseFile(RapidMinerGUI.getMainFrame(), "export_pdf", null, false, false, new String[] { "pdf" }, new String[] { "PDF" }, false);
		if (file == null) {
			return;
		}
		// prompt for overwrite confirmation
		if (file.exists()) {
			int returnVal = SwingTools.showConfirmDialog("export_pdf", ConfirmDialog.YES_NO_OPTION, file.getName());
			if (returnVal == ConfirmDialog.NO_OPTION) {
				return;
			}
		}
		
		try {
			// create pdf document
			Document document = new Document(PageSize.A4);
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			createPdfViaTemplate(template, document, cb);
			document.close();
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("cannot_export_pdf", e, e.getMessage());
		}
	}

	/**
	 * Creates a pdf showing the given {@link Component} via {@link PdfTemplate} usage.
	 * @param component
	 * @param document
	 * @param cb
	 * @throws DocumentException
	 */
	private void createPdfViaTemplate(Component component, Document document,PdfContentByte cb) throws DocumentException {
		PdfTemplate tp = cb.createTemplate(500, PageSize.A4.getHeight()/2);
		Graphics2D g2 = tp.createGraphics(500, PageSize.A4.getHeight()/2);
		AffineTransform at = new AffineTransform();
		double factor = 500d / component.getWidth();
		at.scale(factor, factor);
		g2.transform(at);
		component.print(g2);
		g2.dispose();
		document.add(new Paragraph(componentName));
		document.add(Image.getInstance(tp));
	}
	
	/**
	 * Creates a pdf showing the given {@link PlotterTemplate} via {@link PdfTemplate} usage.
	 * @param template
	 * @param document
	 * @param cb
	 * @throws DocumentException
	 */
	private void createPdfViaTemplate(PlotterTemplate template, Document document,PdfContentByte cb) throws DocumentException {
		PdfTemplate tp = cb.createTemplate(500, PageSize.A4.getHeight()/2);
		Graphics2D g2 = tp.createGraphics(500, PageSize.A4.getHeight()/2);
		AffineTransform at = new AffineTransform();
		double factor = 500d / template.getPlotEngine().getChartPanel().getWidth();
		at.scale(factor, factor);
		g2.transform(at);
		template.getPlotEngine().getChartPanel().print(g2);
		g2.dispose();
		document.add(new Paragraph(componentName));
		document.add(Image.getInstance(tp));
	}
}