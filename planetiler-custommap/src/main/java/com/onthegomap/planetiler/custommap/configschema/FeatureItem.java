package com.onthegomap.planetiler.custommap.configschema;

import com.onthegomap.planetiler.geo.GeometryType;
import java.util.Collection;

public class FeatureItem {
  private Collection<String> sources;
  private ZoomConfig zoom;
  private GeometryType geometry;
  private TagCriteria includeWhen;
  private TagCriteria excludeWhen;

  private Collection<AttributeDefinition> attributes;

  public Collection<String> getSources() {
    return sources;
  }

  public void setSources(Collection<String> sources) {
    this.sources = sources;
  }

  public ZoomConfig getZoom() {
    return zoom;
  }

  public void setZoom(ZoomConfig zoom) {
    this.zoom = zoom;
  }

  public GeometryType getGeometry() {
    return geometry;
  }

  public void setGeometry(GeometryType geometry) {
    this.geometry = geometry;
  }

  public TagCriteria getIncludeWhen() {
    return includeWhen;
  }

  public void setIncludeWhen(TagCriteria includeWhen) {
    this.includeWhen = includeWhen;
  }

  public TagCriteria getExcludeWhen() {
    return excludeWhen;
  }

  public void setExcludeWhen(TagCriteria excludeWhen) {
    this.excludeWhen = excludeWhen;
  }

  public Collection<AttributeDefinition> getAttributes() {
    return attributes;
  }

  public void setAttributes(Collection<AttributeDefinition> attributes) {
    this.attributes = attributes;
  }
}
