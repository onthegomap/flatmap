package com.onthegomap.planetiler.reader.osm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carrotsearch.hppc.LongArrayList;
import com.onthegomap.planetiler.TestUtils;
import com.onthegomap.planetiler.stats.Stats;
import com.onthegomap.planetiler.worker.WorkerPipeline;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Envelope;

class OsmInputFileTest {

  private final Path path = TestUtils.pathToResource("monaco-latest.osm.pbf");
  private final OsmElement.Node expectedNode = new OsmElement.Node(1737114566L, Map.of(
    "highway", "crossing",
    "crossing", "zebra"
  ), 43.7409723, 7.4303278, new OsmElement.Info(0, 1600807207, 0, 5, ""));
  private final OsmElement.Way expectedWay = new OsmElement.Way(4097656L, Map.of(
    "name", "Avenue Princesse Alice",
    "lanes", "2",
    "maxspeed", "30",
    "highway", "primary",
    "surface", "asphalt",
    "lit", "yes"
  ), LongArrayList.from(
    21912089L, 7265761724L, 1079750744L, 2104793864L, 6340961560L, 1110560507L, 21912093L, 6340961559L, 21912095L,
    7265762803L, 2104793866L, 6340961561L, 5603088200L, 6340961562L, 21912097L, 21912099L
  ), new OsmElement.Info(0, 1583398246, 0, 13, ""));
  private final OsmElement.Relation expectedRel = new OsmElement.Relation(7360630L, Map.of(
    "local_ref", "Saint-Roman",
    "public_transport:version", "2",
    "name", "Saint-Roman",
    "public_transport", "stop_area",
    "type", "public_transport",
    "operator", "Compagnie des Autobus de Monaco",
    "network", "Autobus de Monaco"
  ), List.of(
    new OsmElement.Relation.Member(OsmElement.Type.WAY, 503638817L, "platform"),
    new OsmElement.Relation.Member(OsmElement.Type.WAY, 503638816L, "platform"),
    new OsmElement.Relation.Member(OsmElement.Type.NODE, 4939122054L, "platform"),
    new OsmElement.Relation.Member(OsmElement.Type.NODE, 3465728159L, "stop"),
    new OsmElement.Relation.Member(OsmElement.Type.NODE, 4939122068L, "platform"),
    new OsmElement.Relation.Member(OsmElement.Type.NODE, 3805333988L, "stop")
  ), new OsmElement.Info(0, 1586074405, 0, 2, ""));
  private final Envelope expectedBounds = new Envelope(7.409205, 7.448637, 43.72335, 43.75169);

  @Test
  void testGetBounds() {
    assertEquals(expectedBounds, new OsmInputFile(path).getLatLonBounds());
  }

  @Test
  void testGetHeader() {
    assertEquals(new OsmHeader(
      expectedBounds,
      List.of("OsmSchema-V0.6", "DenseNodes"),
      List.of(),
      "osmium/1.8.0",
      "",
      Instant.parse("2021-04-21T20:21:46Z"),
      2947,
      "http://download.geofabrik.de/europe/monaco-updates"
    ),
      new OsmInputFile(path).getHeader()
    );
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  @Timeout(30)
  void testReadMonacoTwice(boolean lazy) {
    for (int i = 1; i <= 2; i++) {
      AtomicInteger nodes = new AtomicInteger(0);
      AtomicInteger ways = new AtomicInteger(0);
      AtomicInteger rels = new AtomicInteger(0);
      AtomicReference<OsmElement.Node> node = new AtomicReference<>();
      AtomicReference<OsmElement.Way> way = new AtomicReference<>();
      AtomicReference<OsmElement.Relation> rel = new AtomicReference<>();
      var file = new OsmInputFile(path, lazy);
      try (var osmReader = file.get()) {
        WorkerPipeline.start("test", Stats.inMemory())
          .fromGenerator("pbf", osmReader::forEachBlock)
          .addBuffer("pbf_blocks", 100)
          .sinkToConsumer("counter", 1, block -> {
            for (var elem : block.decodeElements()) {
              if (elem instanceof OsmElement.Node n) {
                if (n.id() == expectedNode.id()) {
                  node.set(n);
                }
                nodes.incrementAndGet();
              } else if (elem instanceof OsmElement.Way w) {
                if (w.id() == expectedWay.id()) {
                  way.set(w);
                }
                ways.incrementAndGet();
              } else if (elem instanceof OsmElement.Relation r) {
                if (r.id() == expectedRel.id()) {
                  rel.set(r);
                }
                rels.incrementAndGet();
              }
            }
          }).await();
        assertEquals(25_423, nodes.get(), "nodes pass " + i);
        assertEquals(4_106, ways.get(), "ways pass " + i);
        assertEquals(243, rels.get(), "rels pass " + i);

        assertEquals(expectedNode, node.get());
        assertEquals(expectedWay, way.get());
        assertEquals(expectedRel, rel.get());
      }
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  @Timeout(30)
  void testReadMonacoWithoutChangesetsTwice(boolean lazy) {
    for (int i = 1; i <= 2; i++) {
      AtomicInteger nodes = new AtomicInteger(0);
      AtomicInteger ways = new AtomicInteger(0);
      AtomicInteger rels = new AtomicInteger(0);
      AtomicReference<OsmElement.Node> node = new AtomicReference<>();
      AtomicReference<OsmElement.Way> way = new AtomicReference<>();
      AtomicReference<OsmElement.Relation> rel = new AtomicReference<>();
      var file = new OsmInputFile(TestUtils.pathToResource("monaco-latest-without-changesets.osm.pbf"), lazy);
      try (var osmReader = file.get()) {
        WorkerPipeline.start("test", Stats.inMemory())
          .fromGenerator("pbf", osmReader::forEachBlock)
          .addBuffer("pbf_blocks", 100)
          .sinkToConsumer("counter", 1, block -> {
            for (var elem : block.decodeElements()) {
              if (elem instanceof OsmElement.Node n) {
                if (n.id() == expectedNode.id()) {
                  node.set(n);
                }
                nodes.incrementAndGet();
              } else if (elem instanceof OsmElement.Way w) {
                if (w.id() == expectedWay.id()) {
                  way.set(w);
                }
                ways.incrementAndGet();
              } else if (elem instanceof OsmElement.Relation r) {
                if (r.id() == expectedRel.id()) {
                  rel.set(r);
                }
                rels.incrementAndGet();
              }
            }
          }).await();
        assertEquals(27_135, nodes.get(), "nodes pass " + i);
        assertEquals(4_295, ways.get(), "ways pass " + i);
        assertEquals(255, rels.get(), "rels pass " + i);

        assertEquals(new OsmElement.Info(0, 1647347498, 0, 8, ""), node.get().info());
      }
    }
  }
}
