package com.dubture.doctrine.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dubture.doctrine.test.annotation.AnnotationCommentParserTest;
import com.dubture.doctrine.test.annotation.AnnotationParserTest;
import com.dubture.doctrine.test.text.TextSequenceUtilitiesTests;

@RunWith(Suite.class)
@SuiteClasses({ AnnotationParserTest.class, AnnotationCommentParserTest.class, CodeGenerationTest.class, DoctrineAnnotationCodeAssistTest.class,
		TextSequenceUtilitiesTests.class })
public class AllTests {

}
