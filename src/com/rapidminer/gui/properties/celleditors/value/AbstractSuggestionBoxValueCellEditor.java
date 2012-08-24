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
package com.rapidminer.gui.properties.celleditors.value;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.ProgressListener;

/** 
 *  Renders a combo box which can be filled with suggestions.
 * 
 * @author Marcin Skirzynski
 */
public abstract class AbstractSuggestionBoxValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {
	private static final long serialVersionUID = -771727412083431607L;
	
	/**
	 * The model of the combo box which consist of the suggestions
	 */
	private final SuggestionComboBoxModel model; 

	/**
	 * The GUI element
	 */
	private final JComboBox comboBox; 

	private Operator operator;

	private ParameterType type;

	public AbstractSuggestionBoxValueCellEditor(final ParameterType type) {
		this.type = type;
		this.model = new SuggestionComboBoxModel();
		this.comboBox = new SuggestionComboBox(model);
		comboBox.setToolTipText(type.getDescription());		
	}
	
	public abstract List<Object> getSuggestions(Operator operator, ProgressListener progressListener);
	
	private String getValue() {
		String value = null;
		value = operator.getParameters().getParameterOrNull(type.getKey());
		return value;
	}

	@Override
	public boolean rendersLabel() {
		return false;
	}

	@Override
	public boolean useEditorAsRenderer() {
		return true;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//		model.updateModel();
		comboBox.setSelectedItem(value);
		return comboBox;
	}

	@Override
	public Object getCellEditorValue() {
		return comboBox.getSelectedItem();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//		model.updateModel();
		comboBox.setSelectedItem(value);
		return comboBox;
	}

	@Override
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	class SuggestionComboBoxModel extends AbstractListModel implements ComboBoxModel, Serializable {
		private static final long serialVersionUID = -2984664300141879731L;

		private LinkedList<Object> list = new LinkedList<Object>();

		private Object selected = null;
		
		
		public boolean updateModel() {
			final Object selected = getValue();

			ProgressThread t = new ProgressThread("fetching_suggestions") {
				@Override
				public void run() {
					try {
						getProgressListener().setTotal(100);
						getProgressListener().setCompleted(0);
						list.clear();
						
						// fill list with stuff
						list.addAll(getSuggestions(operator, getProgressListener()));
						
						getProgressListener().setCompleted(100);
						if (getSelectedItem() == null) {
							if (model.getSize() == 0) {
								setSelectedItem(null);
							} else {
								if (selected != null) {
									setSelectedItem(selected);
								} else {
									if (model.getSize() > 0) {
										setSelectedItem(model.getElementAt(0));
									}
								}
							}
						}
						fireContentsChanged(this, 0, list.size() - 1);
					} finally {
						getProgressListener().complete();
					}
				}
			};
			t.start();
			return true;
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object object) {
			if ((selected != null && !selected.equals(object)) || selected == null && object != null) {
				selected = object;
				fireContentsChanged(this, -1, -1);
			}
		}

		@Override
		public Object getElementAt(int index) {
			if (index >= 0 && index < list.size()) {
				return list.get(index);
			}
			return null;
		}

		@Override
		public int getSize() {
			return list.size();
		}
	}

	class SuggestionComboBox extends JComboBox {
		private static final long serialVersionUID = 4000279412600950101L;

		private SuggestionComboBox(final SuggestionComboBoxModel model) {
			super(model);
			setEditable(true);
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}			
			});
			getEditor().getEditorComponent().addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if(!e.isTemporary()) {
						fireEditingStopped();
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
					model.updateModel();
				}
			});
			addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					if (model.updateModel()) {
						hidePopup();
						showPopup();
					}
				}
			});
		}
	}


}
