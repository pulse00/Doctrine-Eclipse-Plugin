package com.dubture.doctrine.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dubture.doctrine.test.text.TextSequenceUtilitiesTests;


@RunWith(Suite.class)
@SuiteClasses({
	CodeGenerationTest.class,DoctrineAnnotationCodeAssistTest.class, TextSequenceUtilitiesTests.class
})
public class AllTests {

}
