/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.I18N;

/**
 * @author Venkatesh Umaashankar
 *
 */
public class PackageListCellRenderer implements ListCellRenderer {

	private static RenderingHints HI_QUALITY_HINTS = new RenderingHints(null);

	static {
		HI_QUALITY_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		HI_QUALITY_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		HI_QUALITY_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	private static String MARKED_FOR_INSTALL_COLOR = "#0066CC";
	private static String MARKED_FOR_UPDATE__COLOR = "#3399FF";

	private Map<String, Icon> icons = new HashMap<String, Icon>();
	private double iconScalingFactor;
	private int textPixelSize=0;

	public PackageListCellRenderer(double iconScalingFactor,int textPixelSize) {
		this.iconScalingFactor=iconScalingFactor;
		this.textPixelSize = textPixelSize;
	}
	
	public PackageListCellRenderer() {
		this(45,0);		
	}

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

	private Icon getResizedIcon(Icon originalIcon,double scalingFactor) {
		if (originalIcon == null)
			return null;
		int width = originalIcon.getIconWidth();
		int height = originalIcon.getIconHeight();
		if (width != scalingFactor) {
			double scale = (scalingFactor / width);
			BufferedImage bi = new BufferedImage(
					(int) (scale * width),
					(int) (scale * height),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = bi.createGraphics();
			g.setRenderingHints(HI_QUALITY_HINTS);
			g.scale(scale, scale);
			originalIcon.paintIcon(null, g, 0, 0);
			g.dispose();
			return new ImageIcon(bi);
		} else {
			return originalIcon;
		}
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		JPanel panel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			@Transient
			/*
			 * Overriding this method causes the correct computation
			 * of the width with no overlapping if the scrollbar
			 * is displayed.
			 */
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				if (d == null) {
					return d;
				}
				d.width = 10;
				return d;
			}
		};

		JLabel label = new JLabel();

		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		panel.add(label);

		panel.setOpaque(true);

		if (isSelected && (value instanceof PackageDescriptor)) {
			panel.setBackground(SwingTools.DARKEST_BLUE);
			panel.setBorder(BorderFactory.createLineBorder(Color.black));
		} else {
			if (index % 2 == 0) {
				panel.setBackground(Color.WHITE);
				panel.setBorder(BorderFactory.createLineBorder(Color.WHITE));

			} else {
				panel.setBackground(SwingTools.LIGHTEST_BLUE);
				panel.setBorder(BorderFactory.createLineBorder(SwingTools.LIGHTEST_BLUE));
			}
		}

		String text = "";
		if (value instanceof PackageDescriptor) {
			PackageDescriptor desc = (PackageDescriptor) value;

			Icon packageIcon = getResizedIcon(getIcon(desc),iconScalingFactor);

			text = "<html><body style='width: " + (packageIcon != null ? (300 - packageIcon.getIconWidth()) : 314) + ((textPixelSize>0)?";font-size:":"")+textPixelSize+"px;" +
					(packageIcon == null ? "margin-left:40px;" : "") + "'>";

			// add name and version
			text += "<div><strong>" + desc.getName() + "</strong> " + desc.getVersion();

			if (desc.isRestricted()) {
				text += "&nbsp;&nbsp;<img src='icon:///16/currency_euro.png' style='vertical-align:middle;'/>";
			}

			text += "</div>";
			text += "<div style='margin-top:5px;'>" + getLicenseType(desc.getLicenseName()) + "</div>";
			ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
			if (desc.getPackageTypeName().equals("RAPIDMINER_PLUGIN")) {

				text += "</body></html>";

				label.setIcon(packageIcon);
				label.setVerticalTextPosition(SwingConstants.TOP);
				label.setForeground(Color.BLACK);
			} else {
				text = "<html><div style=\"width:250px;\">" + value.toString() + "</div></html>";
			}
			label.setText(text);

			
		}
		
		return panel;
	}

	private String getLicenseType(String licenseName) {
		return "<b>License Type:</b> "+licenseName;  //TODO I18N
	}

	private String getMarkedForInstallationHtml() {
		return "<div style='" + getActionStyle(MARKED_FOR_INSTALL_COLOR) + "'><img src='icon:///16/nav_down_blue.png'/>&nbsp;" + I18N.getGUILabel("marked.for.installation") + "</div>";
	}

	private String getMarkedForUpdateHtml() {
		return "<div style='" + getActionStyle(MARKED_FOR_UPDATE__COLOR) + "'><img src=\"icon:///16/nav_refresh_blue.png\"/>&nbsp;" + I18N.getGUILabel("marked.for.update") + "</div>";
	}

	private String getActionStyle(String color) {
		return "height:18px;min-height:18px;line-height:18px;vertical-align:middle;color:" + color + ";margin-top:3px;";
	}

}
