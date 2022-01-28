package com.onthegomap.planetiler.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FormatTest {

  @ParameterizedTest
  @CsvSource({
    "1.5,1,en",
    "999,999,en",
    "1000,1k,en",
    "9999,9.9k,en",
    "10001,10k,en",
    "99999,99k,en",
    "999999,999k,en",
    "9999999,9.9M,en",
    "-9999999,-,en",
    "5.5e12,5.5T,en",
    "5.5e12,'5,5T',fr",
  })
  public void testFormatNumeric(Double number, String expected, Locale locale) {
    assertEquals(expected, Format.forLocale(locale).numeric(number, false));
  }

  @ParameterizedTest
  @CsvSource({
    "999,999,en",
    "1000,1k,en",
    "9999,9.9k,en",
    "5.5e9,5.5G,en",
    "5.5e9,'5,5G',fr",
  })
  public void testFormatStorage(Double number, String expected, Locale locale) {
    assertEquals(expected, Format.forLocale(locale).storage(number, false));
  }

  @ParameterizedTest
  @CsvSource({
    "0,0%,en",
    "1,100%,en",
    "0.11111,11%,en",
    "0.11111,11 %,fr",
  })
  public void testFormatPercent(Double number, String formatted, Locale locale) {
    assertEquals(formatted, Format.forLocale(locale).percent(number));
  }

  @ParameterizedTest
  @CsvSource({
    "a,0,a",
    "a,1,a",
    "a,2,' a'",
    "a,3,'  a'",
    "ab,3,' ab'",
    "abc,3,'abc'",
  })
  public void testPad(String in, Integer size, String out) {
    assertEquals(out, Format.padLeft(in, size));
  }

  @ParameterizedTest
  @CsvSource({
    "0,0,en",
    "0.1,0.1,en",
    "0.11,0.1,en",
    "1111.11,'1,111.1',en",
    "1111.11,'1.111,1',it",
  })
  public void testFormatDecimal(Double in, String out, Locale locale) {
    assertEquals(out, Format.forLocale(locale).decimal(in));
  }
}
