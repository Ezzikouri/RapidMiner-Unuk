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

import javax.swing.SwingUtilities;

import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.BubbleListener;

/**
 * A step consisting of a {@link BubbleWindow} and a follower. This class must be inherited by other steps,
 * which define what action has to be perform to succeed to the next step.
 * 
 * @author Philipp Kersting
 *
 */
public abstract class Step {
	
	Step next;
	BubbleWindow bubble;
	protected String tourkey = "";
	protected boolean finalStep = false;
	
	abstract BubbleWindow createBubble();
	
	public Step getNext(){
		return this.next;
	}
	
	public void setNext(Step next){
		this.next = next;
	}
	
	public void start(){
		bubble = createBubble();
		bubble.addBubbleListener(new BubbleListener() {
			
			@Override
			public void bubbleClosed(BubbleWindow bw) {
				bw.removeBubbleListener(this);
				stepCanceled();
				if(Step.this.isFinal()){
					Step.this.writeCompleteToFile();
				}
			}
			
			@Override
			public void actionPerformed(BubbleWindow bw) {
				if (next!=null){
					new Thread() {
						public void run() {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									next.start();
									if(next.isFinal()) {
										next.writeCompleteToFile();
									}
								}
							});
						};
					}.start();
				}
				bw.removeBubbleListener(this);
			}
		});
		bubble.setVisible(true);
	}
	
	public void addBubbleListener(BubbleListener l){
		bubble.addBubbleListener(l);
	}
	public void removeBubbleListener(BubbleListener l){
		bubble.removeBubbleListener(l);
	}
	
	public void setTourKey(String key) {
		tourkey=key;
	}
	
	public void setIsFinalStep(boolean isFinal) {
		finalStep = isFinal;
	}
	
	public boolean isFinal() {
		return finalStep;
	}

	public void writeCompleteToFile() {
		TourManager tm = TourManager.getInstance();
		tm.setTourState(tourkey, TourState.COMPLETED);
	}
	
	/** Can be overridden by subclasses. */
	protected void stepCanceled() {}
}
