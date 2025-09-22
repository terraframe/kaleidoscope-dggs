import { Injectable } from '@angular/core';
import { GeoObject } from '../models/geoobject.model';
import { MockUtil } from '../mock-util';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';
import { LocationPage } from '../models/chat.model';


export interface ExplorerInit {

}

@Injectable({
    providedIn: 'root'
})
export class ExplorerService {

    constructor(private http: HttpClient) { }

    init(): Promise<ExplorerInit> {
        return new Promise<ExplorerInit>((resolve) => {
            setTimeout(() => {
                resolve(MockUtil.explorerInit);
            }, 3000); // Simulating 3-second network delay
        });
    }

    fullTextLookup(query: string): Promise<LocationPage> {
        return firstValueFrom(this.http.post<LocationPage>(environment.apiUrl + 'api/full-text-lookup', { query: query }));
    }

    getAttributes(uri: string, includeGeometry: boolean = false, hasPrefix: boolean = true): Promise<GeoObject> {

        let params = new HttpParams();
        params = params.append("uri", uri);
        params = params.append("includeGeometry", includeGeometry);
        params = params.append("hasPrefix", hasPrefix);

        return firstValueFrom(this.http.get<GeoObject>(environment.apiUrl + 'api/get-attributes', { params }));
    }

    data(zoneDepth: number): Promise<string> {

        let params = new HttpParams();
        params = params.append("zone-depth", zoneDepth);

        return firstValueFrom(this.http.get<string>(environment.apiUrl + 'api/data', { params }));
    }

}
