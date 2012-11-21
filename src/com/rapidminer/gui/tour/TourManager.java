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

package com.rapidminer.gui.tour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.rapidminer.tools.FileSystemService;

/**
 * 
 * @author Thilo Kamradt
 */
public class TourManager {

	private static String TOUR_PROPERTIES = "tours.properties";

	private static TourManager INSTANCE;

	private Properties properties;

	private HashMap<String, Class<? extends IntroductoryTour>> tours;
	private ArrayList<String> indexList;

	private TourManager() {
		load();
		tours = new HashMap<String, Class<? extends IntroductoryTour>>();
		indexList = new ArrayList<String>();
		tours.put("RapidMiner", RapidMinerTour.class);
		indexList.add("RapidMiner");
	}

	public static synchronized TourManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TourManager();
		}
		return INSTANCE;
	}

	private void load() {
		this.properties = new Properties();
		File file = FileSystemService.getUserConfigFile(TOUR_PROPERTIES);
		try {
			file.createNewFile();
			properties.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void save() {
		File file = FileSystemService.getUserConfigFile(TOUR_PROPERTIES);
		try {
			properties.store(new FileOutputStream(file), "RapidMiner Datafiles");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setTourState(String tourKey, TourState state) {
		properties.setProperty(tourKey, state.toString());
		save();
	}

	public TourState getTourState(String tourKey) {
		String stateKey = properties.getProperty(tourKey);
		if (stateKey == null) {
			setTourState(tourKey, TourState.NOT_COMPLETED);
			return TourState.NOT_COMPLETED;
		} else {
			return TourState.valueOf(stateKey);
		}
	}

	public void registerTour(String tourKey, Class<? extends IntroductoryTour> tourClass) {
		tours.put(tourKey, tourClass);
		indexList.add(tourKey);
	}

	public String[] getTourkeys() {
		return tours.keySet().toArray(new String[] {});
	}

	public Set<Entry<String, Class<? extends IntroductoryTour>>> getEntries() {
		return tours.entrySet();
	}

	public int size() {
		return tours.size();
	}

	public IntroductoryTour get(int index) {
		IntroductoryTour tour = null;
		try {
			tour = tours.get(indexList.get(index)).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tour;
	}

	public void startTour(String tourKey) {
		Class<? extends IntroductoryTour> tourClass = tours.get(tourKey);
		try {
			tourClass.newInstance().startTour();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
