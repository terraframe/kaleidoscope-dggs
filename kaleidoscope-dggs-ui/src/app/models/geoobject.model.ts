import { GeoJSONGeometry } from "wellknown";

export interface GeoObject {
    geometry: GeoJSONGeometry,
    id: string
    type?: string
    properties: {
        uri: string,
        type: string,
        code: string,
        label: string,
        edges?: { [key: string]: [string] }, [key: string]: any
    }
}
