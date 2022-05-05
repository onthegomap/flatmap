package com.onthegomap.planetiler.custommap;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.custommap.configschema.FeatureLayer;
import com.onthegomap.planetiler.custommap.configschema.SchemaConfig;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A profile configured from a yml file.
 */
public class ConfiguredProfile implements Profile {

  private String schemaName;
  private String attribution;
  private String description;

  private List<ConfiguredFeature> features = new ArrayList<>();

  public ConfiguredProfile(SchemaConfig schemaConfig) {
    schemaName = schemaConfig.getSchemaName();
    attribution = schemaConfig.getAttribution();
    description = schemaConfig.getSchemaDescription();

    Collection<FeatureLayer> layers = schemaConfig.getLayers();
    if (layers == null) {
      throw new IllegalArgumentException("No layers defined");
    }

    TagValueProducer tagValueProducer = new TagValueProducer(schemaConfig.getDataTypes());

    for (var layer : layers) {
      String layerName = layer.name();
      for (var feature : layer.features()) {
        features.add(new ConfiguredFeature(layerName, tagValueProducer, feature));
      }
    }
  }

  @Override
  public String name() {
    return schemaName;
  }


  @Override
  public String attribution() {
    return attribution;
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector featureCollector) {
    features
      .stream()
      .filter(cf -> cf.includeWhen(sourceFeature))
      .forEach(cf -> cf.processFeature(sourceFeature, featureCollector));
  }

  @Override
  public String description() {
    return description;
  }
}
