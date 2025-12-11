package ai.terraframe.kaleidoscope.dggs.core.model;

public enum LocationType {
  SUBDIVISION("http://terraframe.ai#Subdivision"),

  POWER_STATION("http://terraframe.ai#PowerStation"),

  POWER_SUB_STATION("http://terraframe.ai#PowerSubstation"),

  POWER_TRANSFORMER("http://terraframe.ai#PowerTransformer"),

  PROVIDES_POWER("http://terraframe.ai#ProvidesPower"),

  HIGHWAY("http://terraframe.ai#Highway");

  private String uri;

  private LocationType(String uri)
  {
    this.uri = uri;
  }

  public String getUri()
  {
    return uri;
  }
}
