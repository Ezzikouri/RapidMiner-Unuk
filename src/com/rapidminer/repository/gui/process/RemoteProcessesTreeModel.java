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
package com.rapidminer.repository.gui.process;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.datatype.XMLGregorianCalendar;

import com.rapid_i.repository.wsimport.ProcessResponse;
import com.rapid_i.repository.wsimport.ProcessStackTrace;
import com.rapid_i.repository.wsimport.ProcessStackTraceElement;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.repository.RemoteProcessState;
import com.rapidminer.repository.remote.ProcessServiceFacade;
import com.rapidminer.repository.remote.RemoteRepository;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;

/**
 * The TreeModel for the log of remotely executed processes.
 * 
 * @author Simon Fischer, Nils Woehler
 */
public class RemoteProcessesTreeModel implements TreeModel {

	private static final long UPDATE_PERIOD = 2500;

	/** Object returned by {@link #getChild(Object, int)} if the list is empty or an error has occurred. */
	public static final Object EMPTY_PROCESS_LIST = new Object();
	public static final Object PENDING_PROCESS_LIST = new Object();


	public static enum ProcessListState {
		PENDING,
		READY,
		ERROR,
		CANCELED
	}

	class ProcessList {

		private List<Integer> knownIds = new LinkedList<Integer>();
		private Map<Integer, ProcessResponse> processResponses = new HashMap<Integer, ProcessResponse>();

		private ProcessListState state = ProcessListState.PENDING;

		public int add(ProcessResponse pr) {
			int newIndex = -1;
			if (!processResponses.containsKey(pr.getId())) {
				newIndex = knownIds.size();
				knownIds.add(pr.getId());
			}
			processResponses.put(pr.getId(), pr);
			state = ProcessListState.READY;
			return newIndex;
		}

		public ProcessListState getState() {
			return state;
		}

		public void setState(ProcessListState state) {
			this.state = state;
		}

		public ProcessResponse getByIndex(int index) {
			return processResponses.get(knownIds.get(index));
		}

		public ProcessResponse getById(int id) {
			return processResponses.get(id);
		}

		public int size() {
			return knownIds.size();
		}

		public int indexOf(ProcessResponse child) {
			int index = 0;
			for (Integer id : knownIds) {
				ProcessResponse pr = processResponses.get(id);
				if ((pr != null) && (pr.getId() == child.getId())) {
					return index;
				}
				index++;
			}
			return -1;
		}

		private void trim(Set<Integer> processIds, RemoteRepository repos) {
			boolean wasEmpty = knownIds.isEmpty();
			List<Integer> removedIndices = new LinkedList<Integer>();
			List<Object> removedObjects = new LinkedList<Object>();
			Iterator<Integer> i = knownIds.iterator();
			int index = 0;
			while (i.hasNext()) {
				Integer id = i.next();
				if (!processIds.contains(id)) {
					i.remove();
					ProcessResponse process = processResponses.remove(id);
					removedIndices.add(index);
					removedObjects.add(process);
				}
				index++;
			}
			if (!wasEmpty && knownIds.isEmpty()) {
				// list was not empty before, but now it is empty, so we fire structure changed. Model will return only EMPTY_PROCESS_LIST
				fireStructureChanged(new TreeModelEvent(this, new Object[] { root, repos }));
			} else if (!removedIndices.isEmpty()) {
				int[] indices = new int[removedIndices.size()];
				for (int j = 0; j < removedIndices.size(); j++) {
					indices[j] = removedIndices.get(j);
				}
				fireDelete(new TreeModelEvent(this, new Object[] { root, repos }, indices, removedObjects.toArray()));
			} else {
				return;
			}
		}
	}

	private final class UpdateTask extends TimerTask {
		@Override
		public void run() {
			final List<RemoteRepository> newRepositories = RemoteRepository.getAll();
			if (!newRepositories.equals(repositories)) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						public void run() {
							synchronized (repositories) {
								repositories = newRepositories;
								processes.clear();
								for (RemoteRepository repos : repositories) {
									processes.put(repos, new ProcessList());
								}
								fireStructureChanged(new TreeModelEvent(this, new Object[] { root }));
							}
						};
					});
				} catch (InterruptedException e) {
					LogService.getRoot().log(Level.WARNING, e.toString(), e);
				} catch (InvocationTargetException e) {
					LogService.getRoot().log(Level.WARNING, e.toString(), e);
				}
			}
			synchronized (repositories) {
				Iterator<RemoteRepository> iterator = repositories.iterator();
				while (iterator.hasNext()) {
					final RemoteRepository repos = iterator.next();
					if (observedRepositories.contains(repos)) {
						final ProcessList processList = processes.get(repos);
						try {
							if (repos.isPasswordInputCanceled()) {
								LogService.getRoot().log(Level.FINE,
										I18N.getMessage(LogService.getRoot().getResourceBundle(),
												"com.rapidminer.repository.gui.process.RemoteProcessesTreeModel.skipping_user_canceled_auth", repos.getName()));
								if (processList.getState() != ProcessListState.CANCELED) {
									SwingUtilities.invokeAndWait(new Runnable() {

										@Override
										public void run() {
											setIntoState(repos, ProcessListState.CANCELED);
										}
									});
								}
								continue;
							}

							ProcessServiceFacade processService = repos.getProcessService();
							if (processService == null) {
								continue;
							}
							final Collection<Integer> processIds = processService.getRunningProcesses(since);

							// First, delete removed ids
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override
								public void run() {
									if (processIds != null) {
										if (processIds.isEmpty()) {
											processList.setState(ProcessListState.READY);
											fireStructureChanged(new TreeModelEvent(this, new Object[] { root, repos }));
										} else {
											if (processList.getState() != ProcessListState.ERROR) {
												processList.setState(ProcessListState.READY);
											}
										}
										processList.trim(new HashSet<Integer>(processIds), repos);
									}
								}
							});
							// Now, update model for new / existing IDs
							for (Integer processId : processIds) {
								ProcessResponse oldProcess = processList.getById(processId);
								// we update if we don't know the id yet or if the process is not complete						
								if (oldProcess == null) {
									final ProcessResponse newResponse = processService.getRunningProcessesInfo(processId);
									SwingUtilities.invokeAndWait(new Runnable() {

										@Override
										public void run() {
											// If was empty before, fire event to remove EMPTY_PROCESS_LIST
											if (processList.size() == 0) {
												fireDelete(new TreeModelEvent(this, new Object[] { root, repos },
														new int[] { 0 },
														new Object[] { EMPTY_PROCESS_LIST }));
											}
											int newIndex = processList.add(newResponse);
											fireAdd(new TreeModelEvent(this, new Object[] { root, repos },
													new int[] { newIndex },
													new Object[] { newResponse }));

										}
									});
								} else if (!RemoteProcessState.valueOf(oldProcess.getState()).isTerminated()) {
									final ProcessResponse updatedResponse = processService.getRunningProcessesInfo(processId);
									SwingUtilities.invokeAndWait(new Runnable() {

										@Override
										public void run() {
											processList.add(updatedResponse);
											fireStructureChanged(new TreeModelEvent(this, new Object[] { root, repos, updatedResponse }));
										}
									});
								} else {
									// If process is terminated, there is not need to update.
									// The process is already in the list since it is copied
								}
							}
						} catch (Exception ex) {
							if (processList.getState() != ProcessListState.ERROR) {
								LogService.getRoot().log(Level.WARNING,
										I18N.getMessage(LogService.getRoot().getResourceBundle(),
												"com.rapidminer.repository.gui.process.RemoteProcessesTreeModel.fetching_remote_process_list_error",
												ex)
										);

								try {
									SwingUtilities.invokeAndWait(new Runnable() {
										@Override
										public void run() {
											setIntoState(repos, ProcessListState.ERROR);
										}
									});
								} catch (InvocationTargetException e) {
									LogService.getRoot().log(Level.WARNING,
											I18N.getMessage(LogService.getRoot().getResourceBundle(),
													"com.rapidminer.repository.gui.process.RemoteProcessesTreeModel.fetching_remote_process_list_error",
													e)
											);
								} catch (InterruptedException e) {
									LogService.getRoot().log(Level.WARNING,
											I18N.getMessage(LogService.getRoot().getResourceBundle(),
													"com.rapidminer.repository.gui.process.RemoteProcessesTreeModel.fetching_remote_process_list_error",
													e)
											);
								}
							}
						}
					}
				}
			}
		}

	}

	private Map<RemoteRepository, ProcessList> processes = new HashMap<RemoteRepository, ProcessList>();
	private List<RemoteRepository> repositories = new LinkedList<RemoteRepository>();
	private Set<RemoteRepository> observedRepositories = new HashSet<RemoteRepository>();

	private Object root = new Object();

	private Timer updateTimer = new Timer("RemoteProcess-Updater", true);

	private XMLGregorianCalendar since;

	public RemoteProcessesTreeModel() {
		updateTimer.schedule(new UpdateTask(), UPDATE_PERIOD, UPDATE_PERIOD);
	}

	private EventListenerList listeners = new EventListenerList();

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(TreeModelListener.class, l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(TreeModelListener.class, l);
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == root) {
			return repositories.get(index);
		} else if (parent instanceof RemoteRepository) {
			ProcessList processList = getProcessList((RemoteRepository) parent);
			if (processList.size() == 0) {
				if (processList.getState() == ProcessListState.PENDING) {
					return PENDING_PROCESS_LIST;
				} else {
					return EMPTY_PROCESS_LIST;
				}
			} else {
				return processList.getByIndex(index);
			}
		} else if (parent instanceof ProcessResponse) {
			ProcessResponse proResponse = (ProcessResponse) parent;
			if (proResponse.getException() != null) {
				if (index == 0) {
					return new ExceptionWrapper(proResponse.getException());
				} else {
					return null;
				}
			} else {
				ProcessStackTrace trace = proResponse.getTrace();
				int elementsSize = 0;
				if ((trace != null) && (trace.getElements() != null)) {
					elementsSize = trace.getElements().size();
				}
				if (index < elementsSize && trace != null) {
					return trace.getElements().get(index);
				} else {
					return new OutputLocation(proResponse.getOutputLocations().get(index - elementsSize));
				}
			}
		} else {
			return null;
		}
	}


	@Override
	public int getChildCount(Object parent) {
		if (parent == root) {
			return repositories.size();
		} else if (parent instanceof RemoteRepository) {
			ProcessList list = getProcessList((RemoteRepository) parent);
			if ((list == null) || (list.size() == 0)) {
				return 1; // if empty, just display single message string
			} else {
				return list.size();
			}
		} else if (parent instanceof ProcessResponse) {
			ProcessResponse proResponse = (ProcessResponse) parent;
			if (proResponse.getException() != null) {
				return 1;
			} else {
				int size = 0;
				ProcessStackTrace trace = proResponse.getTrace();
				if ((trace != null) && (trace.getElements() != null)) {
					size += trace.getElements().size();
				}
				if (proResponse.getOutputLocations() != null) {
					size += proResponse.getOutputLocations().size();
				}
				return size;
			}
		} else {
			return 0;
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (child == RemoteProcessesTreeModel.EMPTY_PROCESS_LIST || child == RemoteProcessesTreeModel.PENDING_PROCESS_LIST) {
			return 0;
		} else if (parent == root) {
			return repositories.indexOf(child);
		} else if (parent instanceof RemoteRepository) {
			if (child instanceof ProcessResponse) {
				return getProcessList((RemoteRepository) parent).indexOf((ProcessResponse) child);
			} else {
				return 0;
			}
		} else if (parent instanceof ProcessResponse) {
			ProcessResponse proResponse = (ProcessResponse) parent;
			if (child instanceof ProcessStackTraceElement) {
				ProcessStackTrace trace = proResponse.getTrace();
				if ((trace != null) && (trace.getElements() != null)) {
					return trace.getElements().indexOf(child);
				} else {
					return -1;
				}
			} else if (child instanceof OutputLocation) {
				if (proResponse.getOutputLocations() != null) {
					return proResponse.getOutputLocations().indexOf(((OutputLocation) child).getLocation());
				} else {
					return -1;
				}
			} else if (child instanceof ExceptionWrapper) {
				return 0;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object node) {
		return (node != root) && !(node instanceof ProcessResponse) && !(node instanceof RemoteRepository);
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// not editable		
	}

	private void fireAdd(TreeModelEvent e) {
		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
			l.treeNodesInserted(e);
		}
	}

//	private void fireUpdate(TreeModelEvent e) {
//		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
//			l.treeNodesChanged(e);
//		}
//	}

	private void fireDelete(TreeModelEvent event) {
		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
			l.treeNodesRemoved(event);
		}
	}

	private void fireStructureChanged(TreeModelEvent e) {
		for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {

			l.treeStructureChanged(e);
		}
	}

	public void setSince(Date since) {
		if (((since == null) && (this.since == null)) ||
				((since != null) && since.equals(this.since))) {
			return;
		}
		if (since == null) {
			this.since = null;
		} else {
			this.since = XMLTools.getXMLGregorianCalendar(since);
		}

		Iterator<RemoteRepository> iterator = repositories.iterator();
		while (iterator.hasNext()) {
			final RemoteRepository repos = iterator.next();
			setIntoState(repos, ProcessListState.PENDING);

		}
		// update all models and reset state into PENDING

	}

	private void setIntoState(final RemoteRepository repos, ProcessListState state) {
		ProcessList processList = processes.get(repos);
		if (processList.getState() == state) {
			return; // nothing to do
		}
		processList.setState(state);
		fireStructureChanged(new TreeModelEvent(this, new TreePath(new Object[] { root, repos })));
	}

	protected void observe(RemoteRepository rep) {
		observedRepositories.add(rep);
	}

	protected void ignore(RemoteRepository rep) {
		observedRepositories.remove(rep);
	}

	ProcessList getProcessList(RemoteRepository repository) {
		return processes.get(repository);
	}
}