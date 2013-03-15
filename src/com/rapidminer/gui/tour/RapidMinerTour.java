/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
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

import com.rapidminer.gui.flow.ProcessPanel;
import com.rapidminer.gui.processeditor.NewOperatorEditor;
import com.rapidminer.gui.properties.OperatorPropertyPanel;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.gui.tour.AddBreakpointStep.Position;
import com.rapidminer.gui.tour.Step.BubbleTo;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.SimpleOperatorChain;
import com.rapidminer.operator.io.RepositorySource;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.tree.AbstractTreeLearner;
import com.rapidminer.repository.gui.RepositoryBrowser;

/**
 * A class that starts a beginner's tour for RapidMiner as soon as the <code>startTour()</code> method is called.
 * When the user completes an action, the next one is shown by a bubble.
 * 
 * @author Philipp Kersting and Thilo Kamradt
 *
 */
public class RapidMinerTour extends IntroductoryTour {

	
	public RapidMinerTour() {
		super(19, "RapidMiner");
	}

	protected void buildTour() {
		//create Steps which will be performed
		step[0] = new SimpleStep(Alignment.TOPLEFT, "start", "new", true);
		step[1] = new SimpleStep(Alignment.TOPLEFT, "tryrun", "run", true);
		step[2] = new AddOperatorStep(Alignment.BOTTOMLEFT, "adddatabase", RepositorySource.class, RepositoryBrowser.REPOSITORY_BROWSER_DOCK_KEY);
		step[3] = new AddOperatorStep(Alignment.LEFTBOTTOM, "dragdrop", AbstractTreeLearner.class, NewOperatorEditor.NEW_OPERATOR_DOCK_KEY);
		step[4] = new ChangeParameterStep(Alignment.RIGHTBOTTOM, "changeparam", AbstractTreeLearner.class, AbstractTreeLearner.PARAMETER_CRITERION, OperatorPropertyPanel.PROPERTY_EDITOR_DOCK_KEY, "information_gain");
		step[5] = new AddBreakpointStep(BubbleTo.OPERATOR, Alignment.RIGHTBOTTOM, "addbreakpoint", AbstractTreeLearner.class, Position.AFTER);
		step[6] = new SimpleStep(Alignment.TOPLEFT, "run", "run", true);
		step[7] = new ResumeFromBreakpointStep(Alignment.TOPLEFT, "goon",((Class<? extends Operator>) AbstractTreeLearner.class), Position.DONT_CARE);
		step[8] = new SaveProcessStep(Alignment.TOPLEFT, "saveas", "save_as");
		step[9] = new OpenProcessStep(Alignment.TOPLEFT, "open", "open");
		step[10] = new RemoveOperatorStep(BubbleTo.OPERATOR, Alignment.BOTTOMLEFT, "remove", AbstractTreeLearner.class);
		step[11] = new AddOperatorStep(Alignment.BOTTOMLEFT, "restore", AbstractLearner.class, NewOperatorEditor.NEW_OPERATOR_DOCK_KEY);
		step[12] = new AddBreakpointStep(BubbleTo.OPERATOR, Alignment.RIGHTBOTTOM, "restorebreakpoint", AbstractLearner.class, Position.BEFORE);
		step[13] = new RemoveBreakpointStep(BubbleTo.OPERATOR, Alignment.RIGHTBOTTOM, "removebreakpoint", AbstractLearner.class, Position.DONT_CARE);
		step[14] = new RenameOperatorStep(BubbleTo.OPERATOR, Alignment.RIGHTBOTTOM, "rename", AbstractLearner.class, "Tree");
		step[15] = new SaveProcessStep(Alignment.TOPLEFT, "save", "save");
		step[16] = new AddOperatorStep(Alignment.TOPLEFT, "subprocess", SimpleOperatorChain.class, ProcessPanel.PROCESS_PANEL_DOCK_KEY);
		step[17] = new OpenSubprocessStep(BubbleTo.OPERATOR, Alignment.LEFTBOTTOM, "opensubprocess", SimpleOperatorChain.class);
		step[18] = new AddOperatorStep(Alignment.TOPLEFT, "subprocesses", Operator.class, ProcessPanel.PROCESS_PANEL_DOCK_KEY, false);
	}
}
