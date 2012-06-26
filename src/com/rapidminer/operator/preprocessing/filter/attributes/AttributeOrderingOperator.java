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

package com.rapidminer.operator.preprocessing.filter.attributes;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.features.selection.AbstractFeatureSelection;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDTransformationRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttributeOrderingRules;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.Tools;

/**
 * This Operator is capable of sorting attributes of an {@link ExampleSet}.
 * Either alphabetically or by user specified ordering roles.
 * 
 * @author Nils Woehler
 *
 */
public class AttributeOrderingOperator extends AbstractFeatureSelection {

	//--------------------- Order method ---------------------------------
	public static final String PARAMETER_ORDER_MODE = "sort_mode";

	public static final String USER_SPECIFIED_RULES_MODE = "user specified";
	public static final String ALPHABETICALLY_MODE = "alphabetically";

	public static final String[] SORT_MODES = new String[] { USER_SPECIFIED_RULES_MODE, ALPHABETICALLY_MODE };
	public static final int USER_SPECIFIED_RULES_MODE_INDEX = 0;
	public static final int ALPHABETICALLY_MODE_INDEX = 1;

	//--------------------- Sort direction -------------------------------
	public static final String PARAMETER_SORT_DIRECTION = "sort_direction";

	public static final String DIRECTION_ASCENDING = "ascending";
	public static final String DIRECTION_DESCENDING = "descending";
	public static final String DIRECTION_NONE = "none";

	public static final String[] SORT_DIRECTIONS = new String[] { DIRECTION_ASCENDING, DIRECTION_DESCENDING, DIRECTION_NONE };
	public static final int DIRECTION_ASCENDING_INDEX = 0;
	public static final int DIRECTION_DESCENDING_INDEX = 1;
	public static final int DIRECTION_NONE_INDEX = 2;

	//--------------------- Others ---------------------------------------

	public static final String PARAMETER_ORDER_RULES = "attribute_ordering";
	public static final String PARAMETER_USE_REGEXP = "use_regular_expressions";

	public static final String PARAMETER_HANDLE_UNMATCHED_ATTRIBUTES = "handle_unmachted";

	public static final String REMOVE_UNMATCHED_MODE = "remove";
	public static final String PREPEND_UNMATCHED_MODE = "prepend";
	public static final String APPEND_UNMATCHED_MODE = "append";

	public static final String[] HANDLE_UNMATCHED_MODES = { REMOVE_UNMATCHED_MODE, PREPEND_UNMATCHED_MODE, APPEND_UNMATCHED_MODE };

	public static final int REMOVE_UNMATCHED_MODE_INDEX = 0;
	public static final int PREPEND_UNMATCHED_MODE_INDEX = 1;
	public static final int APPEND_UNMATCHED_MODE_INDEX = 2;

	/**
	 * @param description
	 */
	public AttributeOrderingOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new MDTransformationRule() {

			@Override
			public void transformMD() {
				MetaData md1 = getInputPorts().getPortByIndex(0).getMetaData();
				OutputPort outputPort = getOutputPorts().getPortByIndex(0);

				try {
					if ((md1 != null)) {
						if ((md1 instanceof ExampleSetMetaData)) {
							ExampleSetMetaData emd1 = (ExampleSetMetaData) md1;
							ExampleSetMetaData sortedEmd = new ExampleSetMetaData();

							List<AttributeMetaData> allAttributes = new LinkedList<AttributeMetaData>(emd1.getAllAttributes());

							if (getParameterAsString(PARAMETER_ORDER_MODE).equals(ALPHABETICALLY_MODE)) {
								outputPort.deliverMD(emd1); // no attributes will be removed, just deliver old MD
							} else if (getParameterAsString(PARAMETER_ORDER_MODE).equals(USER_SPECIFIED_RULES_MODE)) {
								String combinedMaskedRules = getParameterAsString(PARAMETER_ORDER_RULES);
								if (combinedMaskedRules == null || combinedMaskedRules.length() == 0) {
									outputPort.deliverMD(emd1);
								}

								// iterate over all rules
								for (String maskedRule : combinedMaskedRules.split("\\|")) {
									String rule = Tools.unmask('|', maskedRule); // unmask them to allow regexp

									// iterate over all attributes and check if rules apply
									Iterator<AttributeMetaData> iterator = allAttributes.iterator();
									while (iterator.hasNext()) {
										AttributeMetaData attrMD = iterator.next();
										boolean match = false;
										if (getParameterAsBoolean(PARAMETER_USE_REGEXP)) {
											try {
												if (attrMD.getName().matches(rule)) {
													match = true;
												}
											} catch (PatternSyntaxException e) {
												outputPort.deliverMD(emd1);
											}
										} else {
											if (attrMD.getName().equals(rule)) {
												match = true;
											}
										}

										// if rule applies remove attribute from unmachted list and add it to rules matched list
										if (match) {
											iterator.remove();
											sortedEmd.addAttribute(attrMD);
										}
									}

								}

								if (!getParameterAsString(PARAMETER_HANDLE_UNMATCHED_ATTRIBUTES).equals(REMOVE_UNMATCHED_MODE)) {
									sortedEmd.addAllAttributes(allAttributes);
								}

								outputPort.deliverMD(sortedEmd);
							} else {
								outputPort.deliverMD(new ExampleSetMetaData());
							}
						} else {
							outputPort.deliverMD(null);
						}
					}
				} catch (UndefinedParameterError e) {
					outputPort.deliverMD(null);
				}
			}

		});
	}

	@Override
	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {

		if (getParameterAsString(PARAMETER_ORDER_MODE).equals(ALPHABETICALLY_MODE)) {

			if (getParameterAsString(PARAMETER_SORT_DIRECTION).equals(DIRECTION_NONE)) {
				return exampleSet;
			}

			// get attributes
			Attributes attributes = exampleSet.getAttributes();
			List<Attribute> sortedAttributeList = getAttributeList(attributes);

			// sort attributes
			sortAttributeListAlphabetically(sortedAttributeList);

			// apply sorted attributes
			applySortedAttributes(sortedAttributeList, null, attributes);

		} else if (getParameterAsString(PARAMETER_ORDER_MODE).equals(USER_SPECIFIED_RULES_MODE)) {
			String combinedMaskedRules = getParameterAsString(PARAMETER_ORDER_RULES);
			if (combinedMaskedRules == null || combinedMaskedRules.length() == 0) {
				throw new UserError(this, 205, PARAMETER_ORDER_RULES, "");
			}

			Attributes attributes = exampleSet.getAttributes();
			List<Attribute> unmachtedAttributes = getAttributeList(attributes);
			List<Attribute> sortedAttributes = new LinkedList<Attribute>();

			// iterate over all rules
			for (String maskedRule : combinedMaskedRules.split("\\|")) {
				String rule = Tools.unmask('|', maskedRule); // unmask them to allow regexp
				List<Attribute> matchedAttributes = new LinkedList<Attribute>();

				// iterate over all attributes and check if rules apply
				Iterator<Attribute> iterator = unmachtedAttributes.iterator();
				while (iterator.hasNext()) {
					Attribute attr = iterator.next();
					boolean match = false;
					if (getParameterAsBoolean(PARAMETER_USE_REGEXP)) {
						try {
							if (attr.getName().matches(rule)) {
								match = true;
							}
						} catch (PatternSyntaxException e) {
							throw new UserError(this, 206, rule, e.getMessage());
						}
					} else {
						if (attr.getName().equals(rule)) {
							match = true;
						}
					}

					// if rule applies remove attribute from unmachted list and add it to rules matched list
					if (match) {
						iterator.remove();
						matchedAttributes.add(attr);
					}
				}

				// sort matched attributes according to sort direction if more then one match has been found
				if (matchedAttributes.size() > 1) {
					sortAttributeListAlphabetically(matchedAttributes);
				}

				// add matched attributes to sorted attribute list
				sortedAttributes.addAll(matchedAttributes);

			}

			if (!getParameterAsString(PARAMETER_HANDLE_UNMATCHED_ATTRIBUTES).equals(REMOVE_UNMATCHED_MODE)) {
				// sort unmachted attributes according to sort direction
				sortAttributeListAlphabetically(unmachtedAttributes);

				if (getParameterAsString(PARAMETER_HANDLE_UNMATCHED_ATTRIBUTES).equals(PREPEND_UNMATCHED_MODE)) {
					// prepend attributes to ordered attributes list 
					sortedAttributes.addAll(0, unmachtedAttributes);
				} else {
					// append attributes to ordered attributes list 
					sortedAttributes.addAll(unmachtedAttributes);
				}

				applySortedAttributes(sortedAttributes, null, attributes);

			} else {
				applySortedAttributes(sortedAttributes, unmachtedAttributes, attributes);
			}

		} else {
			throw new IllegalArgumentException("Order mode " + getParameterAsString(PARAMETER_ORDER_MODE) + " is not implemented!");
		}
		return exampleSet;
	}

	private List<Attribute> getAttributeList(Attributes attributes) {
		List<Attribute> attributeList = new LinkedList<Attribute>();
		for (Attribute attr : attributes) {
			attributeList.add(attr);
		}
		return attributeList;
	}

	/**
	 * Applies the sorted and unmachted attribute list to the provided {@link Attributes}.
	 * All unmachted attributes are removed from attributes and all {@link Attribute}s from the sorted list
	 * are added in correct order.
	 * 
	 * @param sortedAttributeList attributes that will be removed first and added in correct order afterwards.
	 * @param unmachtedAttributes attributes that should be removed. May be <code>null</code> if no attributes should be removed.
	 */
	private void applySortedAttributes(List<Attribute> sortedAttributeList, List<Attribute> unmachtedAttributes, Attributes attributes) {
		if (unmachtedAttributes != null) {
			for (Attribute unmachted : unmachtedAttributes) {
				attributes.remove(unmachted);
			}
		}

		for (Attribute attribute : sortedAttributeList) {
			AttributeRole role = attributes.getRole(attribute);
			attributes.remove(attribute);

			if (role.isSpecial()) {
				attributes.setSpecialAttribute(attribute, role.getSpecialName());
			} else { //regular
				attributes.addRegular(attribute);
			}
		}
	}

	/**
	 * Sorts a list of attributes alphabetically according to the desired sort direction.
	 * CAUTION: The provided list 'unsortedAttributeList' will be changed internally.
	 */
	private void sortAttributeListAlphabetically(List<Attribute> unsortedAttributeList) throws UndefinedParameterError {

		// sort direction none -> just return 
		if (getParameterAsString(PARAMETER_SORT_DIRECTION).equals(DIRECTION_NONE)) {
			return;
		}

		// sort attributes
		Collections.sort(unsortedAttributeList, new Comparator<Attribute>() {

			@Override
			public int compare(Attribute o1, Attribute o2) {
				return Collator.getInstance().compare(o1.getName(), o2.getName());
			}

		});

		// if descending, reverse sort
		if (getParameterAsString(PARAMETER_SORT_DIRECTION).equals(DIRECTION_DESCENDING)) {
			Collections.reverse(unsortedAttributeList);
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterType type = new ParameterTypeCategory(PARAMETER_ORDER_MODE, "Ordering method that should be applied.", SORT_MODES, USER_SPECIFIED_RULES_MODE_INDEX, false);
		parameterTypes.add(type);

		type = new ParameterTypeCategory(PARAMETER_SORT_DIRECTION, "Sort direction for attribute names.", SORT_DIRECTIONS, DIRECTION_ASCENDING_INDEX, false);
		parameterTypes.add(type);

		// --------------------------- USER SPECIFIED -------------------------

		type = new ParameterTypeAttributeOrderingRules(PARAMETER_ORDER_RULES, "Rules to order attributes.", getInputPorts().getPortByIndex(0), false);
		type.setExpert(false);
		type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_ORDER_MODE, SORT_MODES, true, USER_SPECIFIED_RULES_MODE_INDEX));
		parameterTypes.add(type);

		type = new ParameterTypeCategory(PARAMETER_HANDLE_UNMATCHED_ATTRIBUTES, "Defines the behavior for unmatched attributes.", HANDLE_UNMATCHED_MODES,
				APPEND_UNMATCHED_MODE_INDEX, false);
		type.setOptional(true);
		type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_ORDER_MODE, SORT_MODES, false, USER_SPECIFIED_RULES_MODE_INDEX));
		parameterTypes.add(type);

		type = new ParameterTypeBoolean(PARAMETER_USE_REGEXP, "If checked attribute orders will be evaluated as regular expressions.", false, true);
		type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_ORDER_MODE, SORT_MODES, true, USER_SPECIFIED_RULES_MODE_INDEX));
		parameterTypes.add(type);

		return parameterTypes;
	}
}