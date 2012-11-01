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

/**
 * @author Thilo Kamradt
 *
 */
public abstract class IntroductoryTour {

	final int maxSteps;

	protected Step[] sights;
	
	protected int startSight;

	public IntroductoryTour(int max, String tourName) {
		this.maxSteps = max+1;
		sights = new Step[maxSteps];
		sights[max] = new FinalStep("complete_Tour", tourName);
	}

	public void startTour(int startPoint){
		if (startPoint <= maxSteps) {
			sights[startPoint].start();
		} else {
			sights[0].start();
		}
	}
	
	/**
	 * method to connect the single Steps
	 */
	protected void placeFollower() {
		for(int i = 0;i<(sights.length-1);i++){
			sights[i].setNext(sights[i+1]);
		}
		
	}
}
