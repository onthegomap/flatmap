package com.onthegomap.planetiler.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carrotsearch.hppc.DoubleArrayList;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;

class MutableCoordinateSequenceTest {

  private static void assertContents(MutableCoordinateSequence seq, double... expected) {
    double[] actual = new double[seq.size() * 2];
    for (int i = 0; i < seq.size(); i++) {
      actual[i * 2] = seq.getX(i);
      actual[i * 2 + 1] = seq.getY(i);
    }
    assertEquals(DoubleArrayList.from(expected), DoubleArrayList.from(actual), "getX/getY");
    PackedCoordinateSequence copy = seq.copy();
    for (int i = 0; i < seq.size(); i++) {
      actual[i * 2] = copy.getX(i);
      actual[i * 2 + 1] = copy.getY(i);
    }
    assertEquals(DoubleArrayList.from(expected), DoubleArrayList.from(actual), "copied getX/getY");
  }

  @Test
  void testEmpty() {
    var seq = new MutableCoordinateSequence();
    assertEquals(0, seq.copy().size());
  }

  @Test
  void testSingle() {
    var seq = new MutableCoordinateSequence();
    seq.addPoint(1, 2);
    assertContents(seq, 1, 2);
  }

  @Test
  void testTwoPoints() {
    var seq = new MutableCoordinateSequence();
    seq.addPoint(1, 2);
    seq.addPoint(3, 4);
    assertContents(seq, 1, 2, 3, 4);
  }

  @Test
  void testClose() {
    var seq = new MutableCoordinateSequence();
    seq.addPoint(1, 2);
    seq.addPoint(3, 4);
    seq.addPoint(0, 1);
    seq.closeRing();
    assertContents(seq, 1, 2, 3, 4, 0, 1, 1, 2);
  }

  @Test
  void testScaling() {
    var seq = MutableCoordinateSequence.newScalingSequence(1, 2, 3);
    seq.addPoint(1, 2);
    seq.addPoint(3, 4);
    seq.addPoint(0, 1);
    seq.closeRing();
    assertContents(seq, 0, 0, 6, 6, -3, -3, 0, 0);
  }
}
