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

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.flow.ProcessPanel;
import com.rapidminer.gui.processeditor.NewOperatorEditor;
import com.rapidminer.gui.properties.OperatorPropertyPanel;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.gui.tour.AddBreakpointStep.Position;
import com.rapidminer.gui.tour.AddOperatorStep.AddOperatorStepListener;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.SimpleOperatorChain;
import com.rapidminer.operator.io.RepositorySource;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.tree.AbstractTreeLearner;
import com.rapidminer.repository.gui.RepositoryBrowser;

/**
 * A class that starts a beginner's tour for RapidMiner as soon as the <code>start()</code> method is called.
 * When the user completes an action, the next one is shown by a bubble.
 * 
 * @author Philipp Kersting
 *
 */
public class RapidMinerTour extends IntroductoryTour {

	public RapidMinerTour() {
		super(19, "RapidMiner");

		//create Sights
		sights[0] = new SimpleStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "start", "new");
		sights[1] = new SimpleStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "tryrun", "run");
		sights[2] = new AddOperatorStep(Alignment.BOTTOMLEFT, RapidMinerGUI.getMainFrame(), "adddatabase", RepositorySource.class, RepositoryBrowser.REPOSITORY_BROWSER_DOCK_KEY);
		sights[3] = new AddOperatorStep(Alignment.LEFTBOTTOM, RapidMinerGUI.getMainFrame(), "dragdrop", AbstractTreeLearner.class, NewOperatorEditor.NEW_OPERATOR_DOCK_KEY);
		sights[4] = new ChangeParameterStep(Alignment.RIGHTBOTTOM, RapidMinerGUI.getMainFrame(), "changeparam", null, AbstractTreeLearner.PARAMETER_CRITERION, OperatorPropertyPanel.PROPERTY_EDITOR_DOCK_KEY, "information_gain");
		sights[5] = new AddBreakpointStep(Alignment.RIGHTBOTTOM, RapidMinerGUI.getMainFrame(), "addbreakpoint", null, Position.AFTER);
		sights[6] = new SimpleStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "run", "run");
		sights[7] = new ResumeFromBreakpointStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "goon", null, "run");
		sights[8] = new SaveProcessStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "saveas", "save_as");
		sights[9] = new OpenProcessStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "open","open");
		sights[10] = new RemoveOperatorStep(Alignment.BOTTOMLEFT, RapidMinerGUI.getMainFrame(), "remove", null, RapidMinerGUI.getMainFrame().getProcessPanel().getComponent());
		sights[11] = new AddOperatorStep(Alignment.BOTTOMLEFT, RapidMinerGUI.getMainFrame(), "restore", AbstractLearner.class, NewOperatorEditor.NEW_OPERATOR_DOCK_KEY);
		sights[12] = new AddBreakpointStep(Alignment.RIGHTBOTTOM, RapidMinerGUI.getMainFrame(), "restorebreakpoint", AbstractLearner.class, Position.BEFORE);
		sights[13] = new RemoveBreakpointStep(Alignment.RIGHTBOTTOM, RapidMinerGUI.getMainFrame(), "removebreakpoint", AbstractLearner.class);
		sights[14] = new RenameOperatorStep(Alignment.RIGHTBOTTOM, RapidMinerGUI.getMainFrame(), "rename", AbstractLearner.class, "Tree", "rename_in_processrenderer");
		sights[15] = new SaveProcessStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "save", "save");
		sights[16] = new AddOperatorStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "subprocess", SimpleOperatorChain.class, ProcessPanel.PROCESS_PANEL_DOCK_KEY);
		sights[17] = new OpenSubprocessStep(Alignment.LEFTBOTTOM, RapidMinerGUI.getMainFrame(), "opensubprocess", RapidMinerGUI.getMainFrame().getProcessPanel(), SimpleOperatorChain.class);
		sights[18] = new AddOperatorStep(Alignment.TOPLEFT, RapidMinerGUI.getMainFrame(), "subprocesses", Operator.class, ProcessPanel.PROCESS_PANEL_DOCK_KEY);
		//add needed listeners
		AddOperatorStepListener operatorListener = new AddOperatorStepListener() {

			@Override
			public void operatorAvailable(Operator op) {
				((OperatorStep) sights[4]).setOperator(op.getClass());
				((OperatorStep) sights[5]).setOperator(op.getClass());
				((OperatorStep) sights[7]).setOperator(op.getClass());
				((OperatorStep) sights[10]).setOperator(op.getClass());
				for (ExecutionUnit e : RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getSubprocesses()) {
					System.out.println(e.getEnclosingOperator().equals(RapidMinerGUI.getMainFrame().getProcess().getRootOperator()));
				}
				((AddOperatorStep) sights[3]).removeListener(this);
			}
		};
		((AddOperatorStep) sights[16]).addListener(new AddOperatorStepListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void operatorAvailable(Operator op) {
				if (op instanceof OperatorChain) {
					((AddOperatorStep) sights[18]).setTargetEnclosingOperatorChain((Class<? extends OperatorChain>) op.getClass());
				}

			}
		});
		((AddOperatorStep) sights[3]).addListener(operatorListener);

		//placeFollower
		super.placeFollowers();
	}
}
