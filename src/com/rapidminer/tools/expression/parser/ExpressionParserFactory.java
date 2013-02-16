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

package com.rapidminer.tools.expression.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import com.rapidminer.Process;
import com.rapidminer.tools.LogService;

/**
 * @author Venkatesh Umaashankar
 *
 */
public class ExpressionParserFactory {

	private static String parserClassName;
	private static Class<? extends AbstractExpressionParser> parserClass;

	static {
		ExpressionParserFactory.registerParserClass("com.rapidminer.tools.jep.function.ExpressionParser");
	}

	public static void registerParserClass(String parserClass) {
		ExpressionParserFactory.parserClassName = parserClass;
	}

	public static AbstractExpressionParser getExpressionParser(boolean useStandardConstants) {

		Object[] initargs = { new Boolean(useStandardConstants) };
		Class[] paramTypes = {boolean.class};
		try {
			Class<? extends AbstractExpressionParser> parserClass = (Class<? extends AbstractExpressionParser>) Class.forName(parserClassName);
			Method getParserMethod = parserClass.getDeclaredMethod("getExpressionParser", paramTypes);
			return (AbstractExpressionParser) getParserMethod.invoke(parserClass, initargs);
		} catch (ClassNotFoundException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (IllegalAccessException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (NoSuchMethodException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (SecurityException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (IllegalArgumentException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (InvocationTargetException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		}
		return null;

	}

	public static AbstractExpressionParser getExpressionParser(boolean useStandardConstants, Process process) {

		Object[] initargs = { new Boolean(useStandardConstants), process };
		Class[] paramTypes = { boolean.class, Process.class };
		try {
			Class<? extends AbstractExpressionParser> parserClass = (Class<? extends AbstractExpressionParser>) Class.forName(parserClassName);
			Method getParserMethod = parserClass.getDeclaredMethod("getExpressionParser", paramTypes);
			return (AbstractExpressionParser) getParserMethod.invoke(parserClass, initargs);
		} catch (ClassNotFoundException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (IllegalAccessException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (NoSuchMethodException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (SecurityException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (IllegalArgumentException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		} catch (InvocationTargetException e) {
			LogService.getRoot().log(Level.WARNING, "Could not instantiate expression parser", e);
		}
		return null;

	}

	private ExpressionParserFactory() {}

}
