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



/** A tour consisting of multiple {@link Step}s explaining the usage of RapidMiner
 *  or an Extension.
 *  
 *   Implementations of tour must implement a default (no-arg) constructor since
 *   they are created reflectively.
 * 
 * 
 * @author Thilo Kamradt
 *
 */
public abstract class IntroductoryTour {

	private int maxSteps;

	protected Step[] sights;

	protected int startSight;

	protected String tourKey;
	
	protected boolean completeWindow;
	
	public IntroductoryTour(int max, String tourName) {
		this(max, tourName, true);
	}

	public IntroductoryTour(int max, String tourName, boolean addComppleteWindow) {
		this.tourKey = tourName;
		this.maxSteps = max;
		this.completeWindow = addComppleteWindow;
	}

	protected void init() {
		if (completeWindow) {
			this.maxSteps = maxSteps + 1;
			sights = new Step[maxSteps];
			sights[maxSteps-1] = new FinalStep("complete_Tour", tourKey);
		} else {
			sights = new Step[maxSteps];
		}
	}
	
	public void startTour() {
		init();
		buildTour();
		placeFollowers();
		sights[0].start();
	}
	
	
	protected abstract void buildTour();
	
	/**
	 * method to get the key of the tour
	 * @return String with key of the tour
	 */
	public String getKey() {
		return tourKey;
	}
	
	/**
	 * method to connect the single Steps
	 */
	protected void placeFollowers() {
		for (int i = 0; i < (sights.length - 1); i++) {
			sights[i].setNext(sights[i + 1]);
		}
		sights[maxSteps - 1].setIsFinalStep(true);
		sights[maxSteps - 1].setTourKey(tourKey);

	}
}
