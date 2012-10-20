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
public class RapidMinerTour {
	
	public static void startTour(){
			Step step = new SimpleStep (Alignment.TOP, RapidMinerGUI.getMainFrame(), "start", "new");
			Step step1 = new SimpleStep (Alignment.TOP, RapidMinerGUI.getMainFrame(), "tryrun", "run");
			Step step2 = new AddOperatorStep(Alignment.BOTTOM, RapidMinerGUI.getMainFrame(), "adddatabase", RepositorySource.class, RepositoryBrowser.REPOSITORY_BROWSER_DOCK_KEY);
			final AddOperatorStep step3 = new AddOperatorStep(Alignment.LEFT, RapidMinerGUI.getMainFrame(), "dragdrop", AbstractTreeLearner.class, NewOperatorEditor.NEW_OPERATOR_DOCK_KEY);
			final OperatorStep step4 = new ChangeParameterStep(Alignment.RIGHT, RapidMinerGUI.getMainFrame(), "changeparam",null, AbstractTreeLearner.PARAMETER_CRITERION, OperatorPropertyPanel.PROPERTY_EDITOR_DOCK_KEY,  "information_gain");
			final OperatorStep step5 = new AddBreakpointStep(Alignment.RIGHT, RapidMinerGUI.getMainFrame(),"addbreakpoint", null, Position.AFTER);
			Step step6 = new SimpleStep (Alignment.TOP, RapidMinerGUI.getMainFrame(), "run", "run");
			final OperatorStep step7 = new ResumeFromBreakpointStep(Alignment.TOP, RapidMinerGUI.getMainFrame(), "goon", null, "run") ;
			Step step8 = new SaveProcessStep(Alignment.TOP, RapidMinerGUI.getMainFrame(), "saveas", "save_as");
			Step step8b = new OpenProcessStep(Alignment.TOP, RapidMinerGUI.getMainFrame(), "open");
			final OperatorStep step9 = new RemoveOperatorStep(Alignment.BOTTOM, RapidMinerGUI.getMainFrame(), "remove", null, RapidMinerGUI.getMainFrame().getProcessPanel().getComponent());
			AddOperatorStep step10 = new AddOperatorStep(Alignment.BOTTOM, RapidMinerGUI.getMainFrame(), "restore", AbstractLearner.class, NewOperatorEditor.NEW_OPERATOR_DOCK_KEY);
			OperatorStep step11 = new AddBreakpointStep(Alignment.RIGHT, RapidMinerGUI.getMainFrame(), "restorebreakpoint", AbstractLearner.class, Position.BEFORE);
			OperatorStep step12 = new RemoveBreakpointStep(Alignment.RIGHT, RapidMinerGUI.getMainFrame(), "removebreakpoint", AbstractLearner.class);
			OperatorStep step13 = new RenameOperatorStep(Alignment.RIGHT, RapidMinerGUI.getMainFrame(), "rename", AbstractLearner.class, "Tree", "rename_in_processrenderer");
			Step step14 = new SaveProcessStep(Alignment.TOP, RapidMinerGUI.getMainFrame(), "save", "save");
			AddOperatorStep step15 = new AddOperatorStep(Alignment.TOP, RapidMinerGUI.getMainFrame(), "subprocess", SimpleOperatorChain.class, ProcessPanel.PROCESS_PANEL_DOCK_KEY);
			Step step16 = new OpenSubprocessStep(Alignment.LEFT, RapidMinerGUI.getMainFrame(), "opensubprocess", RapidMinerGUI.getMainFrame().getProcessPanel(), SimpleOperatorChain.class);
			final AddOperatorStep step17 = new AddOperatorStep(Alignment.TOP, RapidMinerGUI.getMainFrame(), "subprocesses", Operator.class, ProcessPanel.PROCESS_PANEL_DOCK_KEY); 
			AddOperatorStepListener operatorListener = new AddOperatorStepListener() {
				
				@Override
				public void operatorAvailable(Operator op) {
					step4.setOperator(op.getClass());
					step5.setOperator(op.getClass());
					step7.setOperator(op.getClass());
					step9.setOperator(op.getClass());
					for (ExecutionUnit e : RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getSubprocesses()){
						System.out.println(e.getEnclosingOperator().equals(RapidMinerGUI.getMainFrame().getProcess().getRootOperator()));
						
					}
					step3.removeListener(this);
				}
			};
			step15.addListener(new AddOperatorStepListener() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void operatorAvailable(Operator op) {
					if (op instanceof OperatorChain){
						step17.setTargetEnclosingOperatorChain((Class<? extends OperatorChain>) op.getClass());
					}
					
					
				}
			});
			step3.addListener(operatorListener);
			
			step.setNext(step1);
			step1.setNext(step2);
			step2.setNext(step3);
			step3.setNext(step4);
			step4.setNext(step5);
			step5.setNext(step6);
			step6.setNext(step7);
			step7.setNext(step8);
			step8.setNext(step9);
			step8b.setNext(step9);
			step9.setNext(step10);
			step10.setNext(step11);
			step11.setNext(step12);
			step12.setNext(step13);
			step13.setNext(step14);
			step14.setNext(step15);
			step15.setNext(step16);
			step16.setNext(step17);
			
			
			step.start();
	}
}
