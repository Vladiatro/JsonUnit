/**
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.test.base;

import net.javacrumbs.jsonunit.assertj.JsonAssert.ConfigurableJsonAssert;
import net.javacrumbs.jsonunit.assertj.JsonSoftAssertions;
import net.javacrumbs.jsonunit.core.Option;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.MultipleFailuresError;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.value;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static net.javacrumbs.jsonunit.core.internal.JsonUtils.jsonSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.Matchers.greaterThan;

public abstract class AbstractAssertJTest {

    @Test
    void demo() {
        // Both actual value (in the left) and expected value (in the right) are parsed as JSON
        assertThatJson("{\"root\":{\"a\":1}}").node("root").isEqualTo("{a:1}");

        // Works with arrays too
        assertThatJson("{\"root\":[{\"a\":1}]}").node("root").isEqualTo("[{a:1}]");

        // Strings passed to AssertJ methods like containsExactly are parsed as JSON too
        assertThatJson("{\"root\":[{\"a\":1}]}").node("root").isArray().containsExactly("{a:1}");

        // Primitive boolean? No problem
        assertThatJson("{\"root\":[true]}").node("root").isArray().containsExactly(true);

        // Boolean in string? Tricky, "true" is valid JSON so it gets parsed to primitive `true`
        // Have to wrap it to JsonAssertions.value() in order to make sure it's not parsed
        assertThatJson("{\"root\":[\"true\"]}").node("root").isArray().containsExactly(value("true"));
    }

    @Test
    void shouldAssertSimple() {
        assertThatJson("{\"a\":1, \"b\":2}").isEqualTo("{\"b\":2, \"a\":1}");
    }


    @Test
    void shouldAssertLenient() {
        assertThatJson("{\"a\":\"1\", \"b\":2}").isEqualTo("{b:2, a:'1'}");
    }

    @Test
    void shouldAssertObject() {
        assertThatJson("{\"a\":1}").isObject().containsEntry("a", valueOf(1));
    }

    @Test
    void objectShouldContainKeys() {
        assertThatJson("{\"a\":1, \"b\": 2}").isObject().containsKeys("a", "b");
    }

    @Test
    void objectShouldContainValue() {
        assertThatJson("{\"a\":1, \"b\": 2}").isObject().containsValue(valueOf(2));
    }

    @Test
    void objectShouldContainComplexValue() {
        assertThatJson("{\"a\":1, \"b\": {\"c\" :3}}").isObject().containsValue(json("{\"c\" :\"${json-unit.any-number}\"}"));
    }

    @Test
    void objectShouldContainComplexValueError() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"a\":1, \"b\": {\"c\" :3}}}")
            .node("root")
            .isObject()
            .containsValue(json("{\"c\" :5}")))
            .hasMessage("[Different value found in node \"root\"] \n" +
                "Expecting:\n" +
                "  <{\"a\":1,\"b\":{\"c\":3}}>\n" +
                "to contain value:\n" +
                "  <{\"c\":5}>");
    }


    @Test
    void objectShouldContainComplexValueErrorSoftly() {
        JsonSoftAssertions soft = new JsonSoftAssertions();

        soft.assertThatJson("{\"root\":{\"a\":1, \"b\": {\"c\" :3}}}")
            .node("root.b")
            .isEqualTo(json("{\"c\" :5}"));

        assertThatThrownBy(soft::assertAll)
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"root.b.c\", \n" +
                "Expected :5\n" +
                "Actual   :3\n");
    }

    @Test
    void testSoftAssert() {
        SoftAssertions soft = new SoftAssertions();

        soft.assertThat("string").usingComparator(Comparator.naturalOrder()).isEqualTo("string2");

        assertThatThrownBy(soft::assertAll).isInstanceOf(MultipleFailuresError.class);
    }

    @Test
    void objectDoesContainComplexValue() {
        assertThatJson("{\"a\":1, \"b\": {\"c\" :3}}").isObject().doesNotContainValue(json("{\"c\" :\"${json-unit.any-string}\"}"));
    }


    @Test
    void objectDoesContainComplexValueError() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"a\":1, \"b\": {\"c\" :3}}}")
            .node("root")
            .isObject()
            .doesNotContainValue(json("{\"c\" :3}")))
            .hasMessage("[Different value found in node \"root\"] \n" +
                "Expecting:\n" +
                "  <{\"a\":1,\"b\":{\"c\":3}}>\n" +
                "not to contain value:\n" +
                "  <{\"c\":3}>");
    }

    @Test
    void compareJsonInJsonPathArray() {
        assertThatJson("{\"root\": [{\"target\": 450} ]}")
                        .inPath("$.root")
                        .isArray()
                        .containsExactly("{target: 450 }");
    }

    @Test
    void compareJsonInJsonPathShallowArray() {
        assertThatJson("{\"root\": [450]}")
                        .inPath("$.root")
                        .isArray()
                        .containsExactly(json("450"));
    }

    @Test
    void compareJsonInJsonPathShallowArrayString() {
        assertThatThrownBy(() -> assertThatJson("{\"root\": [450]}")
                        .inPath("$.root")
                        .isArray()
                        .containsExactly(value("450")))
            .hasMessage("[Different value found in node \"$.root\"] \n" +
                "Expecting:\n" +
                "  <[450]>\n" +
                "to contain exactly (and in same order):\n" +
                "  <[\"450\"]>\n" +
                "but some elements were not found:\n" +
                "  <[\"450\"]>\n" +
                "and others were not expected:\n" +
                "  <[450]>\n" +
                "when comparing values using JsonComparator");
    }

    @Test
    void compareJsonInPathArrayOfArrays() {
        assertThatJson("{\"root\": [[{\"target\": 450} ]]}")
                        .inPath("$.root")
                        .isArray()
                        .containsExactly("[{target: 450 }]");
    }

    @Test
    void compareJsonInNodeArray() {
        assertThatJson("{\"root\": [{\"target\": 450} ]}")
                        .node("root")
                        .isArray()
                        .containsExactly("{target: 450 }");
    }

    @Test
    void compareJsonInNodeShallowArray() {
        assertThatJson("{\"root\": [450]}")
                        .node("root")
                        .isArray()
                        .containsExactly(valueOf(450));
    }

    @Test
    void compareJsonInNodeShallowArrayBigDecimal() {
        assertThatJson("{\"root\": [450]}")
                        .node("root")
                        .isArray()
                        .containsExactly(valueOf(450));
    }

    @Test
    void compareJsonInNodeShallowArrayBoolean() {
        assertThatJson("{\"root\": [true]}")
                        .node("root")
                        .isArray()
                        .containsExactly(true);
    }

    @Test
    void compareJsonInNodeArrayOfArrays() {
        assertThatJson("{\"root\": [[{\"target\": 450} ]]}")
                        .node("root")
                        .isArray()
                        .containsExactly("[{target: 450 }]");
    }

    @Test
    void compareJsonArray() {
        assertThatJson("{\"root\": [{\"target\": 450} ]}")
                        .node("root")
                        .isEqualTo(singletonList(singletonMap("target", 450)));
    }

    @Test
    void shouldAssertDirectEqual() {
        assertThatJson("{\"a\":1}").isEqualTo(json("{'a':'${json-unit.ignore}'}"));
    }

    @Test
    void shouldAssertObjectJson() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}").node("a").isObject().isEqualTo(json("{\"b\": 2}")))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"a.b\", expected: <2> but was: <1>.\n");
    }

    @Test
    void shouldAssertContainsEntry() {
        assertThatJson("{\"a\":{\"b\": 1}}").node("a").isObject().contains(entry("b", valueOf(1)));
    }

    @Test
    void shouldAssertContainsJsonError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}").node("a").isObject().contains(entry("b", valueOf(2))))
            .hasMessage("[Different value found in node \"a\"] \n" +
                "Expecting:\n" +
                " <{\"b\":1}>\n" +
                "to contain:\n" +
                " <[MapEntry[key=\"b\", value=2]]>\n" +
                "but could not find:\n" +
                " <[MapEntry[key=\"b\", value=2]]>\n");
    }

    @Test
    void shouldAssertContainsOnlyKeys() {
        assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}").node("a").isObject().containsOnlyKeys("b", "c");
    }

    @Test
    void shouldAssertContainsOnlyKeysError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}").node("a").isObject().containsOnlyKeys("b", "c", "d"))
            .hasMessage("[Different value found in node \"a\"] \n" +
                "Expecting:\n" +
                "  <{\"b\":1,\"c\":true}>\n" +
                "to contain only following keys:\n" +
                "  <[\"b\", \"c\", \"d\"]>\n" +
                "but could not find the following keys:\n" +
                "  <[\"d\"]>\n");
    }

    @Test
    void shouldAssertContainsAllEntries() {
        assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}").node("a").isObject().containsAllEntriesOf(singletonMap("c", true));
    }

    @Test
    void shouldAssertContainsAllEntriesError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1, \"c\": true}}").node("a").isObject().containsAllEntriesOf(singletonMap("c", false)))
            .hasMessage("[Different value found in node \"a\"] \n" +
                "Expecting:\n" +
                " <{\"b\":1,\"c\":true}>\n" +
                "to contain:\n" +
                " <[c=false]>\n" +
                "but could not find:\n" +
                " <[c=false]>\n");
    }

    @Test
    void shouldAssertJson() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}").node("a").isEqualTo(json("{\"b\": 2}")))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"a.b\", expected: <2> but was: <1>.\n");
    }

    @Test
    void shouldAssertObjectJsonWithPlaceholder() {
        assertThatJson("{\"a\":{\"b\": \"ignored\"}}").node("a").isObject().isEqualTo(json("{'b':'${json-unit.any-string}'}"));
    }

    @Test
    void shouldAssertObjectIsNotEqualToJsonWithPlaceholder() {
        assertThatJson("{\"a\":{\"b\": 1}}").node("a").isObject().isNotEqualTo(json("{'b':'${json-unit.any-string}'}"));
    }

    @Test
    void shouldAssertObjectIsNotEqualToJsonWithPlaceholderError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": \"string\"}}").node("a").isObject().isNotEqualTo(json("{'b':'${json-unit.any-string}'}")))
            .hasMessage("[Different value found in node \"a\"] \n" +
                "Expecting:\n" +
                " <{\"b\":\"string\"}>\n" +
                "not to be equal to:\n" +
                " <{\"b\":\"${json-unit.any-string}\"}>\n" +
                "when comparing values using JsonComparator");
    }

    @Test
    void shouldAssertObjectJsonWithPlaceholderFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}").node("a").isObject().isEqualTo(json("{'b':'${json-unit.any-string}'}")))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"a.b\", expected: <a string> but was: <1>.\n");
    }

    @Test
    void shouldAssertString() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": \"foo\"}}").node("a.b").isString().startsWith("bar"))
            .hasMessage("[Different value found in node \"a.b\"] \n" +
                "Expecting:\n" +
                " <\"foo\">\n" +
                "to start with:\n" +
                " <\"bar\">\n");
    }

    @Test
    void shouldAssertStringCustomDescription() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": \"foo\"}}").node("a.b").isString().as("Sad!").startsWith("bar"))
            .hasMessage("[Sad!] \n" +
                "Expecting:\n" +
                " <\"foo\">\n" +
                "to start with:\n" +
                " <\"bar\">\n");
    }

    @Test
    void shouldAssertArray() {
        assertThatJson("{\"a\":[1, 2, 3]}").node("a").isArray().contains(valueOf(3));
    }

    @Test
    void shouldFindObjectInArray() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").node("a").isArray().contains(json("{\"c\": 1}"));
    }

    @Test
    void shouldFindObjectInArrayWithPlaceholder() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").node("a").isArray().contains(json("{\"c\": \"${json-unit.any-number}\"}"));
    }

    @Test
    void arrayIgnoringOrderComparison() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").node("a").isArray()
            .containsExactlyInAnyOrder(json("{\"c\": 1}"), json("{\"b\": 1}"), json("{\"d\": 1}"));
    }

    @Test
    void arraySimpleIgnoringOrderComparison() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").when(Option.IGNORING_ARRAY_ORDER).node("a").isArray()
            .isEqualTo(json("[{\"c\": 1}, {\"b\": 1} ,{\"d\": 1}]"));
    }

    @Test
    void arraySimpleIgnoringOrderComparisonError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").when(Option.IGNORING_ARRAY_ORDER).node("a").isArray()
            .isEqualTo(json("[{\"c\": 2}, {\"b\": 1} ,{\"d\": 1}]")))
            .hasMessage("JSON documents are different:\n" +
                "Different value found when comparing expected array element a[0] to actual element a[1].\n" +
                "Different value found in node \"a[1].c\", expected: <2> but was: <1>.\n");
    }

    @Test
    void arraySimpleIgnoringOrderNotEqualComparison() {
        assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").when(Option.IGNORING_ARRAY_ORDER).node("a").isArray()
            .isNotEqualTo(json("[{\"c\": 2}, {\"b\": 1} ,{\"d\": 1}]"));
    }

    @Test
    void arraySimpleIgnoringOrderNotEqualComparisonError() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":[{\"b\": 1}, {\"c\": 1}, {\"d\": 1}]}").when(Option.IGNORING_ARRAY_ORDER).node("a").isArray()
            .isNotEqualTo(json("[{\"c\": 1}, {\"b\": 1} ,{\"d\": 1}]")))
            .hasMessage("[Different value found in node \"a\"] \n" +
                "Expecting:\n" +
                " <[{\"b\":1}, {\"c\":1}, {\"d\":1}]>\n" +
                "not to be equal to:\n" +
                " <[{\"c\":1},{\"b\":1},{\"d\":1}]>\n" +
                "when comparing as JSON with [IGNORING_ARRAY_ORDER]");
    }

    @Test
    void shouldAssertBoolean() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": true}}").node("a.b").isBoolean().isFalse())
            .hasMessage("[Different value found in node \"a.b\"] \n" +
                "Expecting:\n" +
                " <true>\n" +
                "to be equal to:\n" +
                " <false>\n" +
                "but was not.");
    }


    @Test
    void shouldAssertNull() {
        assertThatJson("{\"a\":{\"b\": null}}").node("a.b").isNull();
    }

    @Test
    void shouldAssertNullFail() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}").node("a.b").isNull())
            .hasMessage("Node \"a.b\" has invalid type, expected: <null> but was: <1>.");
    }

    @Test
    void shouldAssertNotNull() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": null}}").node("a.b").isNotNull())
            .hasMessage("Node \"a.b\" has invalid type, expected: <not null> but was: <null>.");
    }

    @Test
    void shouldAssertNotNullChain() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": null}}").node("a").isPresent().node("b").isNotNull())
            .hasMessage("Node \"a.b\" has invalid type, expected: <not null> but was: <null>.");
    }

    @Test
    void shouldAssertNotNullChaining() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}").node("a").isNotNull().node("b").isNumber().isEqualByComparingTo("2"))
            .hasMessage("[Different value found in node \"a.b\"] \n" +
                "Expecting:\n" +
                " <1>\n" +
                "to be equal to:\n" +
                " <2>\n" +
                "but was not.");
    }

    @Test
    void shouldAssertNotNullChainingSuccess() {
        assertThatJson("{\"a\":{\"b\": 1}}")
            .node("a")
            .isNotNull()
            .node("b")
            .isNumber()
            .isEqualByComparingTo("1");
    }

    @Test
    void canNotConfigureAfterAssertion() {
        // do not want to allow this
        //assertThatJson("[1, 2]").isEqualTo("[2, 1]").when(IGNORING_ARRAY_ORDER);
    }

    @Test
    void shouldAssertNotNullMissing() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": null}}").node("a.c").isNotNull())
            .hasMessage("Different value found in node \"a.c\", expected: <not null> but was: <missing>.");
    }

    @Test
    void shouldAssertObjectFailure() {
        assertThatThrownBy(() -> assertThatJson("true").isObject())
            .hasMessage("Node \"\" has invalid type, expected: <object> but was: <true>.");
    }

    @Test
    void shouldAssertNumber() {
        assertThatJson("{\"a\":1}").node("a").isNumber().isEqualByComparingTo("1");
    }

    @Test
    void shouldAssertAsNumber() {
        assertThatJson("{\"a\":1}").node("a").asNumber().isEqualByComparingTo("1");
    }

    @Test
    void shouldAssertStringNumber() {
        assertThatJson("{\"a\":\"1\"}").node("a").asNumber().isEqualByComparingTo("1");
    }

    @Test
    void shouldAssertStringNumberFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":\"x\"}").node("a").asNumber().isEqualByComparingTo("1"))
            .hasMessage("Node \"a\" can not be converted to number expected: <a number> but was: <\"x\">.");
    }

    @Test
    void shouldAssertNumberFailure() {
        assertThatThrownBy(() ->  assertThatJson("{\"a\":1}").node("a").isNumber().isEqualByComparingTo("2"))
            .hasMessage("[Different value found in node \"a\"] \n" +
                "Expecting:\n" +
                " <1>\n" +
                "to be equal to:\n" +
                " <2>\n" +
                "but was not.");
    }

    @Test
    void testAssertToleranceSimple() {
        assertThatJson("{\"test\":1.00001}").withTolerance(0.001).isEqualTo("{\"test\":1}");
    }

    @Test
    void testAssertTolerance() {
        assertThatJson("{\"test\":1.00001}").withConfiguration(c -> c.withTolerance(0.001)).isEqualTo("{\"test\":1}");
    }

    @Test
    public void shouldAllowUnquotedKeysAndCommentInExpectedValue() {
        assertThatJson("{\"test\":1}").isEqualTo("{//comment\ntest:1}");
    }

    @Test
    void testAssertNode() {
        assertThatThrownBy(() -> assertThatJson(readValue("{\"test\":1}")).isEqualTo(readValue("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertNodeInExpectOnly() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isEqualTo(readValue("{\"test\":2}")))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testAssertPathWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$")).node("test").isEqualTo("2"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"$.test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testPresentWithDescription() {
        assertThatThrownBy(() -> assertThatJson(jsonSource("{\"test\":1}", "$")).node("test2").isPresent())
            .hasMessage("Different value found in node \"$.test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void testNotEqualTo() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").isNotEqualTo("{\"test\": \"${json-unit.any-number}\"}"))
            .hasMessage("\n" +
                "Expecting:\n" +
                " <{\"test\":1}>\n" +
                "not to be equal to:\n" +
                " <\"{\"test\": \"${json-unit.any-number}\"}\">\n" +
                "when comparing values using JsonComparator");

    }

    @Test
    void testAssertPathArray() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[0]").isEqualTo(2))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"root.test[0]\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testNodeAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isAbsent())
            .hasMessage("Different value found in node \"test2\", expected: <node to be absent> but was: <1>.");
    }

    @Test
    void testNodeAbsentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isAbsent();
    }

    @Test
    void shouldTreatNullAsAbsent() {
        assertThatJson("{\"a\":1, \"b\": null}").when(Option.TREATING_NULL_AS_ABSENT).node("b").isAbsent();
    }

    @Test
    void shouldNotTreatNullAsAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":1, \"b\": null}").node("b").isAbsent())
            .hasMessage("Different value found in node \"b\", expected: <node to be absent> but was: <null>.");
    }

    @Test
    void testNodePresent() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":1}").node("test3").isPresent())
            .hasMessage("Different value found in node \"test3\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void testNodePresentOk() {
        assertThatJson("{\"test1\":2, \"test2\":1}").node("test2").isPresent();
    }

    @Test
    void testNodePresentNull() {
        assertThatJson("{\"test1\":2, \"test2\":null}").node("test2").isPresent();
    }

    @Test
    void isPresentShouldTreatNullAsAbsentWhenSpecified() {
        assertThatThrownBy(() -> assertThatJson("{\"test1\":2, \"test2\":null}").when(Option.TREATING_NULL_AS_ABSENT).node("test2").isPresent())
            .hasMessage("Different value found in node \"test2\", expected: <node to be present> but was: <missing>.");
    }

    @Test
    void testMessage() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").as("Test is different").isEqualTo("{\"test\":2}"))
            .hasMessage("[Test is different] JSON documents are different:\nDifferent value found in node \"test\", expected: <2> but was: <1>.\n");
    }

    @Test
    void testIgnoreDifferent() {
        assertThatJson("{\"test\":1}").withConfiguration(c -> c.withIgnorePlaceholder("##IGNORE##")).isEqualTo("{\"test\":\"##IGNORE##\"}");
    }

    @Test
    void anyNumberShouldFailOnNull() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":null}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <null>.\n");
    }

    @Test
    void anyNumberShouldFailOnObject() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1}}").isEqualTo("{\"test\":\"${json-unit.any-number}\"}"))
            .hasMessage("JSON documents are different:\nDifferent value found in node \"test\", expected: <a number> but was: <{\"a\":1}>.\n");
    }

    @Test
    void anyBooleanShouldAcceptTrue() {
        assertThatJson("{\"test\":true}").isEqualTo("{\"test\":\"${json-unit.any-boolean}\"}");
    }

    @Test
    void emptinessCheck() {
        assertThatJson("{\"test\":{}}").node("test").isEqualTo("{}");
    }

    @Test
    void emptinessCheck2() {
        assertThatJson("{\"test\":{}}").node("test").isObject().isEmpty();
    }

    @Test
    void ifMatcherDoesNotMatchReportDifference() {
        RecordingDifferenceListener listener = new RecordingDifferenceListener();
        assertThatThrownBy(() -> assertThatJson("{\"test\":-1}")
            .withMatcher("positive", greaterThan(valueOf(0)))
            .withDifferenceListener(listener)
            .isEqualTo("{\"test\": \"${json-unit.matches:positive}\"}"))
            .hasMessage("JSON documents are different:\nMatcher \"positive\" does not match value -1 in node \"test\". <-1> was less than <0>\n");

        assertThat(listener.getDifferenceList()).hasSize(1);
        assertThat(listener.getDifferenceList().get(0).toString()).isEqualTo("DIFFERENT Expected ${json-unit.matches:positive} in test got -1 in test");
    }

    @Test
    void shoulEscapeDot() {
        assertThatJson("{\"name.with.dot\": \"value\"}").node("name\\.with\\.dot").isEqualTo("value");
    }

    @Test
    void pathShouldBeIgnoredForExtraKey() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
            .withConfiguration(c -> c.whenIgnoringPaths("root.ignored"))
            .isEqualTo("{\"root\":{\"test\":1}}");
    }

    @Test
    void withFailMessageShouldWork() {
        assertThatThrownBy(() -> assertThatJson("{\"a\":{\"b\": 1}}")
            .withFailMessage("It's broken")
            .isEqualTo("{\"b\": 2}")
        ).hasMessage("It's broken");
    }

    @Test
    void pathShouldBeIgnoredForDifferentValue() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
            .whenIgnoringPaths("root.ignored")
            .isEqualTo("{\"root\":{\"test\":1, \"ignored\": 2}}");
    }

    @Test
    void testEqualsToArray() {
        assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo(new int[]{1, 2, 3});
    }

    @Test
    void testEqualsToDoubleArray() {
        assertThatJson("{\"test\":[1.0,2.0,3.0]}").node("test").isEqualTo(new double[]{1, 2, 3});
    }

    @Test
    void isArrayShouldFailIfArrayDoesNotExist() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test2").isArray())
            .hasMessage("Different value found in node \"test2\", expected: <array> but was: <missing>.");
    }

    @Test
    void isArrayShouldFailIfItIsNotArray() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":\"1\"}").node("test").isArray())
            .hasMessage("Node \"test\" has invalid type, expected: <array> but was: <\"1\">.");
    }

    @Test
    void arrayOfLengthShouldFailOnIncorrectSize() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").isArray().hasSize(2))
            .hasMessage("[Different value found in node \"test\"] \n" +
                "Expected size:<2> but was:<3> in:\n" +
                "<[1, 2, 3]>");
    }

    @Test
    void shouldReportExtraArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").node("test").isEqualTo("[1]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content. Extra values: [2, 3], expected: <[1]> but was: <[1,2,3]>\n");
    }

    @Test
    void shouldReportExtraArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1,2,3]}").when(IGNORING_ARRAY_ORDER).node("test").isEqualTo("[1]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content. Missing values: [], extra values: [2, 3], expected: <[1]> but was: <[1,2,3]>\n");
    }

    @Test
    void shouldReportMissingArrayItemsWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}").node("test").isEqualTo("[1, 2, 3]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <3> but was: <1>.\n" +
                "Array \"test\" has different content. Missing values: [2, 3], expected: <[1,2,3]> but was: <[1]>\n");
    }

    @Test
    void shouldReportMissingArrayItemsWhenIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[1]}").when(IGNORING_ARRAY_ORDER).node("test").isEqualTo("[1, 2, 3]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <3> but was: <1>.\n" +
                "Array \"test\" has different content. Missing values: [2, 3], extra values: [], expected: <[1,2,3]> but was: <[1]>\n");
    }

    @Test
    void shouldReportExtraArrayItemsAndDifferencesWhenNotIgnoringOrder() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[\"x\",\"b\",\"c\"]}").node("test").isEqualTo("[\"a\"]"))
            .hasMessage("JSON documents are different:\n" +
                "Array \"test\" has different length, expected: <1> but was: <3>.\n" +
                "Array \"test\" has different content. Extra values: [\"b\", \"c\"], expected: <[\"a\"]> but was: <[\"x\",\"b\",\"c\"]>\n" +
                "Different value found in node \"test[0]\", expected: <\"a\"> but was: <\"x\">.\n");
    }

    @Test
    void negativeArrayIndexShouldCountBackwards() {
        assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-1]").isEqualTo(3);
    }

    @Test
    void negativeArrayIndexShouldCountBackwardsAndReportFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-3]").isEqualTo(3))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"root.test[-3]\", expected: <3> but was: <1>.\n");
    }

    @Test
    void negativeArrayIndexOutOfBounds() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[-5]").isEqualTo(3))
            .hasMessage("JSON documents are different:\n" +
                "Missing node in path \"root.test[-5]\".\n");
    }

    @Test
    void positiveArrayIndexOutOfBounds() {
        assertThatThrownBy(() -> assertThatJson("{\"root\":{\"test\":[1,2,3]}}").node("root.test[5]").isEqualTo(3))
            .hasMessage("JSON documents are different:\n" +
                "Missing node in path \"root.test[5]\".\n");
    }

    @Test
    void arrayThatContainsShouldFailOnMissingNode() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":[{\"id\":36},{\"id\":37},{\"id\":38}]}").node("test").isArray().contains("{\"id\":42}"))
            .hasMessage("[Different value found in node \"test\"] \n" +
                "Expecting:\n" +
                " <[{\"id\":36}, {\"id\":37}, {\"id\":38}]>\n" +
                "to contain:\n" +
                " <[\"{\"id\":42}\"]>\n" +
                "but could not find:\n" +
                " <[\"{\"id\":42}\"]>\n" +
                "when comparing values using JsonComparator");
    }

    @Test
    void testContains() {
        assertThatJson("[\"foo\", \"bar\", null, false, 1]").isArray().hasSize(5).contains("foo", "bar", null, false, valueOf(1));
    }

    @Test
    void arrayContainsShouldMatch() {
        assertThatJson("[{\"a\": 7}, 8]").isArray().containsAnyOf(json("{\"a\": \"${json-unit.any-number}\"}"));
    }

    @Test
    void shouldNotParseValueTwice() {
        assertThatJson("{\"json\": \"{\\\"a\\\" : 1}\"}").node("json").isString().isEqualTo("{\"a\" : 1}");
    }


    @Test
    void testArrayShouldMatchRegardlessOfOrder() {

        final String actual = "{\"response\":[{\"attributes\":null,\"empolyees\":[{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"}],\"id\":123}]}";
        final String expected = "{\"response\":[{\"attributes\":null,\"empolyees\":[{\"dob\":\"1985-01-11\",\"firstName\":\"Kate\",\"lastName\":\"Smith\"},{\"dob\":\"1986-02-12\",\"firstName\":\"Jason\",\"lastName\":\"Kowalski\"},{\"dob\":\"1987-03-21\",\"firstName\":\"Joe\",\"lastName\":\"Doe\"}],\"id\":123}]}";

        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }


    @Test
    void objectShouldMatch() {
        assertThatJson("{\"test\":[{\"value\":1},{\"value\":2},{\"value\":3}]}").node("test").isArray().allSatisfy(v -> assertThatJson(v).node("value").isNumber().isLessThan(valueOf(4)));
    }

    @Test
    void isStringShouldFailIfItIsNotAString() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":1}").node("test").isString())
            .hasMessage("Node \"test\" has invalid type, expected: <string> but was: <1>.");
    }

    @Test
    void isStringEqualToShouldPass() {
        assertThatJson("{\"test\":\"1\"}").node("test").isString().isEqualTo("1");
    }

    @Test
    void testIssue3SpaceStrings() {
        assertThatJson("{\"someKey\":\"a b\"}").node("someKey").isEqualTo("a b");
    }

    @Test
    void testTreatNullAsAbsent() {
        assertThatJson("{\"test\":{\"a\":1, \"b\": null}}").when(TREATING_NULL_AS_ABSENT).isEqualTo("{\"test\":{\"a\":1}}");
    }

    @Test
    void shouldIgnoreExtraFields() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").when(IGNORING_EXTRA_FIELDS).isEqualTo("{\"test\":{\"b\":2}}");
    }

    @Test
    void andFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").and(
            a -> a.node("test").isObject(),
            a -> a.node("test.b").isEqualTo(3)
        )).hasMessage("JSON documents are different:\n" +
            "Different value found in node \"test.b\", expected: <3> but was: <2>.\n");
    }

    @Test
    void andSucess() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").and(
            a -> a.node("test").isObject(),
            a -> a.node("test.b").isEqualTo(2)
        );
    }

    @Test
    void andNestedFailure() {
        assertThatThrownBy(() -> assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").node("test").and(
            a -> a.node("a").isEqualTo(1),
            a -> a.node("b").isEqualTo(3)
        )).hasMessage("JSON documents are different:\n" +
            "Different value found in node \"test.b\", expected: <3> but was: <2>.\n");
    }

    @Test
    void andNestedSucess() {
        assertThatJson("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}").node("test").and(
            a -> a.node("a").isEqualTo(1),
            a -> a.node("b").isEqualTo(2)
        );
    }

    @Test
    void shouldAcceptEscapedPath() {
        assertThatJson("{\"foo.bar\":\"baz\"}").node("foo\\.bar").isEqualTo("baz");
    }

    @Test
    void shouldAcceptEscapedPathWithTwoDots() {
        assertThatJson("{\"foo.bar.baz\":\"baz\"}").node("foo\\.bar\\.baz").isEqualTo("baz");
    }

    @Test
    void shouldAcceptEscapedPathAndShowCorrectErrorMessage() {
        assertThatThrownBy(() -> assertThatJson("{\"foo.bar\":\"boo\"}").node("foo\\.bar").isEqualTo("baz"))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"foo\\.bar\", expected: <\"baz\"> but was: <\"boo\">.\n");
    }

    @Test
    void testCompareArrays() {
        assertThatJson("[{\"b\": 10}]")
            .isArray()
            .isEqualTo(json("[{\"b\": 10}]"));
    }

    @Test
    void asStringShouldWork() {
        String json = "{\"myNum\": \"1140.53\"}";
        assertThatJson(json).node("myNum").asString().isEqualTo("1140.53");
    }

    @Test
    void shouldWorkWithPercentSign() {
       assertThatThrownBy(() -> assertThatJson("{\"a\": \"1\"}").isEqualTo("{\"%\": \"2\"}"))
           .hasMessage("JSON documents are different:\n" +
               "Different keys found in node \"\", missing: \"%\", extra: \"a\", expected: <{\"%\":\"2\"}> but was: <{\"a\":\"1\"}>\n");
    }

    @Test
    void shouldIgnoreFields() {
        assertThatJson("{\"root\":{\"test\":1, \"ignored\": 1}}")
               .isObject()
               .isEqualToIgnoringGivenFields("{\"root\":{\"test\":1, \"ignored\": 2}}", "root.ignored");

    }

    @Test
    void arrayWithStringBooleansShouldBeComparable() {
        assertThatJson("{\"array\": [\"true\"]}")
            .node("array")
            .isArray()
            .containsExactly(value("true"));
    }

    // *****************************************************************************************************************
    // ********************************************** JSON Path ********************************************************
    // *****************************************************************************************************************
    @Test
    void jsonPathShouldBeAbleToUseObjects() {
        assertThatThrownBy(() -> assertThatJson(json)
            .describedAs("My little assert")
            .inPath("$.store.book[0]")
            .isEqualTo(
                "            {\n" +
                    "                \"category\": \"reference\",\n" +
                    "                \"author\": \"Nigel Rees\",\n" +
                    "                \"title\": \"Sayings of the Century\",\n" +
                    "                \"price\": 8.96\n" +
                    "            }"
            ))
            .hasMessage("JSON documents are different:\n" +
                "Different value found in node \"$.store.book[0].price\", expected: <8.96> but was: <8.95>.\n");
    }

    @Test
    void jsonPathWithIgnoredPaths() {
        assertThatJson(json)
            .withConfiguration(c -> c.whenIgnoringPaths("$.store.book[*].price"))
            .inPath("$.store.book[0]")
            .isEqualTo(
                "            {\n" +
                    "                \"category\": \"reference\",\n" +
                    "                \"author\": \"Nigel Rees\",\n" +
                    "                \"title\": \"Sayings of the Century\",\n" +
                    "                \"price\": 999\n" +
                    "            }"
            );
    }

    @Test
    void jsonPathWithNode() {
        assertThatJson(json)
            .inPath("$.store.book[0]")
            .node("title")
            .isEqualTo("Sayings of the Century");
    }

    @Test
    void jsonPathWithNodeError() {
        assertThatThrownBy(() ->  assertThatJson(json)
            .inPath("$.store.book[0]")
            .node("title")
            .isEqualTo("Sayings of the Century2")
        ).hasMessage("JSON documents are different:\nDifferent value found in node \"$.store.book[0].title\", expected: <\"Sayings of the Century2\"> but was: <\"Sayings of the Century\">.\n");
    }

    @Test
    void jsonPatNumber() {
        assertThatJson(json)
            .inPath("$..book.length()")
            .isArray()
            .containsExactly(valueOf(4));
    }

    @Test
    void booleanArrayExtractionShouldWork() {
        assertThatJson("{\"fields\": [{ \"id\": \"test\", \"value\": \"true\" }]}")
            .inPath("$.fields[?(@.id==\"test\")].value")
            .isArray()
            .containsExactly(value("true"));
    }

    @Test
    void stringComparisonShouldWork() {
        final Map<String, Integer> map = new LinkedHashMap<>();
        map.put("FOO", 4);
        map.put("BAR", 3);
        assertThatJson("{\"fields\": [{ \"id\": \"test\", \"value\": \"{FOO=4, BAR=3}\" }]}")
            .inPath("$.fields[?(@.id==\"test\")].value")
            .isArray()
            .containsExactly(value(map.toString()));
    }

    @Test
    void testAbsentInJsonPath() {
        assertThatJson("{}").inPath("$.abc").isAbsent();
    }

    @Test
    void testAbsentInJsonPathEquals() {
        assertThatThrownBy(() -> assertThatJson("{}").inPath("$.abc").isEqualTo("value"))
            .hasMessage("JSON documents are different:\n" +
                "Missing node in path \"$.abc\".\n");
    }

    @Test
    void testAbsentInJsonPathIsArray() {
        assertThatThrownBy(() -> assertThatJson("{}").inPath("$.abc").isArray())
            .hasMessage("Different value found in node \"$.abc\", expected: <array> but was: <missing>.");
    }

    @Test
    void testAbsentInJsonPathNotAbsent() {
        assertThatThrownBy(() -> assertThatJson("{\"abc\": 1}").inPath("$.abc").isAbsent())
            .hasMessage("Different value found in node \"$.abc\", expected: <node to be absent> but was: <1>.");
    }

    @Test
    public void jsonPathShouldBeAbleToUseArrays() {
        assertThatThrownBy(() -> assertThatJson(json)
            .inPath("$.store.book")
            .isArray()
            .contains(json(
                "            {\n" +
                    "                \"category\": \"reference\",\n" +
                    "                \"author\": \"Nigel Rees\",\n" +
                    "                \"title\": \"Sayings of the Century\",\n" +
                    "                \"price\": 8.96\n" +
                    "            }"
            )))
            .hasMessage("[Different value found in node \"$.store.book\"] \n" +
                "Expecting:\n" +
                " <[{\"author\":\"Nigel Rees\",\"category\":\"reference\",\"price\":8.95,\"title\":\"Sayings of the Century\"},\n" +
                "    {\"author\":\"Evelyn Waugh\",\"category\":\"fiction\",\"price\":12.99,\"title\":\"Sword of Honour\"},\n" +
                "    {\"author\":\"Herman Melville\",\"category\":\"fiction\",\"isbn\":\"0-553-21311-3\",\"price\":8.99,\"title\":\"Moby Dick\"},\n" +
                "    {\"author\":\"J. R. R. Tolkien\",\"category\":\"fiction\",\"isbn\":\"0-395-19395-8\",\"price\":22.99,\"title\":\"The Lord of the Rings\"}]>\n" +
                "to contain:\n" +
                " <[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.96}]>\n" +
                "but could not find:\n" +
                " <[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.96}]>\n" +
                "when comparing values using JsonComparator");
    }

    @Test
    void jsonPathShouldBeAbleToUseArraysDeep() {
            assertThatJson(json)
                .inPath("$.store.book[*].category")
                .isArray()
                .containsExactlyInAnyOrder("fiction", "reference", "fiction", "fiction");
    }

    @Test
    void jsonPathShouldBeAbleToUseArraysFromObject() {
            assertThatJson(readValue(json))
                .inPath("$.store.book[*].category")
                .isArray()
                .containsExactlyInAnyOrder("fiction", "reference", "fiction", "fiction");
    }

    @Test
    void testInnerString() {
        String json = "{\"myNode\":{\"inner\":\"foo\"}}";
        assertThatJson(json).inPath("$.myNode.inner").isString().isEqualTo("foo");
    }

    @Test
    void testInnerQuotedString() {
        String json = "{\"myNode\":{\"inner\":\"\\\"foo\\\"\"}}";
        assertThatJson(json).inPath("$.myNode.inner").isString().isEqualTo("\"foo\"");
    }

    @Test
    void testInnerNumber() {
        final String json = "{\"myNode\":{\"inner\":123}}";
        assertThatJson(json).inPath("$.myNode.inner").isNumber().isEqualByComparingTo("123");
    }

    @Test
    void shouldUseInPathInAnd() {
        ConfigurableJsonAssert json = assertThatJson("{\"key1\":\"foo\",\"key2\":\"bar\"}");
        json.inPath("key1").isEqualTo("foo");
        json.inPath("key2").isEqualTo("bar");
    }

    private static final String json = "{\n" +
        "    \"store\": {\n" +
        "        \"book\": [\n" +
        "            {\n" +
        "                \"category\": \"reference\",\n" +
        "                \"author\": \"Nigel Rees\",\n" +
        "                \"title\": \"Sayings of the Century\",\n" +
        "                \"price\": 8.95\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"Evelyn Waugh\",\n" +
        "                \"title\": \"Sword of Honour\",\n" +
        "                \"price\": 12.99\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"Herman Melville\",\n" +
        "                \"title\": \"Moby Dick\",\n" +
        "                \"isbn\": \"0-553-21311-3\",\n" +
        "                \"price\": 8.99\n" +
        "            },\n" +
        "            {\n" +
        "                \"category\": \"fiction\",\n" +
        "                \"author\": \"J. R. R. Tolkien\",\n" +
        "                \"title\": \"The Lord of the Rings\",\n" +
        "                \"isbn\": \"0-395-19395-8\",\n" +
        "                \"price\": 22.99\n" +
        "            }\n" +
        "        ],\n" +
        "        \"bicycle\": {\n" +
        "            \"color\": \"red\",\n" +
        "            \"price\": 19.95\n" +
        "        }\n" +
        "    },\n" +
        "    \"expensive\": 10\n" +
        "}";

    protected abstract Object readValue(String value);
}
