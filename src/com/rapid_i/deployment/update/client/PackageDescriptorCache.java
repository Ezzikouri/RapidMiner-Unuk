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

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.deployment.client.wsimport.UpdateService;
import com.rapidminer.gui.tools.SwingTools;

/**
 * Cache for PackageDescriptor which is used by the Rapidminer Update Dialog.
 * If the needed information is not in the cache, it is fetched though the {@link UpdateService}.
 * @author Dominik Halfkann
 *
 */
public class PackageDescriptorCache {

	private final Map<String, PackageDescriptor> descriptors = new HashMap<String, PackageDescriptor>();

	private UpdateService updateService = null;

	public PackageDescriptor getPackageInfo(String packageId, String targetPlatform) {
		if (descriptors.containsKey(packageId)) {
			//in Cache
			return descriptors.get(packageId);
		} else {
			//need to fetch Info
			fetchPackageInfo(packageId, targetPlatform);
			if (descriptors.containsKey(packageId)) {
				return descriptors.get(packageId);
			} else {
				return null;
			}
		}
	}
	
	public void fetchPackageInfo(String packageId, String targetPlatform) {
		initUpdateService();
		if (updateService != null) {
			try {
				PackageDescriptor descriptor = updateService.getPackageInfo(packageId, updateService.getLatestVersion(packageId, targetPlatform), targetPlatform);
				descriptors.put(packageId, descriptor);
			} catch (Exception e) {
				SwingTools.showSimpleErrorMessage("failed_update_server", e, UpdateManager.getBaseUrl());
			}
		}
	}

	private void initUpdateService() {
		if (updateService == null) {
			try {
				updateService = UpdateManager.getService();
			} catch (Exception e) {
				SwingTools.showSimpleErrorMessage("failed_update_server", e, UpdateManager.getBaseUrl());
			}
		}
	}
	
	public UpdateService getUpdateService() {
		initUpdateService();
		return updateService;
	}

}
