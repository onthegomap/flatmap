package com.onthegomap.planetiler.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ParseTest {

  @ParameterizedTest
  @CsvSource({
    "0, false, 0",
    "false, false, 0",
    "no, false, 0",
    "yes, true, 1",
    "true, true, 1",
    "ok, true, 1",
  })
  public void testBoolean(String in, boolean out, int boolint) {
    assertEquals(out, Parse.bool(in));
    assertEquals(boolint, Parse.boolInt(in));
  }

  @ParameterizedTest
  @CsvSource(
      value = {"0, 0, 0", "false, 0, null", "123, 123, 123"},
      nullValues = {"null"})
  public void testLong(String in, long out, Long obj) {
    assertEquals(out, Parse.parseLong(in));
    assertEquals(obj, Parse.parseLongOrNull(in));
  }

  @ParameterizedTest
  @CsvSource({"1, 1", "yes, 1", "true, 1", "-1, -1", "2, 0", "0, 0"})
  public void testDirection(String in, int out) {
    assertEquals(out, Parse.direction(in));
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "1, 1",
        "0, 0",
        "-1, -1",
        "1.1, 1",
        "-1.1, -1",
        "-1.23 m, -1",
        "one meter, null",
        "null, null"
      },
      nullValues = {"null"})
  public void testIntSubstring(String in, Integer out) {
    assertEquals(out, Parse.parseIntSubstring(in));
  }

  @TestFactory
  public Stream<DynamicTest> testWayzorder() {
    return Stream.<Map.Entry<Map<String, Object>, Integer>>of(
            Map.entry(Map.of(), 0),
            Map.entry(Map.of("layer", 1), 10),
            Map.entry(Map.of("layer", -3), -30),
            Map.entry(Map.of("highway", "motorway"), 9),
            Map.entry(Map.of("railway", "anything"), 7),
            Map.entry(Map.of("railway", "anything", "tunnel", "1"), -3),
            Map.entry(Map.of("railway", "anything", "bridge", "1"), 17))
        .map(
            entry ->
                dynamicTest(
                    entry.getKey().toString(),
                    () -> assertEquals(entry.getValue(), Parse.wayzorder(entry.getKey()))));
  }
}
