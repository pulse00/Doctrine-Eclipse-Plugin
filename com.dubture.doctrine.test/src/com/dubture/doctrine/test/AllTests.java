package com.dubture.doctrine.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
	CodeGenerationTest.class,DoctrineAnnotationCodeAssistTest.class
})
public class AllTests {

}
