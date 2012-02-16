package com.rapidminer.test.utils;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.ComparisonFailure;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.visualization.dependencies.NumericalMatrix;
import com.rapidminer.tools.math.Averagable;
import com.rapidminer.tools.math.AverageVector;

/**
 * Extension for JUnit's Assert for testing RapidMiner objects.
 * 
 * @author Simon Fischer, Marcin Skirzynski, Marius Helf
 * 
 */
public class RapidAssert extends Assert {

	public static final double DELTA = 0.000000001; 
	public static final double MAX_RELATIVE_ERROR = 0.000000001; 

	public static final AsserterRegistry ASSERTER_REGISTRY = new AsserterRegistry();


	/** 
	 * init asserter registry
	 */
	static {
		ASSERTER_REGISTRY.registerAsserter(PerformanceCriterion.class, new Asserter<PerformanceCriterion>() {
			/**
			 * Tests for equality by testing all averages, standard deviation and variances, as well as the fitness, max fitness 
			 * and example count.
			 *  
			 * @param message		message to display if an error occurs
			 * @param expected		expected criterion
			 * @param actual		actual criterion
			 */
			@Override
			public void assertEquals(String message, PerformanceCriterion expected, PerformanceCriterion actual) {
				Asserter<? super Averagable> averegableAsserter = ASSERTER_REGISTRY.getAsserterForClass(Averagable.class);
				averegableAsserter.assertEquals(message , (Averagable)expected, (Averagable)actual);
				Assert.assertEquals(message + " (fitness is not equal)", expected.getFitness(), actual.getFitness());
				Assert.assertEquals(message + " (max fitness is not equal)", expected.getMaxFitness(), actual.getMaxFitness());
				Assert.assertEquals(message + " (example count is not equal)", expected.getExampleCount(), actual.getExampleCount());
			}
		});
		
		
		ASSERTER_REGISTRY.registerAsserter(Averagable.class, new Asserter<Averagable>() {

			/**
			 * Tests for equality by testing all averages, standard deviation and variances.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected averagable
			 * @param actual		actual averagable
			 */
			@Override
			public void assertEquals(String message, Averagable expected, Averagable actual) {

				Assert.assertEquals(message + " (average is not equal)", expected.getAverage(), actual.getAverage());
				Assert.assertEquals(message + " (makro average is not equal)", expected.getMakroAverage(), actual.getMakroAverage());
				Assert.assertEquals(message + " (mikro average is not equal)", expected.getMikroAverage(), actual.getMikroAverage());
				Assert.assertEquals(message + " (average count is not equal)", expected.getAverageCount(), actual.getAverageCount());
				Assert.assertEquals(message + " (makro standard deviation is not equal)", expected.getMakroStandardDeviation(), actual.getMakroStandardDeviation());
				Assert.assertEquals(message + " (mikro standard deviation is not equal)", expected.getMikroStandardDeviation(), actual.getMikroStandardDeviation());
				Assert.assertEquals(message + " (standard deviation is not equal)", expected.getStandardDeviation(), actual.getStandardDeviation());
				Assert.assertEquals(message + " (makro variance is not equal)", expected.getMakroVariance(), actual.getMakroVariance());
				Assert.assertEquals(message + " (mikro variance is not equal)", expected.getMikroVariance(), actual.getMikroVariance());
				Assert.assertEquals(message + " (variance is not equal)", expected.getVariance(), actual.getVariance());

			}
		});


		ASSERTER_REGISTRY.registerAsserter(AverageVector.class, new Asserter<AverageVector>() {
			/**
			 * Tests the two average vectors for equality by testing the size and each averagable.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected vector
			 * @param actual		actual vector
			 */
			@Override
			public void assertEquals(String message, AverageVector expected, AverageVector actual) {
				message = message + "Average vectors are not equals";
				
				int expSize = expected.getSize();
				int actSize = actual.getSize();
				Assert.assertEquals(message + " (size of the average vector is not equal)", expSize, actSize);
				int size = expSize;

				for( int i=0; i<size; i++ ) {
					RapidAssert.assertEquals(message, expected.getAveragable(i), actual.getAveragable(i));
				}
			}


		});



		ASSERTER_REGISTRY.registerAsserter(ExampleSet.class, new Asserter<ExampleSet>() {
			/**
			 * Tests two example sets by iterating over all examples.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected value
			 * @param actual		actual value
			 */
			public void assertEquals(String message, ExampleSet expected, ExampleSet actual) {
				message = message + "ExampleSets are not equal";
				
				RapidAssert.assertEquals(message, expected.getAttributes(), actual.getAttributes());
				Assert.assertEquals(message + " (number of examples)", expected.size(), actual.size());
				Iterator<Example> i1 = expected.iterator();
				Iterator<Example> i2 = actual.iterator();
				int row = 1;
				while (i1.hasNext() && i2.hasNext()) {
					RapidAssert.assertEquals(message + "(example number " + row + ", {0} value of {1})", i1.next(), i2.next());
					row++;
				}
			}
		});


		ASSERTER_REGISTRY.registerAsserter(IOObjectCollection.class, new Asserter<IOObjectCollection<IOObject>>() {
			/**
			 * Tests the collection of ioobjects
			 * 
			 * @param expected
			 * @param actual
			 */
			@Override
			public void assertEquals(String message, IOObjectCollection expected, IOObjectCollection actual) {
				message = message + "Collection of IOObjects are not equal: ";
				Assert.assertEquals(message + " (number of items)", expected.size(), actual.size());
				RapidAssert.assertEquals(message, expected.getObjects(), actual.getObjects());
			}

		});


		ASSERTER_REGISTRY.registerAsserter(NumericalMatrix.class, new Asserter<NumericalMatrix>() {
			/**
			 * Test two numerical matrices for equality. This contains tests about the number of columns and rows, as well as column&row names and if
			 * the matrices are marked as symmetrical and if every value within the matrix is equal.
			 *  
			 * @param message		message to display if an error occurs
			 * @param expected		expected matrix
			 * @param actual		actual matrix
			 */
			@Override
			public void assertEquals(String message, NumericalMatrix expected, NumericalMatrix actual) {
				message = message + "Numerical matrices are not equal";

				int expNrOfCols = expected.getNumberOfColumns();
				int actNrOfCols = actual.getNumberOfColumns();
				Assert.assertEquals(message + " (column number is not equal)", expNrOfCols, actNrOfCols);

				int expNrOfRows = expected.getNumberOfRows();
				int actNrOfRows = actual.getNumberOfRows();
				Assert.assertEquals(message + " (row number is not equal)", expNrOfRows, actNrOfRows);

				int cols = expNrOfCols; 
				int rows = expNrOfRows;

				for( int col=0; col<cols; col++ ) {
					String expectedColName = expected.getColumnName(col);
					String actualColName = actual.getColumnName(col);
					Assert.assertEquals(message + " (column name at index "+col+" is not equal)", expectedColName, actualColName );
				}

				for( int row=0; row<rows; row++ ) {
					String expectedRowName = expected.getRowName(row);
					String actualRowName = actual.getRowName(row);
					Assert.assertEquals(message + " (row name at index "+row+" is not equal)", expectedRowName, actualRowName );
				}

				Assert.assertEquals(message + " (matrix symmetry is not equal)", expected.isSymmetrical(), actual.isSymmetrical());

				for( int row=0; row<rows; row++ ) {
					for( int col=0; col<cols; col++ ) {

						double expectedVal = expected.getValue(row, col);
						double actualVal = actual.getValue(row, col);
						Assert.assertEquals(message + " (value at row "+row+" and column "+col+" is not equal)", expectedVal, actualVal );

					}
				}

			}


		});
		
		ASSERTER_REGISTRY.registerAsserter(PerformanceVector.class, new Asserter<PerformanceVector>() {
			/**
			 * Tests the two performance vectors for equality by testing the size, the criteria names, the main criterion and each criterion.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected vector
			 * @param actual		actual vector
			 */
			@Override
			public void assertEquals(String message, PerformanceVector expected, PerformanceVector actual) {
				message = message + "Performance vectors are not equal";
				
				int expSize = expected.getSize();
				int actSize = actual.getSize();
				Assert.assertEquals(message + " (size of the performance vector is not equal)", expSize, actSize);
				int size = expSize;

				RapidAssert.assertArrayEquals(message, expected.getCriteriaNames(), actual.getCriteriaNames());
				RapidAssert.assertEquals(message, expected.getMainCriterion(), actual.getMainCriterion());

				for( int i=0; i<size; i++ ) {
					RapidAssert.assertEquals(message, expected.getCriterion(i), actual.getCriterion(i));
				}
			}
		});
	}

	/**
	 * Returns <code>true</code> if the ioobjects class is supported for
	 * comparison in the test extension and <code>false</code> otherwise.
	 */
	public static boolean comparable(IOObject ioobject) {
		return ASSERTER_REGISTRY.getAsserterForObject(ioobject) != null;
	}

	/**
	 * Returns <code>true</code> if both ioobject classes are comparable to
	 * each other and <code>false</code> otherwise.
	 */
	public static boolean comparable(IOObject ioobject1, IOObject ioobject2) {
		return ASSERTER_REGISTRY.getAsserterForObjects(ioobject1, ioobject2) != null;
	}


	/**
	 * Extends the Junit assertEquals method by additionally checking the doubles for NaN.
	 *  
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEqualsNaN(String message, double expected, double actual) {
		if (Double.isNaN(expected)) {
			if (!Double.isNaN(actual)) {
				throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
			}
		} else {
			assertEquals(message, expected, actual, DELTA);
		}
	}

	public static void assertEqualsWithRelativeErrorOrBothNaN(String message, double expected, double actual) {
		if ( expected == actual) {
			return;
		}


		if (Double.isNaN(expected) && !Double.isNaN(actual)) {
			throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
		}

		if (!Double.isNaN(expected) && Double.isNaN(actual)) {
			throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
		}

		double relativeError;
		if (Math.abs(actual) > Math.abs(expected)) {
			relativeError = Math.abs((expected - actual) / actual);
		} else {
			relativeError = Math.abs((expected - actual) / expected);
		}
		if (relativeError > MAX_RELATIVE_ERROR) {
			throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
		}
	}


	/**
	 * Tests if the special names of the attribute roles are equal and the associated attributes themselves.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, AttributeRole expected, AttributeRole actual) {
		Assert.assertEquals(message + " (attribute role)", expected.getSpecialName(), actual.getSpecialName());
		Attribute a1 = expected.getAttribute();
		Attribute a2 = actual.getAttribute();
		assertEquals(message, a1, a2);
	}


	/**
	 * Tests two attributes by using the name, type, block, type, default value and the nominal mapping
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, Attribute expected, Attribute actual) {
		Assert.assertEquals(message + " (attribute name)", expected.getName(), actual.getName());
		Assert.assertEquals(message + " (attribute type)", expected.getValueType(), actual.getValueType());
		Assert.assertEquals(message + " (attribute block type)", expected.getBlockType(), actual.getBlockType());
		Assert.assertEquals(message + " (default value)", expected.getDefault(), actual.getDefault());
		if (expected.isNominal()) {
			assertEquals(message + " (nominal mapping)", expected.getMapping(), actual.getMapping());
		}
	}

	/**
	 * Tests two nominal mappings for its size and values.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, NominalMapping expected, NominalMapping actual) {
		Assert.assertEquals(message + " (nominal mapping size)", expected.size(), actual.size());
		List<String> v1 = expected.getValues();
		List<String> v2 = actual.getValues();
		Assert.assertEquals(message + " (nominal values)", v1, v2);
		if (v1 != null) { // v2 also != null
			for (String value : v1) {
				Assert.assertEquals(message + " (index of nominal value '" + value + "')", expected.getIndex(value), actual.getIndex(value));
			}
		}
	}



	/**
	 * Tests all objects in the array.
	 * 
	 * @param expected	array with expected objects
	 * @param actual	array with actual objects
	 */
	public static void assertArrayEquals(String message, Object[] expected, Object[] actual) {
		if (expected == null) {
			junit.framework.Assert.assertEquals((Object) null, actual);
			return;
		}
		if (actual == null) {
			throw new AssertionFailedError(message + " (expected " + expected.toString() + " , but is null)");
		}
		junit.framework.Assert.assertEquals(message + " (array length is not equal)", expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			junit.framework.Assert.assertEquals(message, expected[i], actual[i]);
		}
	}

	/**
	 * Tests all objects in the array.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected	array with expected objects
	 * @param actual	array with actual objects
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual) {
		assertArrayEquals("", expected, actual);
	}

	/**
	 * Tests if both list of ioobjects are equal.
	 * 
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals( String message, List<IOObject> expected, List<IOObject> actual ) {
		assertSize(expected, actual);

		Iterator<IOObject> expectedIter = expected.iterator();
		Iterator<IOObject> actualIter = actual.iterator();

		while( expectedIter.hasNext() && actualIter.hasNext() )  {
			IOObject expectedIOO = expectedIter.next();
			IOObject actualIOO = actualIter.next();
			assertEquals(message, expectedIOO, actualIOO);
		}

	}

	/**
	 * Tests if both list of ioobjects are equal.
	 * 
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals( List<IOObject> expected, List<IOObject> actual ) {
		RapidAssert.assertEquals("", expected, actual);		
	}

	/**
	 * Tests if both lists of IOObjects have the same size.
	 * 
	 * @param expected
	 * @param actual
	 */
	public static void assertSize( List<IOObject> expected, List<IOObject> actual ) {
		assertEquals("Number of connected output ports in the process is not equal with the number of ioobjects contained in the same folder with the format 'processname-expected-port-1', 'processname-expected-port-2', ...", 
				expected.size(), actual.size());
	}

	/**
	 * Tests if the two IOObjects are equal.
	 * 
	 * @param expectedIOO
	 * @param actualIOO
	 */
	public static void assertEquals(IOObject expectedIOO, IOObject actualIOO) {
		RapidAssert.assertEquals("", expectedIOO, actualIOO);
	}

	/**
	 * Tests if the two IOObjects are equal and appends the given message.
	 * 
	 * @param expectedIOO
	 * @param actualIOO
	 */
	public static void assertEquals(String message, IOObject expectedIOO, IOObject actualIOO) {

		/*
		 * Do not forget to add a newly supported class to the 
		 * ASSERTER_REGISTRY!!!
		 */
		Asserter asserter = ASSERTER_REGISTRY.getAsserterForObjects(expectedIOO, actualIOO);
		if (asserter != null) {
			asserter.assertEquals(message, expectedIOO, actualIOO);
		} else {
			throw new ComparisonFailure("Comparison of the two given IOObject classes "+expectedIOO.getClass()+" and "+actualIOO.getClass()+" is not supported. ", expectedIOO.toString(), actualIOO.toString());
		}

	}

	/**
	 * Tests the two examples by testing the value of the examples for every given attribute. 
	 * This method is sensitive to the attribute ordering.
	 * 
	 * @param message		message to display if an error occurs. If it contains "{0}" and "{1}", it will be replaced with the attribute name and attribute type, if an unequality occurs.
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, Example expected, Example actual) {
		Iterator<Attribute> expectedAttributesToConsider = expected.getAttributes().allAttributes();
		Iterator<Attribute> actualAttributesToConsider = actual.getAttributes().allAttributes();
		while (expectedAttributesToConsider.hasNext() && actualAttributesToConsider.hasNext()) {
			Attribute a1 = expectedAttributesToConsider.next();
			Attribute a2 = actualAttributesToConsider.next();
			if (!a1.getName().equals(a2.getName())) {
				// this should have been detected by previous checks already
				throw new AssertionFailedError("Attribute ordering does not match: " + a1.getName() + "," + a2.getName());
			}
			if (a1.isNominal()) {
				Assert.assertEquals(MessageFormat.format(message, "nominal", a1.getName()), expected.getNominalValue(a1), actual.getNominalValue(a2));
			} else {
				assertEqualsWithRelativeErrorOrBothNaN(MessageFormat.format(message, "numerical", a1.getName()), expected.getValue(a1), actual.getValue(a2));
			}
		}
	}


	/**
	 * Tests if all attributes are equal. This method is sensitive to the attribute ordering.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, Attributes expected, Attributes actual) {
		Assert.assertEquals(message + " (number of attributes)", expected.allSize(), actual.allSize());
		Iterator<AttributeRole> i = expected.allAttributeRoles();
		Iterator<AttributeRole> j = expected.allAttributeRoles();
		while (i.hasNext()) {
			AttributeRole r1 = i.next();
			AttributeRole r2 = j.next();
			RapidAssert.assertEquals(message, r1, r2);
		}
	}

}


