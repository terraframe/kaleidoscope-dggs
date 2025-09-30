import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { v4 as uuidv4 } from 'uuid';

import { ChatMessage, LocationPage, Message, ServerChatResponse, ZoneCollection } from '../models/chat.model';
import { MockUtil } from '../mock-util';
import { environment } from '../../environments/environment';
import { GeoObject } from '../models/geoobject.model';
import { Feature } from 'maplibre-gl';

@Injectable({
  providedIn: 'root',
})
export class ChatService {

  constructor(private http: HttpClient) {
  }


  query(inputText: string): Promise<Message> {

    // if (environment.mockRequests)
    // {
    //   return new Promise<ChatMessage>((resolve) => {
    //     setTimeout(() => {
    //       resolve(MockUtil.message);
    //     }, 500); // Simulating network delay 
    //   });
    // }
    // else
    // {
    // Uncomment below to make a real HTTP request
    let params = new HttpParams();
    params = params.append("inputText", inputText);

    return firstValueFrom(this.http.get<Message>(environment.apiUrl + 'api/chat/query', { params }));
  }

  zones(uri: string, category: string, datetime: string | null): Promise<Message> {

    // if (environment.mockRequests)
    // {
    //   return new Promise<ChatMessage>((resolve) => {
    //     setTimeout(() => {
    //       resolve(MockUtil.message);
    //     }, 500); // Simulating network delay 
    //   });
    // }
    // else
    // {
    // Uncomment below to make a real HTTP request
    let params = new HttpParams();
    params = params.append("uri", uri);
    params = params.append("category", category);

    if (datetime != null) {
      params = params.append("datetime", datetime);
    }

    return firstValueFrom(this.http.get<Message>(environment.apiUrl + 'api/chat/zones', { params }));
  }
  // }


}