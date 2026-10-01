# Kaleidoscope DGGS

Kaleidoscope DGGS brings [Kaleidoscope](https://github.com/terraframe/kaleidoscope)'s plain-language GeoAI to data published on a Discrete Global Grid System (DGGS). You ask a question about a place, and Kaleidoscope DGGS gets the answer from DGGS servers that follow the [OGC API – DGGS](https://ogcapi.ogc.org/dggs/) standard. It then combines that data with a spatial knowledge graph to show what is affected, such as roads, power infrastructure and the people who depend on them.

It's an experimental companion to Kaleidoscope, exploring how Geo-Graph RAG works when the data comes from a DGGS rather than from vector features.

## Why DGGS

A DGGS divides the whole Earth into a hierarchy of cells, called zones, that have consistent shapes and sizes at each level of detail. Data such as elevation or flood depth can be stored as values on those zones instead of as rasters or polygons in many different projections. Because every dataset on the same grid shares the same zones, data from different sources can be compared and combined directly, at whatever level of detail a question needs.

OGC API – DGGS gives these datasets a standard web interface. Kaleidoscope DGGS uses that interface, so it can work with any compliant server rather than one vendor's data.

## What you can do with Kaleidoscope DGGS

- **Ask questions in plain language**, such as asking about elevation or flooding in a named place. The assistant works out which dataset the question is about and which place it refers to.
- **Filter by attribute and date.** Questions like "where is elevation above 2.3 m" are turned into CQL2 filters, using only the attributes each dataset actually supports. Dates are matched against each dataset's time intervals.
- **Find impacted roads.** Kaleidoscope DGGS finds the highways in the knowledge graph that intersect the zones matching the question.
- **Find impacted power infrastructure.** It finds the power stations, substations and transformers that sit within the matching zones.
- **Estimate the population affected by power loss.** Starting from the impacted power infrastructure, it follows "provides power" relationships in the knowledge graph to the census dissemination areas each facility serves. It then adds up their population.
- **See results on a map**, with DGGS zones drawn in the browser and the affected features listed in a results table.

## How it works

Each question is sent to a Claude model on Amazon Bedrock, together with a description of every available DGGS collection, including its time intervals and the attributes it can be filtered on. The model either answers directly, asks a follow-up question, or calls one of the tools below. Kaleidoscope DGGS then runs the tool and returns the result.

| Tool | What it does |
| --- | --- |
| `Name_Resolution` | Looks up a place name in the knowledge graph using full-text search. If more than one place matches, the user is asked to choose. |
| `Location_Data` | Gets the DGGS zones covering a place from a collection, with optional date, filter and zone depth. Results are returned as DGGS-JSON or GeoJSON. |
| `Roads` | Converts the matching zones to polygons and finds highways that intersect them. |
| `Power_Infrastructure` | Converts the matching zones to polygons and finds power facilities inside them. |
| `Dissemination_Area` | Finds power facilities inside the matching zones and follows their "provides power" relationships to dissemination areas, totaling the population served. |

On the DGGS side, Kaleidoscope DGGS discovers collections, their DGGRS and their queryable attributes through OGC API – DGGS. It requests the zones covering a place's bounding box and then each zone's data. On the knowledge graph side, places, infrastructure and the relationships between them are stored as RDF in Apache Jena and queried with GeoSPARQL. [DGGAL](https://dggal.org/) links the two by turning DGGS zone IDs into polygons. This happens on the server for spatial queries and in the browser for drawing zones on the map.

### Main technologies

- **Frontend:** Angular, NgRx, PrimeNG, MapLibre GL and DGGAL (WebAssembly)
- **Backend:** Java 22 and Spring Boot
- **AI:** Claude on Amazon Bedrock, using the Converse API with tool use
- **DGGS:** OGC API – DGGS, DGGS-JSON, CQL2 and DGGAL
- **Knowledge graph:** RDF and GeoSPARQL in Apache Jena, with Jena full-text search
- **Geometry:** JTS and GeoTools

## Configuration

Copy `kaleidoscope-dggs-api/src/main/resources/application.properties.example` to `application.properties` and fill in the settings below.

| Setting | Description |
| --- | --- |
| `bedrock.region` | AWS region for Amazon Bedrock |
| `bedrock.model` | Bedrock model ID. The default is a Claude Sonnet model. |
| `access.key.id`, `secret.access.key` | Credentials for an IAM user that can call Bedrock |
| `jena.url` | URL of the Apache Jena dataset that holds the knowledge graph |
| `dggs.urls` | Comma-separated list of OGC API – DGGS server URLs |
| `spring.security.user.name`, `spring.security.user.password` | Login for the application |

## Repository structure

| Folder | Contents |
| --- | --- |
| `kaleidoscope-dggs-ui` | Angular web application, including the chat, map, results table and attribute panel |
| `kaleidoscope-dggs-api` | Spring Boot API: the chat agent, OGC API – DGGS client, DGGAL integration and knowledge graph queries |
| `agents` | Agent instruction files |

## Related projects

- [Kaleidoscope](https://github.com/terraframe/kaleidoscope), TerraFrame's GeoAI application for spatial knowledge graphs, and its [documentation](https://terraframe.github.io/kaleidoscope-documentation/)
- [OGC API – DGGS](https://ogcapi.ogc.org/dggs/)
- [DGGAL](https://dggal.org/), the Discrete Global Grid Abstraction Library

## Contributing

Bug reports and feature requests are welcome as [issues](https://github.com/terraframe/kaleidoscope-dggs/issues).

## About

Kaleidoscope DGGS is developed by [TerraFrame](https://terraframe.com).

## License

Kaleidoscope DGGS is released under the [MIT License](LICENSE).
