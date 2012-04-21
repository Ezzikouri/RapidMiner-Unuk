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
package com.rapidminer.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.I18N;

/**
 * Dialog to create a cron expression via GUI.
 * 
 * @author Marco Boeck
 *
 */
public class CronEditorDialog extends ButtonDialog {

	// seconds elements
	private JRadioButton radioButtonSecOnce;
	private JRadioButton radioButtonSecEvery;
	private JRadioButton radioButtonSecRepeat;
	private JSpinner spinnerSecStart;
	private JSpinner spinnerSecRepeat;

	// minutes elements
	private JRadioButton radioButtonMinOnce;
	private JRadioButton radioButtonMinEvery;
	private JRadioButton radioButtonMinRepeat;
	private JSpinner spinnerMinStart;
	private JSpinner spinnerMinRepeat;

	// hours elements
	private JRadioButton radioButtonHourOnce;
	private JRadioButton radioButtonHourEvery;
	private JRadioButton radioButtonHourRepeat;
	private JSpinner spinnerHourStart;
	private JSpinner spinnerHourRepeat;

	// days elements
	private JRadioButton radioButtonDayOnce;
	private JRadioButton radioButtonDayEvery;
	private JRadioButton radioButtonDayRepeat;
	private JRadioButton radioButtonDayUseDayOfWeek;
	private JSpinner spinnerDayStart;
	private JSpinner spinnerDayRepeat;
	private JCheckBox checkBoxMonday;
	private JCheckBox checkBoxTuesday;
	private JCheckBox checkBoxWednesday;
	private JCheckBox checkBoxThursday;
	private JCheckBox checkBoxFriday;
	private JCheckBox checkBoxSaturday;
	private JCheckBox checkBoxSunday;

	// months elements
	private JRadioButton radioButtonMonthOnce;
	private JRadioButton radioButtonMonthEvery;
	private JRadioButton radioButtonMonthRepeat;
	private JRadioButton radioButtonMonthUseMonthOfYear;
	private JSpinner spinnerMonthStart;
	private JSpinner spinnerMonthRepeat;
	private JCheckBox checkBoxJanuary;
	private JCheckBox checkBoxFebruary;
	private JCheckBox checkBoxMarch;
	private JCheckBox checkBoxApril;
	private JCheckBox checkBoxMay;
	private JCheckBox checkBoxJune;
	private JCheckBox checkBoxJuly;
	private JCheckBox checkBoxAugust;
	private JCheckBox checkBoxSeptember;
	private JCheckBox checkBoxOctober;
	private JCheckBox checkBoxNovember;
	private JCheckBox checkBoxDecember;

	// years elements
	private JCheckBox checkBoxYearEnabled;
	private JRadioButton radioButtonYearOnce;
	private JRadioButton radioButtonYearEvery;
	private JRadioButton radioButtonYearRepeat;
	private JSpinner spinnerYearStart;
	private JSpinner spinnerYearRepeat;


	private static final long serialVersionUID = 837836954191730785L;


	/**
	 * Creates a new cron editor dialog.
	 */
	public CronEditorDialog() {
		super("croneditordialog", true);

		setupGUI();

		// misc settings
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
	}

	/**
	 * Creates the GUI.
	 */
	private void setupGUI() {
		// setup GUI
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		// seconds section
		JPanel panelSec = new JPanel();
		panelSec.setBorder(BorderFactory.createTitledBorder(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.panel_sec.label")));
		panelSec.setLayout(new GridBagLayout());

		spinnerSecStart = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
		spinnerSecStart.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_once_spinner.tip"));
		spinnerSecRepeat = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
		spinnerSecRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_repeat_spinner.tip"));

		radioButtonSecOnce = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_once.label"));
		radioButtonSecEvery = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_every.label"));
		radioButtonSecRepeat = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_repeat.label"));
		ButtonGroup secButtonGroup = new ButtonGroup();
		secButtonGroup.add(radioButtonSecOnce);
		secButtonGroup.add(radioButtonSecEvery);
		secButtonGroup.add(radioButtonSecRepeat);

		radioButtonSecOnce.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerSecStart.setEnabled(true);
				spinnerSecRepeat.setEnabled(false);
			}
		});
		radioButtonSecOnce.doClick();
		radioButtonSecOnce.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_once.tip"));
		radioButtonSecEvery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerSecStart.setEnabled(false);
				spinnerSecRepeat.setEnabled(false);
			}
		});
		radioButtonSecEvery.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_every.tip"));
		radioButtonSecRepeat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerSecStart.setEnabled(true);
				spinnerSecRepeat.setEnabled(true);
			}
		});
		radioButtonSecRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_sec_repeat.tip"));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelSec.add(radioButtonSecOnce, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		panelSec.add(spinnerSecStart, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelSec.add(radioButtonSecEvery, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		panelSec.add(radioButtonSecRepeat, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		panelSec.add(spinnerSecRepeat, gbc);

		// minutes section
		JPanel panelMin = new JPanel();
		panelMin.setBorder(BorderFactory.createTitledBorder(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.panel_min.label")));
		panelMin.setLayout(new GridBagLayout());

		spinnerMinStart = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
		spinnerMinStart.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_once_spinner.tip"));
		spinnerMinRepeat = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
		spinnerMinRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_repeat_spinner.tip"));

		radioButtonMinOnce = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_once.label"));
		radioButtonMinEvery = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_every.label"));
		radioButtonMinRepeat = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_repeat.label"));
		ButtonGroup minButtonGroup = new ButtonGroup();
		minButtonGroup.add(radioButtonMinOnce);
		minButtonGroup.add(radioButtonMinEvery);
		minButtonGroup.add(radioButtonMinRepeat);

		radioButtonMinOnce.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMinStart.setEnabled(true);
				spinnerMinRepeat.setEnabled(false);
			}
		});
		radioButtonMinOnce.doClick();
		radioButtonMinOnce.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_once.tip"));
		radioButtonMinEvery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMinStart.setEnabled(false);
				spinnerMinRepeat.setEnabled(false);
			}
		});
		radioButtonMinEvery.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_every.tip"));
		radioButtonMinRepeat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMinStart.setEnabled(true);
				spinnerMinRepeat.setEnabled(true);
			}
		});
		radioButtonMinRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_min_repeat.tip"));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelMin.add(radioButtonMinOnce, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		panelMin.add(spinnerMinStart, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelMin.add(radioButtonMinEvery, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		panelMin.add(radioButtonMinRepeat, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		panelMin.add(spinnerMinRepeat, gbc);

		// hours section
		JPanel panelHour = new JPanel();
		panelHour.setBorder(BorderFactory.createTitledBorder(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.panel_hour.label")));
		panelHour.setLayout(new GridBagLayout());

		spinnerHourStart = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
		spinnerHourStart.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_once_spinner.tip"));
		spinnerHourRepeat = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
		spinnerHourRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_repeat_spinner.tip"));

		radioButtonHourOnce = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_once.label"));
		radioButtonHourEvery = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_every.label"));
		radioButtonHourRepeat = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_repeat.label"));
		ButtonGroup hourButtonGroup = new ButtonGroup();
		hourButtonGroup.add(radioButtonHourOnce);
		hourButtonGroup.add(radioButtonHourEvery);
		hourButtonGroup.add(radioButtonHourRepeat);

		radioButtonHourOnce.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerHourStart.setEnabled(true);
				spinnerHourRepeat.setEnabled(false);
			}
		});
		radioButtonHourOnce.doClick();
		radioButtonHourOnce.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_once.tip"));
		radioButtonHourEvery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerHourStart.setEnabled(false);
				spinnerHourRepeat.setEnabled(false);
			}
		});
		radioButtonHourEvery.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_every.tip"));
		radioButtonHourRepeat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerHourStart.setEnabled(true);
				spinnerHourRepeat.setEnabled(true);
			}
		});
		radioButtonHourRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_hour_repeat.tip"));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelHour.add(radioButtonHourOnce, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		panelHour.add(spinnerHourStart, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelHour.add(radioButtonHourEvery, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		panelHour.add(radioButtonHourRepeat, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		panelHour.add(spinnerHourRepeat, gbc);

		// days section
		JPanel panelDay = new JPanel();
		panelDay.setBorder(BorderFactory.createTitledBorder(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.panel_day.label")));
		panelDay.setLayout(new GridBagLayout());

		spinnerDayStart = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
		spinnerDayStart.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_once_spinner.tip"));
		spinnerDayRepeat = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
		spinnerDayRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_repeat_spinner.tip"));

		radioButtonDayOnce = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_once.label"));
		radioButtonDayEvery = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_every.label"));
		radioButtonDayRepeat = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_repeat.label"));
		radioButtonDayUseDayOfWeek = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_day_of_week.label"));
		ButtonGroup dayButtonGroup = new ButtonGroup();
		dayButtonGroup.add(radioButtonDayOnce);
		dayButtonGroup.add(radioButtonDayEvery);
		dayButtonGroup.add(radioButtonDayRepeat);
		dayButtonGroup.add(radioButtonDayUseDayOfWeek);
		
		checkBoxMonday = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_monday.label"));
		checkBoxMonday.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_monday.tip"));
		checkBoxTuesday = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_tuesday.label"));
		checkBoxTuesday.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_tuesday.tip"));
		checkBoxWednesday = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_wednesday.label"));
		checkBoxWednesday.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_wednesday.tip"));
		checkBoxThursday = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_thursday.label"));
		checkBoxThursday.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_thursday.tip"));
		checkBoxFriday = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_friday.label"));
		checkBoxFriday.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_friday.tip"));
		checkBoxSaturday = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_saturday.label"));
		checkBoxSaturday.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_saturday.tip"));
		checkBoxSunday = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_sunday.label"));
		checkBoxSunday.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_of_week_sunday.tip"));

		radioButtonDayOnce.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerDayStart.setEnabled(true);
				spinnerDayRepeat.setEnabled(false);
				checkBoxMonday.setEnabled(false);
				checkBoxTuesday.setEnabled(false);
				checkBoxWednesday.setEnabled(false);
				checkBoxThursday.setEnabled(false);
				checkBoxFriday.setEnabled(false);
				checkBoxSaturday.setEnabled(false);
				checkBoxSunday.setEnabled(false);
			}
		});
		radioButtonDayOnce.doClick();
		radioButtonDayOnce.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_once.tip"));
		radioButtonDayEvery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerDayStart.setEnabled(false);
				spinnerDayRepeat.setEnabled(false);
				checkBoxMonday.setEnabled(false);
				checkBoxTuesday.setEnabled(false);
				checkBoxWednesday.setEnabled(false);
				checkBoxThursday.setEnabled(false);
				checkBoxFriday.setEnabled(false);
				checkBoxSaturday.setEnabled(false);
				checkBoxSunday.setEnabled(false);
			}
		});
		radioButtonDayEvery.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_every.tip"));
		radioButtonDayRepeat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerDayStart.setEnabled(true);
				spinnerDayRepeat.setEnabled(true);
				checkBoxMonday.setEnabled(false);
				checkBoxTuesday.setEnabled(false);
				checkBoxWednesday.setEnabled(false);
				checkBoxThursday.setEnabled(false);
				checkBoxFriday.setEnabled(false);
				checkBoxSaturday.setEnabled(false);
				checkBoxSunday.setEnabled(false);
			}
		});
		radioButtonDayRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_repeat.tip"));
		radioButtonDayUseDayOfWeek.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerDayStart.setEnabled(false);
				spinnerDayRepeat.setEnabled(false);
				checkBoxMonday.setEnabled(true);
				checkBoxTuesday.setEnabled(true);
				checkBoxWednesday.setEnabled(true);
				checkBoxThursday.setEnabled(true);
				checkBoxFriday.setEnabled(true);
				checkBoxSaturday.setEnabled(true);
				checkBoxSunday.setEnabled(true);
			}
		});
		radioButtonDayUseDayOfWeek.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_day_day_of_week.tip"));

		JPanel panelDayOfWeek = new JPanel();
		panelDayOfWeek.setLayout(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelDay.add(radioButtonDayOnce, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		panelDay.add(spinnerDayStart, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelDay.add(radioButtonDayEvery, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		panelDay.add(radioButtonDayRepeat, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		panelDay.add(spinnerDayRepeat, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 7;
		gbc.anchor = GridBagConstraints.WEST;
		panelDay.add(radioButtonDayUseDayOfWeek, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelDayOfWeek.add(checkBoxMonday, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		panelDayOfWeek.add(checkBoxTuesday, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelDayOfWeek.add(checkBoxWednesday, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		panelDayOfWeek.add(checkBoxThursday, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		panelDayOfWeek.add(checkBoxFriday, gbc);
		
		gbc.gridx = 5;
		gbc.gridy = 0;
		panelDayOfWeek.add(checkBoxSaturday, gbc);
		
		gbc.gridx = 6;
		gbc.gridy = 0;
		panelDayOfWeek.add(checkBoxSunday, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 7;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		panelDay.add(panelDayOfWeek, gbc);
		
		// months section
		JPanel panelMonth = new JPanel();
		panelMonth.setBorder(BorderFactory.createTitledBorder(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.panel_month.label")));
		panelMonth.setLayout(new GridBagLayout());

		spinnerMonthStart = new JSpinner(new SpinnerNumberModel(0, 0, 11, 1));
		spinnerMonthStart.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_once_spinner.tip"));
		spinnerMonthRepeat = new JSpinner(new SpinnerNumberModel(0, 0, 11, 1));
		spinnerMonthRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_repeat_spinner.tip"));

		radioButtonMonthOnce = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_once.label"));
		radioButtonMonthEvery = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_every.label"));
		radioButtonMonthRepeat = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_repeat.label"));
		radioButtonMonthUseMonthOfYear = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_month_of_year.label"));
		ButtonGroup monthButtonGroup = new ButtonGroup();
		monthButtonGroup.add(radioButtonMonthOnce);
		monthButtonGroup.add(radioButtonMonthEvery);
		monthButtonGroup.add(radioButtonMonthRepeat);
		monthButtonGroup.add(radioButtonMonthUseMonthOfYear);
		
		checkBoxJanuary = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_january.label"));
		checkBoxJanuary.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_january.tip"));
		checkBoxFebruary = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_february.label"));
		checkBoxFebruary.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_february.tip"));
		checkBoxMarch = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_march.label"));
		checkBoxMarch.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_march.tip"));
		checkBoxApril = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_april.label"));
		checkBoxApril.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_april.tip"));
		checkBoxMay = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_may.label"));
		checkBoxMay.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_may.tip"));
		checkBoxJune = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_june.label"));
		checkBoxJune.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_june.tip"));
		checkBoxJuly = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_july.label"));
		checkBoxJuly.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_july.tip"));
		checkBoxAugust = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_august.label"));
		checkBoxAugust.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_august.tip"));
		checkBoxSeptember = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_september.label"));
		checkBoxSeptember.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_september.tip"));
		checkBoxOctober = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_october.label"));
		checkBoxOctober.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_october.tip"));
		checkBoxNovember = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_november.label"));
		checkBoxNovember.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_november.tip"));
		checkBoxDecember = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_december.label"));
		checkBoxDecember.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_of_year_december.tip"));
		
		radioButtonMonthOnce.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMonthStart.setEnabled(true);
				spinnerMonthRepeat.setEnabled(false);
				checkBoxJanuary.setEnabled(false);
				checkBoxFebruary.setEnabled(false);
				checkBoxMarch.setEnabled(false);
				checkBoxApril.setEnabled(false);
				checkBoxMay.setEnabled(false);
				checkBoxJune.setEnabled(false);
				checkBoxJuly.setEnabled(false);
				checkBoxAugust.setEnabled(false);
				checkBoxSeptember.setEnabled(false);
				checkBoxOctober.setEnabled(false);
				checkBoxNovember.setEnabled(false);
				checkBoxDecember.setEnabled(false);
			}
		});
		radioButtonMonthOnce.doClick();
		radioButtonMonthOnce.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_once.tip"));
		radioButtonMonthEvery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMonthStart.setEnabled(false);
				spinnerMonthRepeat.setEnabled(false);
				checkBoxJanuary.setEnabled(false);
				checkBoxFebruary.setEnabled(false);
				checkBoxMarch.setEnabled(false);
				checkBoxApril.setEnabled(false);
				checkBoxMay.setEnabled(false);
				checkBoxJune.setEnabled(false);
				checkBoxJuly.setEnabled(false);
				checkBoxAugust.setEnabled(false);
				checkBoxSeptember.setEnabled(false);
				checkBoxOctober.setEnabled(false);
				checkBoxNovember.setEnabled(false);
				checkBoxDecember.setEnabled(false);
			}
		});
		radioButtonMonthEvery.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_every.tip"));
		radioButtonMonthRepeat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMonthStart.setEnabled(true);
				spinnerMonthRepeat.setEnabled(true);
				checkBoxJanuary.setEnabled(false);
				checkBoxFebruary.setEnabled(false);
				checkBoxMarch.setEnabled(false);
				checkBoxApril.setEnabled(false);
				checkBoxMay.setEnabled(false);
				checkBoxJune.setEnabled(false);
				checkBoxJuly.setEnabled(false);
				checkBoxAugust.setEnabled(false);
				checkBoxSeptember.setEnabled(false);
				checkBoxOctober.setEnabled(false);
				checkBoxNovember.setEnabled(false);
				checkBoxDecember.setEnabled(false);
			}
		});
		radioButtonMonthRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_repeat.tip"));
		radioButtonMonthUseMonthOfYear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMonthStart.setEnabled(false);
				spinnerMonthRepeat.setEnabled(false);
				checkBoxJanuary.setEnabled(true);
				checkBoxFebruary.setEnabled(true);
				checkBoxMarch.setEnabled(true);
				checkBoxApril.setEnabled(true);
				checkBoxMay.setEnabled(true);
				checkBoxJune.setEnabled(true);
				checkBoxJuly.setEnabled(true);
				checkBoxAugust.setEnabled(true);
				checkBoxSeptember.setEnabled(true);
				checkBoxOctober.setEnabled(true);
				checkBoxNovember.setEnabled(true);
				checkBoxDecember.setEnabled(true);
			}
		});
		radioButtonMonthUseMonthOfYear.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_month_month_of_year.tip"));
		
		JPanel panelMonthOfYear = new JPanel();
		panelMonthOfYear.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelMonth.add(radioButtonMonthOnce, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		panelMonth.add(spinnerMonthStart, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelMonth.add(radioButtonMonthEvery, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		panelMonth.add(radioButtonMonthRepeat, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		panelMonth.add(spinnerMonthRepeat, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 7;
		gbc.anchor = GridBagConstraints.WEST;
		panelMonth.add(radioButtonMonthUseMonthOfYear, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelMonthOfYear.add(checkBoxJanuary, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		panelMonthOfYear.add(checkBoxFebruary, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelMonthOfYear.add(checkBoxMarch, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		panelMonthOfYear.add(checkBoxApril, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		panelMonthOfYear.add(checkBoxMay, gbc);
		
		gbc.gridx = 5;
		gbc.gridy = 0;
		panelMonthOfYear.add(checkBoxJune, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		panelMonthOfYear.add(checkBoxJuly, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		panelMonthOfYear.add(checkBoxAugust, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 1;
		panelMonthOfYear.add(checkBoxSeptember, gbc);
		
		gbc.gridx = 3;
		gbc.gridy = 1;
		panelMonthOfYear.add(checkBoxOctober, gbc);
		
		gbc.gridx = 4;
		gbc.gridy = 1;
		panelMonthOfYear.add(checkBoxNovember, gbc);
		
		gbc.gridx = 5;
		gbc.gridy = 1;
		panelMonthOfYear.add(checkBoxDecember, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 7;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		panelMonth.add(panelMonthOfYear, gbc);

		// years section
		JPanel panelYear = new JPanel();
		panelYear.setBorder(BorderFactory.createTitledBorder(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.panel_year.label")));
		panelYear.setLayout(new GridBagLayout());
		
		spinnerYearStart = new JSpinner(new SpinnerNumberModel(0, 0, 11, 1));
		spinnerYearStart.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_once_spinner.tip"));
		spinnerYearRepeat = new JSpinner(new SpinnerNumberModel(0, 0, 11, 1));
		spinnerYearRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_repeat_spinner.tip"));

		radioButtonYearOnce = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_once.label"));
		radioButtonYearEvery = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_every.label"));
		radioButtonYearRepeat = new JRadioButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_repeat.label"));
		ButtonGroup yearButtonGroup = new ButtonGroup();
		yearButtonGroup.add(radioButtonYearOnce);
		yearButtonGroup.add(radioButtonYearEvery);
		yearButtonGroup.add(radioButtonYearRepeat);

		radioButtonYearOnce.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerYearStart.setEnabled(true);
				spinnerYearRepeat.setEnabled(false);
			}
		});
		radioButtonYearOnce.doClick();
		radioButtonYearOnce.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_once.tip"));
		radioButtonYearEvery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerYearStart.setEnabled(false);
				spinnerYearRepeat.setEnabled(false);
			}
		});
		radioButtonYearEvery.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_every.tip"));
		radioButtonYearRepeat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerYearStart.setEnabled(true);
				spinnerYearRepeat.setEnabled(true);
			}
		});
		radioButtonYearRepeat.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_repeat.tip"));
		
		checkBoxYearEnabled = new JCheckBox(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_enabled.label"));
		checkBoxYearEnabled.setToolTipText((I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.cron_editor.cron_year_enabled.tip")));
		checkBoxYearEnabled.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkBoxYearEnabled.isSelected()) {
					radioButtonYearOnce.setEnabled(true);
					radioButtonYearEvery.setEnabled(true);
					radioButtonYearRepeat.setEnabled(true);
					if (radioButtonYearOnce.isSelected()) {
						radioButtonYearOnce.doClick();
					} else if (radioButtonYearEvery.isSelected()) {
						radioButtonYearEvery.doClick();
					} else if (radioButtonYearRepeat.isSelected()) {
						radioButtonYearRepeat.doClick();
					}
				} else {
					radioButtonYearOnce.setEnabled(false);
					radioButtonYearEvery.setEnabled(false);
					radioButtonYearRepeat.setEnabled(false);
					spinnerYearStart.setEnabled(false);
					spinnerYearRepeat.setEnabled(false);
				}
			}
		});
		checkBoxYearEnabled.setSelected(true);
		checkBoxYearEnabled.doClick();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		panelYear.add(checkBoxYearEnabled, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		panelYear.add(radioButtonYearOnce, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		panelYear.add(spinnerYearStart, gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
		panelYear.add(radioButtonYearEvery, gbc);

		gbc.gridx = 3;
		gbc.gridy = 1;
		panelYear.add(radioButtonYearRepeat, gbc);

		gbc.gridx = 4;
		gbc.gridy = 1;
		panelYear.add(spinnerYearRepeat, gbc);
		
		
		// button section
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelButtons.add(makeOkButton(), gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		// don't want to dispose of dialog, can be reused with previously entered values
		// so removing standard disposing listener and creating own listener
		Action cancelAction = new ResourceAction("cancel") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				wasConfirmed = false;
				setVisible(false);
			}			
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "CANCEL");  
		getRootPane().getActionMap().put("CANCEL", cancelAction);
		JButton cancelButton = new JButton(cancelAction);
		panelButtons.add(cancelButton, gbc);


		// add panels
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 10, 5, 10);
		add(panelSec, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		add(panelMin, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		add(panelHour, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		add(panelDay, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		add(panelMonth, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		add(panelYear, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 6;
		add(panelButtons, gbc);
		

		// center dialog
		setSize(500, 720);
		setLocationRelativeTo(null);
	}

	/**
	 * Returns the cron expression as a {@link String} if {@link #wasConfirmed()} returns <code>true</code>,
	 * otherwise returns an empty {@link String}.
	 * @return
	 */
	public String getCronExpression() {
		if (wasConfirmed()) {
			StringBuffer cronBuffer = new StringBuffer();
			
			
			return cronBuffer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Shows the cron editor dialog.
	 */
	public void prompt() {
		setVisible(true);
	}

}
