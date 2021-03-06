/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under
 * the Apache License, Version 2.0 (the "License") you may not
 * use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package dnars.inference

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import dnars.DNarsTestUtils.TEST_KEYSPACE
import dnars.DNarsTestUtils.assertSeq
import dnars.DNarsTestUtils.assertTerms
import dnars.DNarsTestUtils.create
import dnars.DNarsTestUtils.createAndAdd
import dnars.base.Statement
import dnars.base.StatementParser
import dnars.graph.DNarsGraph
import dnars.graph.DNarsGraphFactory
import dnars.base.AtomicTerm
import dnars.base.Truth

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ResolutionEngineTest {
	var graph: DNarsGraph = null

	@Before
	def setUp(): Unit =
		graph = DNarsGraphFactory.create(TEST_KEYSPACE)

	@After
	def tearDown: Unit = {
		graph.shutdown()
		graph.clear()
	}

	@Test
	def testAnswer(): Unit = {
		createAndAdd(graph,
			"tiger -> mammal (1.0, 0.9)",
			"cat -> animal (1.0, 0.9)",
			"developer ~ job (0.87, 0.91)")
		assertAtomicAnswer("tiger -> ?", "mammal")
		assertAtomicAnswer("? -> cat", null)
		assertAtomicAnswer("? -> animal", "cat")
		assertAtomicAnswer("cat -> ?", "animal")
		assertAtomicAnswer("animal -> ?", null)
		assertAtomicAnswer("water -> ?", null)
		assertAtomicAnswer("developer ~ ?", "job")
		assertAtomicAnswer("? ~ developer", "job")
	}

	@Test
	def testMultipleAnswers(): Unit = {
		val kb = createAndAdd(graph,
			"cat -> animal (0.6, 0.3)", // expectation 0.53
			"bird -> animal (1.0, 0.9)", // expectation 0.95
			"developer ~ job (0.87, 0.91)",
			"tiger -> animal (0.6, 0.4)") // expectation 0.54

		val q = StatementParser("? -> animal (1.0, 0.9)")
		val answers = graph.answer(q, 2)
		assertTerms(List(AtomicTerm("bird"), AtomicTerm("tiger")), answers)
	}
	
	@Test
	def testEdgeReordering(): Unit = {
		val t = Truth(1.0, 0.9).revision(Truth(0.5, 0.5))
		println(s"+++++++++++ ${Truth(1.0, 0.9).expectation} ${Truth(1.0, 0.85).expectation}")
		
		val kb = createAndAdd(graph,
			"cat -> animal (1.0, 0.85)", // exp 0.925
			"bird -> animal (1.0, 0.9)" // exp 0.95
		)
		graph.add(StatementParser("bird -> animal (0.5, 0.5)")) // now 0.90
		val q = StatementParser("? -> animal (1.0, 0.9)")
		val answers = graph.answer(q, 2)
		assertTerms(List(AtomicTerm("cat"), AtomicTerm("bird")), answers)
	}

	@Test
	def testBackwardDeductionAnalogy(): Unit = {
		val kb = createAndAdd(graph,
			"cat -> animal (0.82, 0.91)",
			"water -> liquid (1.0, 0.9)",
			"tiger -> cat (0.5, 0.7)",
			"developer ~ job (1.0, 0.9)",
			"feline ~ cat (0.76, 0.83)",
			"(x einstein physics) -> field (1.0, 0.9)",
			"quantum ~ physics (1.0, 0.9)")
		val expected = create(
			"tiger -> animal " + kb(2).truth.deduction(kb(0).truth),
			"feline -> animal " + kb(0).truth.analogy(kb(4).truth, false),
			"(x einstein quantum) -> field " + kb(5).truth.analogy(kb(6).truth, false))
		assertBackwards(expected)
	}

	@Test
	def testBackwardAbductionComparisonAnalogy(): Unit = {
		val kb = createAndAdd(graph,
			"tiger -> cat (1.0, 0.9)",
			"water -> liquid (0.68, 0.39)",
			"developer -> job (0.93, 0.46)",
			"lion -> cat (0.43, 0.75)",
			"feline ~ cat (0.49, 0.52)")
		val expected = create(
			"lion -> tiger " + kb(0).truth.abduction(kb(3).truth),
			"lion ~ tiger " + kb(0).truth.comparison(kb(3).truth),
			"tiger ~ lion " + kb(0).truth.comparison(kb(3).truth),
			"tiger -> feline " + kb(0).truth.analogy(kb(4).truth, false),
			"tiger -> lion " + kb(3).truth.abduction(kb(0).truth),
			"lion -> feline " + kb(3).truth.analogy(kb(4).truth, false))
		assertBackwards(expected)
	}

	@Test
	def testBackwardAnalogyResemblance(): Unit = {
		val kb = createAndAdd(graph,
			"developer -> job (0.6, 0.77)",
			"cat ~ feline (0.33, 0.51)",
			"tiger -> cat (0.95, 0.83)",
			"water -> liquid (0.63, 0.72)",
			"lion ~ cat (0.85, 0.48)")
		val expected = create(
			"tiger -> feline " + kb(1).truth.analogy(kb(2).truth, true),
			"tiger -> lion " + kb(4).truth.analogy(kb(2).truth, true),
			"lion ~ feline " + kb(1).truth.resemblance(kb(4).truth))
		assertBackwards(expected)
	}

	@Test
	def testBackwardCompoundExtentionalDeduction(): Unit = {
		val kb = createAndAdd(graph,
			"(cat x bird) -> eat (1.0, 0.9)",
			"tiger -> cat (1.0, 0.9)")
		val expected = create(
			"(tiger x bird) -> eat " + kb(0).truth.deduction(kb(1).truth))
		assertBackwards(expected)
	}

	private def assertBackwards(expected: List[Statement]): Unit = {
		expected.foreach { q: Statement =>
			val answers = graph.backwardInference(q, Int.MaxValue)
			assertTrue("No answers for question " + q, answers.length > 0)
		}
	}

	private def assertAtomicAnswer(question: String, answer: String): Unit = {
		val st = StatementParser(question)
		val a = graph.answer(st, 1).toList
		a match {
			case List() => assertTrue(answer == null)
			case _ => a.head == AtomicTerm(answer)
		}
	}
}