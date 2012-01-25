/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2011 by Rapid-I and the contributors
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
package com.rapidminer.gui.new_plotter.templates.style;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.gui.new_plotter.gui.FontDialog;
import com.rapidminer.tools.I18N;

/**
 * This class provides a GUI for the {@link DefaultPlotterStyleProvider}.
 * 
 * @author Marco Boeck
 *
 */
public class DefaultPlotterStyleProviderGUI extends JPanel implements Observer {
	
	/** the axes font change button */
	private JButton axesFontButton;
	
	/** the legend font change button */
	private JButton legendFontButton;
	
	/** the title font change button */
	private JButton titleFontButton;
	
	/** the combo box containing the color schemes */
	private JComboBox colorSchemeComboBox;
	
	
	private static final long serialVersionUID = -6394913829696833045L;
	
	
	/**
	 * Creates a new {@link JPanel} to edit the {@link DefaultPlotterStyleProvider} settings.
	 * @param defaultStylProvider
	 */
	public DefaultPlotterStyleProviderGUI(final DefaultPlotterStyleProvider defaultStyleProvider) {
		defaultStyleProvider.addObserver(this);
		final Font axesFont = defaultStyleProvider.getAxesFont();
		final Font titleFont = defaultStyleProvider.getTitleFont();
		final Font legendFont = defaultStyleProvider.getLegendFont();
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		// start layout
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.weighty = 0;
		JLabel descriptionLabel = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.description.label"));
		this.add(descriptionLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		// removed by Simon's request; why does nobody like them :-(
//		this.add(new JSeparator(), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 1;
		JLabel axesLabel = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.axes.label"));
		this.add(axesLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		axesFontButton = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.button.label"));
		axesFontButton.setFont(new Font(axesFont.getFamily(), axesFont.getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
		axesFontButton.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.axes.tip"));
		axesFontButton.setPreferredSize(new Dimension(120, 30));
		axesFontButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FontDialog fontDialog = new FontDialog(null, defaultStyleProvider.getAxesFont(), "select_font");
				fontDialog.setVisible(true);
				fontDialog.requestFocusInWindow();
				if (fontDialog.getReturnStatus() == FontDialog.RET_OK) {
					if (fontDialog.getFont() != null) {
						Font axesFont = fontDialog.getFont();
						defaultStyleProvider.setAxesFont(axesFont);
						axesFontButton.setFont(new Font(axesFont.getName(), axesFont.getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
					}
				}
				fontDialog.dispose();
			}
		});
		this.add(axesFontButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		JLabel titleLabel = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.title.label"));
		this.add(titleLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		titleFontButton = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.button.label"));
		titleFontButton.setFont(new Font(titleFont.getFamily(), titleFont.getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
		titleFontButton.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.title.tip"));
		titleFontButton.setPreferredSize(new Dimension(120, 30));
		titleFontButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FontDialog fontDialog = new FontDialog(null, defaultStyleProvider.getTitleFont(), "select_font");
				fontDialog.setVisible(true);
				fontDialog.requestFocusInWindow();
				if (fontDialog.getReturnStatus() == FontDialog.RET_OK) {
					if (fontDialog.getFont() != null) {
						Font titleFont = fontDialog.getFont();
						defaultStyleProvider.setTitleFont(titleFont);
						titleFontButton.setFont(new Font(titleFont.getName(), titleFont.getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
					}
				}
				fontDialog.dispose();
			}
		});
		this.add(titleFontButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		JLabel legendLabel = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.legend.label"));
		this.add(legendLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 4;
		legendFontButton = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.button.label"));
		legendFontButton.setFont(new Font(legendFont.getFamily(), legendFont.getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
		legendFontButton.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.font.legend.tip"));
		legendFontButton.setPreferredSize(new Dimension(120, 30));
		legendFontButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FontDialog fontDialog = new FontDialog(null, defaultStyleProvider.getLegendFont(), "select_font");
				fontDialog.setVisible(true);
				fontDialog.requestFocusInWindow();
				if (fontDialog.getReturnStatus() == FontDialog.RET_OK) {
					if (fontDialog.getFont() != null) {
						Font legendFont = fontDialog.getFont();
						defaultStyleProvider.setLegendFont(legendFont);
						legendFontButton.setFont(new Font(legendFont.getName(), legendFont.getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
					}
				}
				fontDialog.dispose();
			}
		});
		this.add(legendFontButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		JLabel colorLabel = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.colorscheme.label"));
		this.add(colorLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		colorSchemeComboBox = new JComboBox(defaultStyleProvider.getColorSchemes().toArray());
		colorSchemeComboBox.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.styleprovider.colorscheme.tip"));
		colorSchemeComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				defaultStyleProvider.setSelectedColorSchemeIndex(colorSchemeComboBox.getSelectedIndex());
			}
		});
		this.add(colorSchemeComboBox, gbc);
		
		// fill empty area
		gbc.gridx = 0;
		gbc.gridy = 999;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		this.add(new JLabel(), gbc);
	}


	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof DefaultPlotterStyleProvider) {
			// update ComboBox with ColorSchemes
			DefaultPlotterStyleProvider defaultStyleProvider = (DefaultPlotterStyleProvider)o;
			Object selectedItem = colorSchemeComboBox.getSelectedItem();
			colorSchemeComboBox.setModel(new DefaultComboBoxModel(defaultStyleProvider.getColorSchemes().toArray()));
			colorSchemeComboBox.setSelectedItem(selectedItem);
			
			// update font buttons
			axesFontButton.setFont(new Font(defaultStyleProvider.getAxesFont().getName(), defaultStyleProvider.getAxesFont().getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
			titleFontButton.setFont(new Font(defaultStyleProvider.getTitleFont().getName(), defaultStyleProvider.getTitleFont().getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
			legendFontButton.setFont(new Font(defaultStyleProvider.getLegendFont().getName(), defaultStyleProvider.getLegendFont().getStyle(), DefaultPlotterStyleProvider.FONT_SIZE_DEFAULT));
			
		}
	}

}
