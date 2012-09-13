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
package com.rapid_i.deployment.update.client.listmodels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapid_i.deployment.update.client.ManagedExtension;
import com.rapid_i.deployment.update.client.PackageDescriptorCache;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;


/**
 * 
 * @author Dominik Halfkann
 *
 */
public class UpdatesPackageListModel extends AbstractPackageListModel {

	private static final long serialVersionUID = 1L;

	public UpdatesPackageListModel(PackageDescriptorCache cache) {
		super(cache);
	}
	
	@Override
	public List<String> fetchPackageNames() {
		List<String> packageNames = new ArrayList<String>();
		for (ManagedExtension me : ManagedExtension.getAll()) {
			packageNames.add(me.getPackageId());
		}
		return packageNames;
	}

	@Override
	public void modifyPackageList() {
		
		Iterator<String> i = packageNames.iterator();
		while(i.hasNext()) {
			String packageName = i.next();
			PackageDescriptor desc = cache.getPackageInfo(packageName, "ANY");
			ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
			String installed = ext.getLatestInstalledVersion();
			if (installed != null) {
				boolean upToDate = installed.compareTo(desc.getVersion()) >= 0;
				if (upToDate) {
					i.remove();
				}
			}
		}
	}

}
