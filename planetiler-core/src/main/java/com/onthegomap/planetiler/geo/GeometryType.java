package com.onthegomap.planetiler.geo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onthegomap.planetiler.expression.Expression;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.Puntal;
import vector_tile.VectorTileProto;

public enum GeometryType {
  UNKNOWN(VectorTileProto.Tile.GeomType.UNKNOWN, 0, "unknown"),
  @JsonProperty("point")
  POINT(VectorTileProto.Tile.GeomType.POINT, 1, "point"),
  @JsonProperty("line")
  LINE(VectorTileProto.Tile.GeomType.LINESTRING, 2, "linestring"),
  @JsonProperty("polygon")
  POLYGON(VectorTileProto.Tile.GeomType.POLYGON, 4, "polygon");

  private final VectorTileProto.Tile.GeomType protobufType;
  private final int minPoints;
  private final String matchTypeString;

  GeometryType(VectorTileProto.Tile.GeomType protobufType, int minPoints, String matchTypeString) {
    this.protobufType = protobufType;
    this.minPoints = minPoints;
    this.matchTypeString = matchTypeString;
  }

  public static GeometryType typeOf(Geometry geom) {
    return geom instanceof Puntal ? POINT : geom instanceof Lineal ? LINE : geom instanceof Polygonal ? POLYGON :
      UNKNOWN;
  }

  public static GeometryType valueOf(VectorTileProto.Tile.GeomType geomType) {
    return switch (geomType) {
      case POINT -> POINT;
      case LINESTRING -> LINE;
      case POLYGON -> POLYGON;
      default -> UNKNOWN;
    };
  }

  public static GeometryType valueOf(byte val) {
    return valueOf(VectorTileProto.Tile.GeomType.forNumber(val));
  }

  public byte asByte() {
    return (byte) protobufType.getNumber();
  }

  public VectorTileProto.Tile.GeomType asProtobufType() {
    return protobufType;
  }

  public int minPoints() {
    return minPoints;
  }

  /**
   * Generates a test for whether a source feature is of the correct geometry to be included in the tile.
   *
   * @return geometry test method
   */
  public Expression featureTest() {
    return Expression.matchType(matchTypeString);
  }

}
