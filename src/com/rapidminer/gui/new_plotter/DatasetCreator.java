/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2011 by Rapid-I and the contributors
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
package com.rapidminer.gui.new_plotter;

import java.util.Random;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableExampleSetAdapter;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.repository.IOObjectEntry;
import com.rapidminer.repository.MalformedRepositoryLocationException;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.tools.Ontology;


/**
 * TODO remove me, I am a debug-only class
 * 
 * @author Marius Helf, Nils Woehler
 *
 */
public class DatasetCreator {

	public static DataTable createDataSet() throws MalformedRepositoryLocationException, RepositoryException {
		ExampleSet iris = (ExampleSet) ((IOObjectEntry)new RepositoryLocation("//Samples/data/Iris").locateEntry()).retrieveData(null);
		ExampleSet exampleSet = iris;
		Attribute expAttr = AttributeFactory.createAttribute("e_a1", Ontology.REAL);
		Attribute a1_6 = AttributeFactory.createAttribute("a1-6", Ontology.REAL);
		Attribute a1_noisy = AttributeFactory.createAttribute("a1_noisy", Ontology.REAL);
		Attribute a1_noisy_d1000 = AttributeFactory.createAttribute("a1_noisy/1000", Ontology.REAL);
		Attribute a1_noisy_m1000 = AttributeFactory.createAttribute("a1_noisy*1.000.000", Ontology.REAL);
		Attribute distinct_values = AttributeFactory.createAttribute("distinct", Ontology.REAL);
		Attribute ordered_distinct_values = AttributeFactory.createAttribute("ordered distinct", Ontology.REAL);
		
		
		ExampleTable dTable = exampleSet.getExampleTable();
		dTable.addAttribute(expAttr);
		dTable.addAttribute(a1_6);
		dTable.addAttribute(a1_noisy);
		dTable.addAttribute(a1_noisy_d1000);
		dTable.addAttribute(a1_noisy_m1000);
		dTable.addAttribute(distinct_values);
		dTable.addAttribute(ordered_distinct_values);
		
		
		exampleSet.getAttributes().addRegular(expAttr);
		exampleSet.getAttributes().addRegular(a1_6);
		exampleSet.getAttributes().addRegular(a1_noisy);
		exampleSet.getAttributes().addRegular(a1_noisy_d1000);
		exampleSet.getAttributes().addRegular(a1_noisy_m1000);
		exampleSet.getAttributes().addRegular(distinct_values);
		exampleSet.getAttributes().addRegular(ordered_distinct_values);
		
		Attribute a1 = exampleSet.getAttributes().get("a1");
		
		Random rng = new Random();

		double value = 0;
		for (Example example : exampleSet) {
			value += rng.nextDouble()+.001;
			double a1Value = example.getValue(a1);
			double powValue = Math.pow(Math.E, a1Value);
			example.setValue(expAttr, powValue);
			example.setValue(a1_6, a1Value-6);
			double noisyValue = a1Value+rng.nextDouble()-0.5;
			example.setValue(a1_noisy, noisyValue);
			example.setValue(a1_noisy_d1000, noisyValue/1000.0);
			example.setValue(a1_noisy_m1000, noisyValue*1000000.0);
			example.setValue(distinct_values, rng.nextDouble());
			example.setValue(ordered_distinct_values, value);
		}
//		ExampleSet hugeRandomData = (ExampleSet) ((IOObjectEntry)new RepositoryLocation("//Berta/home/helf/test/much_data100000").locateEntry()).retrieveData(null);
//		ExampleSet exampleSet = hugeRandomData;
//		ExampleSet hugeRandomData = (ExampleSet) ((IOObjectEntry)new RepositoryLocation("//LocalRepository/development/Plotters/data/random_10000").locateEntry()).retrieveData(null);
//		exampleSet = hugeRandomData;
		DataTableExampleSetAdapter dataTableExampleSetAdapter = new DataTableExampleSetAdapter(exampleSet, null);
		return dataTableExampleSetAdapter;
	}
	
}
