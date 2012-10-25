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

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.rapidminer.RapidMiner;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.plugin.Dependency;


/**
 * @author Simon Fischer, Dominik Halfkann
 */
public class UpdatePackagesModel extends Observable {

	private final List<PackageDescriptor> descriptors;
	
	private final Map<PackageDescriptor, Boolean> selectionMap = new HashMap<PackageDescriptor, Boolean>();
	
	private final Map<PackageDescriptor, List<Dependency>> dependencyMap = new HashMap<PackageDescriptor, List<Dependency>>();
	
	/** Read the comment of {@link #isPurchased(PackageDescriptor)}. */
	private Set<String> purchasedPackages = new HashSet<String>();
	
	public UpdatePackagesModel(List<PackageDescriptor> descriptors, UpdateServerAccount usAccount) {
		this.descriptors = descriptors;
		
		usAccount.addObserver(new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				try {
					purchasedPackages = new HashSet<String>(UpdateManager.getAccountService().getLicensedProducts());
				} catch (Exception e1) {
					purchasedPackages = new HashSet<String>();
				}
			}
		});
	}
	
	public void setSelectedForInstallation(PackageDescriptor desc, boolean selected) {
		selectionMap.put(desc, true);
	}
	
	public void forceNotifyObservers() {
		setChanged();
		notifyObservers();
	}
	
	public void toggleSelesctionForInstallation(PackageDescriptor desc) {
		if (desc != null) {
			boolean select = !isSelectedForInstallation(desc);
			if (isUpToDate(desc)) {
				select = false;
			}
			if (desc.getPackageTypeName().equals("RAPIDMINER_PLUGIN")) {
				if (select) {
					resolveDependencies(desc);
				}
			} else if (desc.getPackageTypeName().equals("STAND_ALONE")) {
				String longVersion = RapidMiner.getLongVersion();
				String myVersion = ManagedExtension.normalizeVersion(longVersion);
				String remoteVersion = ManagedExtension.normalizeVersion(desc.getVersion());
				if ((myVersion != null) && (remoteVersion.compareTo(myVersion) <= 0)) {
					select = false;
				}
			}
			if (UpdateManager.COMMERCIAL_LICENSE_NAME.equals(desc.getLicenseName()) && !isPurchased(desc)) {
				select = false;
				SwingTools.showMessageDialog("purchase_package", desc.getName());
			}
			selectionMap.put(desc, select);
			this.setChanged();
			this.notifyObservers(desc);
		}
	}
	
	public boolean isSelectedForInstallation(PackageDescriptor desc) {
		Boolean selected = selectionMap.get(desc);
		return (selected != null) && selected.booleanValue();
	}
	
	public List<PackageDescriptor> getInstallationList() {
		List<PackageDescriptor> downloadList = new LinkedList<PackageDescriptor>();
		for (Entry<PackageDescriptor, Boolean> entry : selectionMap.entrySet()) {
			if (entry.getValue()) {
				downloadList.add(entry.getKey());
			}
		}
		return downloadList;
	}
	
	public void setDependencies(PackageDescriptor desc, List<Dependency> dependencies) {

	}
	
	private boolean isUpToDate(PackageDescriptor desc) {
		ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
		if (ext != null) {
			String remoteVersion = ManagedExtension.normalizeVersion(desc.getVersion());
			String myVersion = ManagedExtension.normalizeVersion(ext.getLatestInstalledVersion());
			if ((myVersion != null) && (remoteVersion.compareTo(myVersion) <= 0)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	private void resolveDependencies(PackageDescriptor desc) {
		List<Dependency> deps = dependencyMap.get(desc);
		if (deps != null) {
			for (Dependency dep : deps) {
				for (PackageDescriptor other : descriptors) {
					if (other.getPackageId().equals(dep.getPluginExtensionId())) {
						Boolean selected = selectionMap.get(other);
						boolean selectedB = (selected != null) && selected.booleanValue();
						if (!selectedB && !isUpToDate(other)) {
							selectionMap.put(other, true);
							resolveDependencies(other);
						}
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Currently, this is an unused feature. There are no extensions that can be purchased. Don't be afraid, RapidMiner
	 * is, and will always be, open source and free. However, future extensions like connectors to SAP or other data
	 * sources requiring proprietary drivers with expensive license fees may only be available on a commercial basis,
	 * for obvious reasons :-)
	 */
	public boolean isPurchased(PackageDescriptor desc) {
		return purchasedPackages.contains(desc.getPackageId());
	}
	
	
	public String toString(PackageDescriptor descriptor) {
		StringBuilder b = new StringBuilder("<html><body>");
		b.append("<span style=\"font-size:14px;\">");
		if (descriptor.isRestricted()) {
			b.append("<img src=\"icon:///").append("16/currency_euro.png").append("\"/>&nbsp;");
		}
		b.append(descriptor.getName()).append("</span>");
		Date date = new Date(descriptor.getCreationTime().toGregorianCalendar().getTimeInMillis());
		b.append("<hr style=\"margin-bottom:8px;\"/><p style=\"margin-bottom:8px;\"><strong>Version ").append(descriptor.getVersion()).append(", released ").append(Tools.formatDate(date));
		b.append(", ").append(Tools.formatBytes(descriptor.getSize())).append("</strong></p>");
		if ((descriptor.getDependencies() != null) && !descriptor.getDependencies().isEmpty()) {
			b.append("<div style=\"margin-bottom:8px;\">Depends on: " + descriptor.getDependencies() + "</div>");
		}
		b.append("<div style=\"margin-bottom:8px;\">").append(descriptor.getLongDescription()).append("</div>");
		// Before you are shocked, read the comment of isPurchased() :-)
		if (UpdateManager.COMMERCIAL_LICENSE_NAME.equals(descriptor.getLicenseName())) {
			if (isPurchased(descriptor)) {
				b.append("<p>You have purchased this package. However, you cannot install this extension with this version of RapidMiner. Please upgrade first.</p>");
			} else {
				try {
					b.append("<p><a style=\"color:blue;text-decoration:underline;\"href=" + UpdateManager.getUpdateServerURI("/shop/" + descriptor.getPackageId()).toString() + ">Order this extension.</a></p><p>You cannot install this extension with this pre-release of RapidMiner. Please upgrade first.</p>");
				} catch (URISyntaxException e) {}
			}
		}
		b.append("<p><a href=\"" + UpdateManager.getBaseUrl() + "/faces/product_details.xhtml?productId=" + descriptor.getPackageId() + "\">Extension homepage</a></p>");
		b.append("</body></html>");
		return b.toString();
	}
	
	
}
