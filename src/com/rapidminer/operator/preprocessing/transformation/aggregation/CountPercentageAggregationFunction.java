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
package com.rapidminer.operator.preprocessing.transformation.aggregation;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.tools.Ontology;

/**
 * This function first behaves like {@link CountAggregationFunction}, but it
 * delivers percentages of the total count instead of absolute values.
 * E.g. {@link SumAggregationFunction} delivers [2, 5, 3] this function
 * would deliver [20, 50, 30]
 * 
 * @author Marco Boeck
 *
 */
public class CountPercentageAggregationFunction extends CountAggregationFunction {

	public static final String FUNCTION_COUNT_PERCENTAGE = "percentage_count";

	public CountPercentageAggregationFunction(Attribute sourceAttribute, boolean ignoreMissings, boolean countOnlyDisctinct) {
		super(sourceAttribute, ignoreMissings, countOnlyDisctinct, FUNCTION_COUNT_PERCENTAGE, FUNCTION_SEPARATOR_OPEN, FUNCTION_SEPARATOR_CLOSE);
	}

	public CountPercentageAggregationFunction(Attribute sourceAttribute, boolean ignoreMissings, boolean countOnlyDisctinct, String functionName,
			String separatorOpen, String separatorClose) {
		super(sourceAttribute, ignoreMissings, countOnlyDisctinct, functionName, separatorOpen, separatorClose);
	}

	@Override
	public void postProcessing(List<Aggregator> allAggregators) {
		double totalCount = 0;
		
		// calculate total count
		for (Aggregator aggregator : allAggregators) {
			double value = ((CountAggregator)aggregator).getCount();
			if (Double.isNaN(value)) {
				totalCount = Double.NaN;
				break;
			}
			totalCount += value;
		}
		
		// devide by total count
		for (Aggregator aggregator : allAggregators) {
			CountAggregator countAggregator = (CountAggregator) aggregator;
			countAggregator.setCount((countAggregator.getCount()/totalCount)*100);
		}
	}
	
	@Override
	protected int getTargetValueType(int sourceValueType) {
		return Ontology.REAL;
	}
}
