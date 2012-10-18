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
package com.rapid_i.deployment.update.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import com.rapidminer.RapidMiner;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.gui.tools.SwingTools;

/**
 * Renders a cell of the update list. This contains icons for the type of extension or update.
 * 
 * @author Simon Fischer
 */
final class UpdateListCellRenderer implements ListCellRenderer {

    private static RenderingHints HI_QUALITY_HINTS = new RenderingHints(null);
    
    static {
        HI_QUALITY_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        HI_QUALITY_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        HI_QUALITY_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
    	
//	private static final Icon SELECTED_ICON = SwingTools.createIcon("16/checkbox.png");
//	private static final Icon NON_SELECTED_ICON = SwingTools.createIcon("16/checkbox_unchecked.png");
	
	//private final UpdatePanel packageDescriptorListPanel ;
    private final UpdatePackagesModel updateModel;
	private boolean allPurchased = false;

//	private final JLabel freeCommercial = new JLabel();
	
	UpdateListCellRenderer(UpdatePackagesModel updateModel) {
		this.updateModel = updateModel;
	}
	
	UpdateListCellRenderer(boolean allPurchased) {
		this.allPurchased = allPurchased;
		this.updateModel = null;
	}
	
	
	private Map<String,Icon> icons = new HashMap<String,Icon>();
	private Icon getIcon(PackageDescriptor pd) {
		if (pd.getIcon() == null) {
			return null;
		} else {
			Icon result = icons.get(pd.getPackageId());
			if (result == null) {
				result = new ImageIcon(pd.getIcon());
				icons.put(pd.getPackageId(), result);
			}
			return result;
		}
	}
	
	private Icon getResizedIcon(Icon originalIcon) {
		if (originalIcon == null) return null;
		int width = originalIcon.getIconWidth();
		int height = originalIcon.getIconHeight();
		if (width != 48) {
			double scale = (48.0/width);
			BufferedImage bi = new BufferedImage(
					(int)(scale*width),
					(int)(scale*height),
		            BufferedImage.TYPE_INT_ARGB);
		        Graphics2D g = bi.createGraphics();
		        g.setRenderingHints(HI_QUALITY_HINTS);
		        g.scale(scale, scale);
		        originalIcon.paintIcon(null,g,0,0);
		        g.dispose();
		        return new ImageIcon(bi);
		} else {
			return originalIcon;
		}
	}
	
	private String getFirstSentence(String text) {
		if (text != null && text.contains(".")) {
			String[] sentences = text.split("\\.");
			return sentences[0].trim() + ".";
		} else {
			return text;
		}
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JPanel panel = new JPanel();
		JLabel selectedLabel = new JLabel();
		JLabel label = new JLabel();
		
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(selectedLabel);
		//add(freeCommercial);		
		
		panel.add(label);		
		panel.setOpaque(true);

		
		if (isSelected) {
			panel.setBackground(SwingTools.LIGHT_BLUE);
		} else {
			if (index % 2 == 0) {
				panel.setBackground(Color.WHITE);
			} else {
				panel.setBackground(SwingTools.LIGHTEST_BLUE);
			}
		}
		String text = "";
		if (value instanceof PackageDescriptor) {
			PackageDescriptor desc = (PackageDescriptor) value;
			boolean selectedForInstallation = updateModel != null ? updateModel.isSelectedForInstallation(desc) : true;
			Icon packageIcon = getResizedIcon(getIcon(desc));
			
			text = "<html><body style='width: " + (packageIcon != null ? (310 - packageIcon.getIconWidth()) : 314) + ";" + 
					(packageIcon == null ? "margin-left:40px;" : "") + "'><strong>"+desc.getName()+"</strong> "+desc.getVersion()+"<br />";
			text += getFirstSentence(desc.getDescription()) +"<br />";
			ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
			boolean upToDate = false;
			if (desc.getPackageTypeName().equals("RAPIDMINER_PLUGIN")) {
				if (ext == null) {
					if (selectedForInstallation) {
						text += "<span style='color:#008800'>Marked for installation.</span>";
					} else {
						text += "<span style='color:#666666'>Not installed.</span>";
					}
					//text += "<span style='color:#666666'>Not installed.</span>";
				} else {
					String installed = ext.getLatestInstalledVersion();
					//String selectedVersion = desc.getVersion();
					if (installed != null) {
						upToDate = installed.compareTo(desc.getVersion()) >= 0;
						if (upToDate) {
							text += "<span style='color:#009900'>This package is up to date.</span>";
						} else {
							if (selectedForInstallation) {
								text += "<span style='color:#008800'>Installed version: " + ext.getSelectedVersion() + " (marked for update)</span>";
							} else {
								text += "<span style='color:#990000'>Installed version: " + ext.getLatestInstalledVersion() + "</span>";
							}
						}
					} else {
						if (selectedForInstallation) {
							text += "<span style='color:#008800'>Marked for installation.</span>";							
						} else {
							text += "<span style='color:#666666'>Not installed.</span>";
						}
					}
				}	
			} else if (desc.getPackageTypeName().equals("STAND_ALONE")) {
				String myVersion = RapidMiner.getLongVersion();
				upToDate = ManagedExtension.normalizeVersion(myVersion).compareTo(ManagedExtension.normalizeVersion(desc.getVersion())) >= 0;
				if (selectedForInstallation) {
					text += "Marked for updated.";
				} else if (upToDate) {
					text += "This package is up to date.";
				} else {
					text += "Installed version: " + myVersion;
				}
			}
			text += "</body></html>";
			
			label.setIcon(packageIcon);
			label.setVerticalTextPosition(SwingConstants.TOP);
			
			//selectedLabel.setIcon(selectedForInstallation ? SELECTED_ICON : NON_SELECTED_ICON);
			//freeCommercial.setIcon(UpdateManager.COMMERCIAL_LICENSE_NAME.equals(desc.getLicenseName()) ? COMMERCIAL_ICON : FREE_ICON);

			selectedLabel.setEnabled(!upToDate);
			
			if ("COMMERCIAL".equals(desc.getLicenseName())) {
				if (allPurchased || (updateModel != null && updateModel.isPurchased(desc))) {
					selectedLabel.setEnabled(true);
				} else {
					selectedLabel.setEnabled(false);
				}
			}
			label.setForeground(Color.BLACK);
		} else if (value instanceof String){
			text = (String) value;
		} else {
			text = value.toString();
		}
		label.setText(text);	
		return panel;
	}

}
