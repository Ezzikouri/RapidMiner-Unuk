/*
 * 
 */
package com.rapidminer.gui;

/**
 * This interface contains the tutorial related actions.
 * 
 * @author GĂˇbor Bakos
 */
public interface TutorialState {

	public void startTutorial();

	public void setTutorialMode(boolean mode);

	public boolean isTutorialMode();

}