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

package com.rapidminer.tools.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.tools.ProgressListener;

/**
 * This class caches the table meta data so the DB is not queried all the time.
 * 
 * @author Marco Boeck
 *
 */
public class TableMetaDataCache {

	private Map<String, Map<TableName, List<ColumnIdentifier>>> tableMap;

	/** time of the last cache update */
	private Map<String, Long> lastQueryTimeMap;

	private boolean refreshCacheAfterInterval;

	private Object lock;

	/** time before the cache is refreshed again (in ms) */
	private static final int CACHE_REFRESH_INTERVAL = 60000;

	/** if true, will refresh cache if set amount of times expired since last cache update; if false will never update cache automatically */

	/** the instance of this class */
	private static TableMetaDataCache instance;

	/**
	 * Creates a new {@link TableMetaDataCache} instance.
	 * @param refreshCacheAfterInterval
	 */
	private TableMetaDataCache(boolean refreshCacheAfterInterval) {
		this.lastQueryTimeMap = new HashMap<String, Long>();
		this.tableMap = new HashMap<String, Map<TableName, List<ColumnIdentifier>>>();
		this.lock = new Object();
		this.refreshCacheAfterInterval = refreshCacheAfterInterval;
	}

	/**
	 * Get the instance of {@link TableMetaDataCache}.
	 * @return
	 */
	public static synchronized TableMetaDataCache getInstance() {
		if (instance == null) {
			instance = new TableMetaDataCache(false);
		}

		return instance;
	}

	/**
	 * Fetches meta data about all tables and, if selected, all columns in the database.
	 * The returned map maps table names to column descriptions.
	 * If fetchColumns is false, all lists in the returned map will be empty lists, so basically
	 * only the key set contains useful information.
	 * 
	 * This method is cached, so the data might not be up to date before the cache is refreshed.
	 * @param connectionName
	 * @param handler
	 * @param progressListener
	 * @param minProgress
	 * @param maxProgress
	 * @param fetchColumns
	 * @return
	 * @throws SQLException
	 */
	public Map<TableName, List<ColumnIdentifier>> getAllTableMetaData(String connectionName, DatabaseHandler handler,
			ProgressListener progressListener, int minProgress, int maxProgress, boolean fetchColumns)
			throws SQLException {
		synchronized (lock) {
			if (this.tableMap.get(connectionName) == null
					|| (refreshCacheAfterInterval && (System.currentTimeMillis() - this.lastQueryTimeMap
							.get(connectionName)) > CACHE_REFRESH_INTERVAL)) {
				updateCache(connectionName, handler, progressListener, minProgress, maxProgress, fetchColumns);
			}

			progressListener.setCompleted(maxProgress);
			return this.tableMap.get(connectionName);
		}
	}

	/**
	 * Fetches meta data about all tables and, if selected, all columns in the database.
	 * The returned map maps table names to column descriptions.
	 * If fetchColumns is false, all lists in the returned map will be empty lists, so basically
	 * only the key set contains useful information.
	 * 
	 * This method is cached, so the data might not be up to date before the cache is refreshed.
	 * @param connectionName
	 * @param handler
	 * @return
	 * @throws SQLException
	 */
	public Map<TableName, List<ColumnIdentifier>> getAllTableMetaData(String connectionName, DatabaseHandler handler)
			throws SQLException {
		synchronized (lock) {
			if (this.tableMap.get(connectionName) == null
					|| (refreshCacheAfterInterval && (System.currentTimeMillis() - this.lastQueryTimeMap
							.get(connectionName)) > CACHE_REFRESH_INTERVAL)) {
				updateCache(connectionName, handler);
			}

			return this.tableMap.get(connectionName);
		}
	}

	/**
	 * Clears the whole cache.
	 */
	public void clearCache() {
		synchronized (lock) {
			this.tableMap.clear();
		}
	}

	/**
	 * Updates the cache.
	 * @throws SQLException
	 */
	private void updateCache(String connectionName, DatabaseHandler handler, ProgressListener progressListener,
			int minProgress, int maxProgress, boolean fetchColumns) throws SQLException {
		this.tableMap.put(connectionName,
				handler.getAllTableMetaData(progressListener, minProgress, maxProgress, fetchColumns));
		this.lastQueryTimeMap.put(connectionName, new Long(System.currentTimeMillis()));
	}

	/**
	 * Updates the cache without a {@link ProgressListener}.
	 * @throws SQLException
	 */
	private void updateCache(String connectionName, DatabaseHandler handler) throws SQLException {
		this.tableMap.put(connectionName, handler.getAllTableMetaData());
		this.lastQueryTimeMap.put(connectionName, new Long(System.currentTimeMillis()));
	}

}
