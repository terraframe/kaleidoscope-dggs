import { Feature, LngLatBoundsLike } from "maplibre-gl";
import { GeoObject } from "./geoobject.model";

export interface MessageSection {
  text: string;
  type: number;
  uri?: string;
}

export interface ChatMessage {
  id: string
  sender: 'user' | 'system';
  text: string;
  ambiguous?: boolean;
  loading?: boolean;
  purpose: 'info' | 'standard'
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
  category?: string;
}

