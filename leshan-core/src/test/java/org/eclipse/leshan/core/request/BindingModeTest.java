package org.eclipse.leshan.core.request;

import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;

public class BindingModeTest {

	@Test
	public void testGetBindingMode() {
		EnumSet<BindingMode> baseline = EnumSet.allOf(BindingMode.class);
		EnumSet<BindingMode> testSet = BindingMode.getBindingMode(BindingMode.C, BindingMode.Q, BindingMode.S, BindingMode.T, BindingMode.U);
		assertTrue(baseline.equals(testSet));
	}
	
	@Test
	public void testGetBindingModeNotAll() {
		EnumSet<BindingMode> baseline = EnumSet.of(BindingMode.T, BindingMode.U);
		EnumSet<BindingMode> testSet = BindingMode.getBindingMode(BindingMode.T, BindingMode.U);
		assertTrue(baseline.equals(testSet));
	}
	
	@Test
	public void testGetBindingModeOne() {
		EnumSet<BindingMode> baseline = EnumSet.of(BindingMode.U);
		EnumSet<BindingMode> testSet = BindingMode.getBindingMode(BindingMode.U);
		assertTrue(baseline.equals(testSet));
	}

	@Test
	public void testSetToString() {
		String baseline = "USQTC";
		EnumSet<BindingMode> testSet = EnumSet.allOf(BindingMode.class);
		String testString = BindingMode.setToString(testSet);
		assertTrue(baseline.equals(testString));
	}
	
	@Test
	public void testSetToStringNotAll() {
		String baseline = "UC";
		EnumSet<BindingMode> testSet = EnumSet.of(BindingMode.U, BindingMode.C);
		String testString = BindingMode.setToString(testSet);
		assertTrue(baseline.equals(testString));
	}
	
	@Test
	public void testSetToStringOne() {
		String baseline = "U";
		EnumSet<BindingMode> testSet = EnumSet.of(BindingMode.U);
		String testString = BindingMode.setToString(testSet);
		assertTrue(baseline.equals(testString));
	}

	@Test
	public void testParseFromString() {
		EnumSet<BindingMode> baseline = EnumSet.allOf(BindingMode.class);
		String testString = "USQTC";
		EnumSet<BindingMode> testSet = BindingMode.parseFromString(testString);
		assertTrue(baseline.equals(testSet));
	}
	
	@Test
	public void testParseFromStringNotAll() {
		EnumSet<BindingMode> baseline = EnumSet.of(BindingMode.U, BindingMode.Q);
		String testString = "UQ";
		EnumSet<BindingMode> testSet = BindingMode.parseFromString(testString);
		assertTrue(baseline.equals(testSet));
	}
	
	@Test
	public void testParseFromStringOne() {
		EnumSet<BindingMode> baseline = EnumSet.of(BindingMode.U);
		String testString = "U";
		EnumSet<BindingMode> testSet = BindingMode.parseFromString(testString);
		assertTrue(baseline.equals(testSet));
	}

}
