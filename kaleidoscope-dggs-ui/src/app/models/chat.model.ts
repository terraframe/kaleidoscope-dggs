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

export interface Message {
  type: string;
  collection?: ZoneCollection;
  page?: LocationPage;
  content?: string;
  toolUseId?: string;
  locationName?: string;
}

