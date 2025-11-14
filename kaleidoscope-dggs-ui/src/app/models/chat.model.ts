import { Feature, LngLatBoundsLike } from "maplibre-gl";
import { GeoObject } from "./geoobject.model";

export interface MessageSection {
  text: string;
  type: number;
  uri?: string;
}

export interface ChatMessage {
  id: string
  role: 'USER' | 'SYSTEM';
  messageType: 'BASIC' | 'LOCATION_RESOLVED' | 'NAME_RESOLUTION' | 'ERROR';
  text: string;
  ambiguous?: boolean;
  loading?: boolean;
  data?: any;
}

export interface ServerChatResponse {
  content: string;
  sessionId: string;
  mappable: boolean;
  ambiguous: boolean;
  location?: string;
}

export interface LocationPage {
  locations: GeoObject[];
  limit: number;
  offset: number;
  count: number;
}

export interface ZoneCollection {
  bbox: LngLatBoundsLike;
  features: Feature[];
}

export interface DggsJson {
  dggrs: string
  values: Value
  depths: string[]
  zoneId: string
}

export interface Value {
  properties: Properties
}

export interface Properties {
  [key: string]: PropertyData[];
}

export interface PropertyData {
  depth: string;
  shape: Shape;
  data: any[];
}

export interface Shape {
  subZones: number
  count: number
}

export interface Message {
  type: string;
  collection?: ZoneCollection;
  page?: LocationPage;
  content?: string;
  toolUseId?: string;
  locationName?: string;
  zones?: DggsJson[];
  population?: number;
}

