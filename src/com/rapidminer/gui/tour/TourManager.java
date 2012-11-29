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
		if (state == TourState.NEVER_ASK) {
			properties.setProperty(tourKey + ".ask", state.toString());
		} else {
			properties.setProperty(tourKey, state.toString());
		}
		save();
	}

	public void setTourProgress(String tourKey, int step) {
		String stateBefore = properties.getProperty(tourKey + ".progress");
		if (stateBefore == null || Integer.parseInt(stateBefore) < step)
			properties.setProperty(tourKey + ".progress", "" + step);
		save();
	}

	public TourState getTourState(String tourKey) {
		String stateKey = properties.getProperty(tourKey);
		if (stateKey == null) {
			setTourState(tourKey, TourState.NEW_ONE);
			return TourState.NEW_ONE;
		} else {
			return TourState.valueOf(stateKey);
		}
	}

	public boolean getAskState(String tourKey) {
		if (properties.getProperty(tourKey + ".ask", null) == null) {
			return true;
		} else {
			return false;
		}
	}

	public int getProgress(String tourKey) {
		String state = properties.getProperty(tourKey + ".progress");
		if (state == null) {
			setTourProgress(tourKey, 0);
			return 0;
		} else {
			return Integer.parseInt(state);
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

	public IntroductoryTour get(String tourKey) {
		IntroductoryTour tour = null;
		Class<? extends IntroductoryTour> tourClass = tours.get(tourKey);
		try {
			tour = tourClass.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tour;
	}

	public IntroductoryTour startTour(String tourKey) {
		IntroductoryTour tour = null;
		Class<? extends IntroductoryTour> tourClass = tours.get(tourKey);
		try {
			tour = tourClass.newInstance();
			tour.startTour();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tour;
	}

}
