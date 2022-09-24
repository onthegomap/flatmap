package com.onthegomap.planetiler.expression;

import static com.onthegomap.planetiler.expression.Expression.*;
import static com.onthegomap.planetiler.expression.ExpressionTestUtil.featureWithTags;
import static org.junit.jupiter.api.Assertions.*;

import com.onthegomap.planetiler.reader.WithTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExpressionTest {

  public static final Expression.MatchAny matchAB = matchAny("a", "b");
  public static final Expression.MatchAny matchCD = matchAny("c", "d");
  public static final Expression.MatchAny matchBC = matchAny("b", "c");

  @Test
  void testSimplify() {
    assertEquals(matchAB, matchAB.simplify());
  }

  @Test
  void testSimplifyAdjacentOrs() {
    assertEquals(or(matchAB, matchCD),
      or(or(matchAB), or(matchCD)).simplify()
    );
  }

  @Test
  void testSimplifyDuplicates() {
    assertEquals(matchAB, or(or(matchAB), or(matchAB)).simplify());
    assertEquals(matchAB, and(matchAB, matchAB).simplify());
  }

  @Test
  void testMatchAnyEquals() {
    assertEquals(matchAny("a", "b%"), matchAny("a", "b%"));
  }

  @Test
  void testSimplifyOrWithOneChild() {
    assertEquals(matchAB, or(matchAB).simplify());
  }

  @Test
  void testSimplifyOAndWithOneChild() {
    assertEquals(matchAB, and(matchAB).simplify());
  }

  @Test
  void testSimplifyDeeplyNested() {
    assertEquals(matchAB, or(or(and(and(matchAB)))).simplify());
  }

  @Test
  void testSimplifyDeeplyNested2() {
    assertEquals(or(matchAB, matchBC),
      or(or(and(and(matchAB)), matchBC)).simplify());
  }

  @Test
  void testSimplifyDeeplyNested3() {
    assertEquals(or(and(matchAB, matchCD), matchBC),
      or(or(and(and(matchAB), matchCD), matchBC)).simplify());
  }

  @Test
  void testNotNot() {
    assertEquals(matchAB, not(not(matchAB)).simplify());
  }

  @Test
  void testDemorgans() {
    assertEquals(or(not(matchAB), not(matchBC)), not(and(matchAB, matchBC)).simplify());
    assertEquals(and(not(matchAB), not(matchBC)), not(or(matchAB, matchBC)).simplify());
  }

  @Test
  void testSimplifyFalse() {
    assertEquals(FALSE, and(FALSE).simplify());
    assertEquals(FALSE, and(FALSE, matchAB).simplify());
    assertEquals(FALSE, or(FALSE).simplify());
  }

  @Test
  void testSimplifyTrue() {
    assertEquals(TRUE, and(TRUE).simplify());
    assertEquals(matchAB, and(TRUE, matchAB).simplify());
    assertEquals(TRUE, or(TRUE, matchAB).simplify());
  }

  @Test
  void testSimplifyAndCases() {
    assertEquals(TRUE, and(TRUE).simplify());
    assertEquals(TRUE, and(TRUE, TRUE).simplify());
    assertEquals(TRUE, and(TRUE, and()).simplify());
    assertEquals(TRUE, and(and(TRUE)).simplify());
    assertEquals(TRUE, and(and(TRUE), TRUE).simplify());
    assertEquals(TRUE, and(TRUE, and(TRUE), TRUE).simplify());
    assertEquals(matchAB, and(TRUE, and(TRUE), matchAB).simplify());
  }

  @Test
  void testSimplifyOrCases() {
    assertEquals(FALSE, or(or(FALSE)).simplify());
    assertEquals(FALSE, or(or(FALSE), FALSE).simplify());
    assertEquals(FALSE, or(FALSE, or(FALSE), FALSE).simplify());
    assertEquals(matchAB, or(FALSE, or(FALSE), matchAB).simplify());
  }

  @Test
  void testSimplifyNotCases() {
    assertEquals(FALSE, not(TRUE).simplify());
    assertEquals(TRUE, not(FALSE).simplify());
  }

  @Test
  void testEvaluateEmptyAnd() {
    assertEquals(
      and().evaluate(featureWithTags(), new ArrayList<>()),
      and().simplify().evaluate(featureWithTags(), new ArrayList<>())
    );
  }

  @Test
  void testReplace() {
    assertEquals(
      or(not(matchCD), matchCD, and(matchCD, matchBC)),
      or(not(matchAB), matchAB, and(matchAB, matchBC))
        .replace(matchAB, matchCD));
  }

  @Test
  void testReplacePredicate() {
    assertEquals(
      or(not(matchCD), matchCD, and(matchCD, matchCD)),
      or(not(matchCD), matchCD, and(matchCD, matchCD))
        .replace(e -> Set.of(matchAB, matchBC).contains(e), matchCD));
  }

  @Test
  void testContains() {
    assertNull(matchCD.pattern());
    assertTrue(matchCD.contains(e -> e.equals(matchCD)));
    assertTrue(or(not(matchCD)).contains(e -> e.equals(matchCD)));
    assertFalse(matchCD.contains(e -> e.equals(matchAB)));
    assertFalse(or(not(matchCD)).contains(e -> e.equals(matchAB)));
  }

  @Test
  void testWildcardStartsWith() {
    var matcher = matchAny("key", "a%");
    assertEquals(Set.of(), matcher.exactMatches());
    assertNotNull(matcher.pattern());

    assertTrue(matcher.evaluate(featureWithTags("key", "abc")));
    assertTrue(matcher.evaluate(featureWithTags("key", "a")));
    assertFalse(matcher.evaluate(featureWithTags("key", "cba")));
  }

  @Test
  void testWildcardEndsWith() {
    var matcher = matchAny("key", "%a");
    assertEquals(Set.of(), matcher.exactMatches());
    assertNotNull(matcher.pattern());

    assertTrue(matcher.evaluate(featureWithTags("key", "cba")));
    assertTrue(matcher.evaluate(featureWithTags("key", "a")));
    assertFalse(matcher.evaluate(featureWithTags("key", "abc")));
  }

  @Test
  void testWildcardContains() {
    var matcher = matchAny("key", "%a%");
    assertEquals(Set.of(), matcher.exactMatches());
    assertNotNull(matcher.pattern());

    assertTrue(matcher.evaluate(featureWithTags("key", "bab")));
    assertTrue(matcher.evaluate(featureWithTags("key", "a")));
    assertFalse(matcher.evaluate(featureWithTags("key", "c")));
  }

  @Test
  void testWildcardAny() {
    var matcher = matchAny("key", "%");
    assertEquals(Set.of(), matcher.exactMatches());
    assertNotNull(matcher.pattern());
    assertEquals(matchField("key"), matcher.simplify());

    assertTrue(matcher.evaluate(featureWithTags("key", "abc")));
    assertFalse(matcher.evaluate(featureWithTags("key", "")));
  }

  @Test
  void testWildcardMiddle() {
    var matcher = matchAny("key", "a%c");
    assertEquals(Set.of(), matcher.exactMatches());
    assertNotNull(matcher.pattern());

    assertTrue(matcher.evaluate(featureWithTags("key", "abc")));
    assertTrue(matcher.evaluate(featureWithTags("key", "ac")));
    assertFalse(matcher.evaluate(featureWithTags("key", "ab")));
  }

  @Test
  void testWildcardEscape() {
    assertTrue(matchAny("key", "a\\%").evaluate(featureWithTags("key", "a%")));
    assertFalse(matchAny("key", "a\\%").evaluate(featureWithTags("key", "ab")));

    assertTrue(matchAny("key", "a\\%b").evaluate(featureWithTags("key", "a%b")));
    assertTrue(matchAny("key", "%a\\%b%").evaluate(featureWithTags("key", "dda%b")));
    assertTrue(matchAny("key", "\\%%").evaluate(featureWithTags("key", "%abc")));
    assertTrue(matchAny("key", "%\\%").evaluate(featureWithTags("key", "abc%")));
    assertTrue(matchAny("key", "%\\%%").evaluate(featureWithTags("key", "a%c")));
    assertTrue(matchAny("key", "%\\%%").evaluate(featureWithTags("key", "%")));
    assertFalse(matchAny("key", "\\%%").evaluate(featureWithTags("key", "abc%")));
  }

  @Test
  void testStringifyExpression() {
    //Ensure Expression.toString() returns valid Java code
    assertEquals("matchAny(\"key\", \"true\")", matchAny("key", "true").generateJavaCode());
    assertEquals("matchAny(\"key\", \"foo\")", matchAny("key", "foo").generateJavaCode());
    var expression = matchAnyTyped("key", WithTags::getDirection, 1);
    assertThrows(UnsupportedOperationException.class, expression::generateJavaCode);
  }

  @Test
  void testEvaluate() {
    WithTags feature = featureWithTags("key1", "value1", "key2", "value2", "key3", "");

    //And
    assertTrue(and(matchAny("key1", "value1"), matchAny("key2", "value2")).evaluate(feature));
    assertFalse(and(matchAny("key1", "value1"), matchAny("key2", "wrong")).evaluate(feature));
    assertFalse(and(matchAny("key1", "wrong"), matchAny("key2", "value2")).evaluate(feature));
    assertFalse(and(matchAny("key1", "wrong"), matchAny("key2", "wrong")).evaluate(feature));

    //Or
    assertTrue(or(matchAny("key1", "value1"), matchAny("key2", "value2")).evaluate(feature));
    assertTrue(or(matchAny("key1", "value1"), matchAny("key2", "wrong")).evaluate(feature));
    assertTrue(or(matchAny("key1", "wrong"), matchAny("key2", "value2")).evaluate(feature));
    assertFalse(or(matchAny("key1", "wrong"), matchAny("key2", "wrong")).evaluate(feature));

    //Not
    assertFalse(not(matchAny("key1", "value1")).evaluate(feature));
    assertTrue(not(matchAny("key1", "wrong")).evaluate(feature));

    //MatchField
    assertTrue(matchField("key1").evaluate(feature));
    assertFalse(matchField("wrong").evaluate(feature));
    assertTrue(not(matchAny("key1", "")).evaluate(feature));
    assertTrue(matchAny("wrong", "").evaluate(feature));
    assertTrue(matchAny("key3", "").evaluate(feature));

    //Constants
    assertTrue(TRUE.evaluate(feature));
    assertFalse(FALSE.evaluate(feature));
  }

  @Test
  void testCustomExpression() {
    Expression custom = new Expression() {
      @Override
      public boolean evaluate(WithTags input, List<String> matchKeys) {
        return input.hasTag("abc");
      }

      @Override
      public String generateJavaCode() {
        return null;
      }
    };
    WithTags matching = featureWithTags("abc", "123");
    WithTags notMatching = featureWithTags("abcd", "123");

    assertTrue(custom.evaluate(matching));
    assertTrue(and(custom).evaluate(matching));
    assertTrue(and(custom, custom).evaluate(matching));
    assertTrue(or(custom, custom).evaluate(matching));
    assertTrue(and(TRUE, custom).evaluate(matching));
    assertTrue(or(FALSE, custom).evaluate(matching));

    assertFalse(custom.evaluate(notMatching));
    assertFalse(and(custom).evaluate(notMatching));
    assertFalse(and(custom, custom).evaluate(notMatching));
    assertFalse(or(custom, custom).evaluate(notMatching));
    assertFalse(and(TRUE, custom).evaluate(notMatching));
    assertFalse(or(FALSE, custom).evaluate(notMatching));
  }
}
