import { Component, AfterViewInit, TemplateRef, ViewChild, inject, OnInit, OnDestroy } from '@angular/core';
import { Map, NavigationControl, AttributionControl, LngLatBounds, LngLat, GeoJSONSource, LngLatBoundsLike, MapGeoJSONFeature, Source, Feature } from "maplibre-gl";
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PanelModule } from 'primeng/panel';
import { ToastModule } from 'primeng/toast';
import { CheckboxModule } from 'primeng/checkbox';
import { Observable, take, withLatestFrom } from 'rxjs';
import { Store } from '@ngrx/store';
import * as turf from '@turf/turf';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { GeoObject } from '../models/geoobject.model';
import { StyleConfig } from '../models/style.model';

import { AttributePanelComponent } from '../attribute-panel/attribute-panel.component';
import { AichatComponent } from '../aichat/aichat.component';
import { ResultsTableComponent } from '../results-table/results-table.component';
import { ConfigurationService } from '../service/configuration-service.service';
import { defaultQueries, SELECTED_COLOR, HOVER_COLOR, GREEN, RED } from './defaultQueries';
import { AllGeoJSON, bbox, bboxPolygon, center, union } from '@turf/turf';
import { ErrorService } from '../service/error-service.service';
import { ExplorerActions, getNeighbors, getObjects, getStyles, getVectorLayers, getZoomMap, highlightedObject, selectedObject, getWorkflowStep, WorkflowStep, getPage, getZones, getWorkflowData, getDggsJson } from '../state/explorer.state';
import { TabsModule } from 'primeng/tabs';
import { VectorLayer } from '../models/vector-layer.model';
import { environment } from '../../environments/environment';
import { faArrowLeft, faArrowRight, faDownLeftAndUpRightToCenter, faUpRightAndDownLeftFromCenter } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ButtonModule } from 'primeng/button';
import { DggsJson, LocationPage } from '../models/chat.model';
import { TooltipModule } from 'primeng/tooltip';
import { ChatService } from '../service/chat-service.service';
import { MessageService } from '../service/message.service';

import { DGGAL } from 'dggal';


export interface TypeLegend { [key: string]: { label: string, color: string, visible: boolean, included: boolean } }

@Component({
    selector: 'app-explorer',
    imports: [
        CommonModule,
        FormsModule,
        AichatComponent,
        AttributePanelComponent,
        DragDropModule,
        ResultsTableComponent,
        ProgressSpinnerModule,
        PanelModule,
        ToastModule,
        TabsModule,
        CheckboxModule,
        FontAwesomeModule,
        ButtonModule,
        TooltipModule
    ],
    templateUrl: './explorer.component.html',
    styleUrl: './explorer.component.scss'
})
export class ExplorerComponent implements OnInit, OnDestroy, AfterViewInit {
    public WorkflowStep = WorkflowStep;
    public backIcon = faArrowLeft;
    public forwardIcon = faArrowRight;
    public minimizeIcon = faDownLeftAndUpRightToCenter;
    public upsizeIcon = faUpRightAndDownLeftFromCenter;

    private store = inject(Store);

    dggsjson$: Observable<DggsJson[]> = this.store.select(getDggsJson);

    zones$: Observable<Feature[]> = this.store.select(getZones);

    zoomMap$: Observable<boolean> = this.store.select(getZoomMap);

    geoObjects: GeoObject[] = [];


    // renderedObjects: string[] = [];

    // neighbors$: Observable<GeoObject[]> = this.store.select(getNeighbors);

    // neighbors: GeoObject[] = [];

    styles$: Observable<StyleConfig> = this.store.select(getStyles);

    selectedObject$: Observable<GeoObject | null> = this.store.select(selectedObject);

    // highlightedObject$: Observable<GeoObject | null> = this.store.select(highlightedObject);

    workflowStep$: Observable<WorkflowStep> = this.store.select(getWorkflowStep);

    workflowData$: Observable<any> = this.store.select(getWorkflowData);

    page$: Observable<LocationPage> = this.store.select(getPage);

    resolvedStyles: StyleConfig = {};

    public inspectorTab = 0;

    map?: Map;

    // file?: string;

    importError?: string;

    public defaultQueries = defaultQueries;

    public loading: boolean = false;

    public typeLegend: TypeLegend = {};

    public selectedObject?: GeoObject;

    public highlightedObject: GeoObject | null | undefined;

    baseLayers: any[] = [
        {
            name: "streets-v12",
            id: "streets-v12",
            selected: true
        }
    ];

    orderedTypes: string[] = [];

    initialized: boolean = false;

    vectorLayers$: Observable<VectorLayer[]> = this.store.select(getVectorLayers);

    // onVectorLayersChange: Subscription;

    public workflowStep: WorkflowStep = WorkflowStep.AiChatAndResults;

    public activeTab: string = '0';

    public chatMinimized: boolean = false;

    public page: LocationPage = {
        locations: [],
        limit: 100,
        offset: 0,
        count: 0
    };

    dggal: DGGAL | null = null;

    constructor(
        private configurationService: ConfigurationService,
        private chatService: ChatService,
        private messageService: MessageService,
        private errorService: ErrorService
    ) {

        DGGAL.init({
            locateFile: (filename: string) => {
                return 'assets/wasm/libdggal_c_fn.js.0.0.wasm'
            }
        }).then((dggal: DGGAL) => {
            this.dggal = dggal;
        })

        // /*
        //  * The map should reload when the geo objects change, the styles change, or the neighbors change
        //  */
        // this.onMapObjectsChange = combineLatest([this.geoObjects$, this.neighbors$])
        //     .pipe(withLatestFrom(this.styles$, this.zoomMap$))
        //     .subscribe(([[geoObjects, neighbors], styles, zoomMap]) => {
        //         this.geoObjects = geoObjects;
        //         this.neighbors = neighbors;
        //         this.resolvedStyles = styles;
        //         this.zoomMap = zoomMap;

        //         this.render();
        //     });

        // this.onVectorLayersChange = this.vectorLayers$.subscribe(() => {
        //     this.renderVectorLayers();
        // });

        this.selectedObject$
            .pipe(takeUntilDestroyed())
            .pipe(withLatestFrom(this.zoomMap$, this.styles$)).subscribe(([object, zoomMap, styles]) => {
                this.resolvedStyles = styles;

                this.selectObject(object, zoomMap);

                // Selecting or unselecting an object can change the map size. If we don't resize, we can end up with weird white bars on the side when the attribute panel goes away.
                setTimeout(() => {
                    this.map?.resize();
                }, 0);
            });

        // this.onHighlightedObjectChange = this.highlightedObject$.subscribe(object => {
        //     this.highlightObject(object == null ? undefined : object.properties.uri);
        // });

        this.workflowStep$.subscribe(step => {
            this.workflowStep = step;

            this.chatMinimized = step == WorkflowStep.MinimizeChat;
        });

        this.page$
            .pipe(takeUntilDestroyed())
            .pipe(withLatestFrom(this.styles$, this.zoomMap$))
            .subscribe(([page, styles, zoomMap]) => {
                this.page = page;
                this.geoObjects = page.locations;
                this.resolvedStyles = styles;

                this.render();
            });

        this.zones$
            .pipe(takeUntilDestroyed())
            .subscribe(zones => {
                if (this.initialized) {
                    const geojson: any = {
                        type: "FeatureCollection",
                        features: zones
                    }

                    const source = this.map!.getSource('data') as GeoJSONSource;
                    source.setData(geojson);
                }
            })

        this.dggsjson$
            .pipe(takeUntilDestroyed())
            .subscribe(dggsjsons => {

                if (this.initialized) {
                    const features = dggsjsons.flatMap(dggsjson => this.dggsjsonToFeatures(dggsjson));

                    const geojson: any = {
                        type: "FeatureCollection",
                        features: features
                    }

                    const arr = features.map(feature => feature.properties.value);
                    let minVal = Math.min(...arr);
                    let maxVal = Math.max(...arr);

                    this.map!.setPaintProperty(
                        'data-shape',
                        'fill-color',
                        [
                            'interpolate',
                            ['linear'],
                            ['get', 'value'],
                            minVal, GREEN,
                            maxVal, RED
                        ],
                    );

                    const source = this.map!.getSource('data') as GeoJSONSource;
                    source.setData(geojson);
                }
            })

    }

    ngOnInit(): void {
        this.configurationService.get().then(configuration => {
            this.store.dispatch(ExplorerActions.setConfiguration(configuration));
        }).catch(error => this.errorService.handleError(error));
    }

    ngOnDestroy(): void {
    }

    cancelDisambiguation() {
        this.store.dispatch(ExplorerActions.setPage({
            page: {
                locations: [],
                limit: 100,
                offset: 0,
                count: 0
            }
        }));
        this.store.dispatch(ExplorerActions.mergeWorkflowStep({ step: WorkflowStep.AiChatAndResults }));
    }

    disambiguate() {
        this.store.dispatch(ExplorerActions.setPage({
            page: {
                locations: [],
                limit: 100,
                offset: 0,
                count: 0
            }
        }));

        this.store.dispatch(ExplorerActions.mergeWorkflowStep({
            step: WorkflowStep.AiChatAndResults, data: {
                action: 'NAME_RESOLUTION',
                uri: this.selectedObject!.properties.uri,
                code: this.selectedObject!.properties.code,
                label: this.selectedObject!.properties.label
            }
        }));
    }

    minimizeChat() {
        if (!this.chatMinimized) {
            this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.MinimizeChat }));
            this.chatMinimized = true;
        }
        else {
            this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.AiChatAndResults }));
            this.chatMinimized = false;
        }
    }

    onTabChange(event: any) {
        this.activeTab = event;
    }

    ngAfterViewInit() {
        this.initializeMap();
    }

    render(): void {
        if (this.initialized) {
            // Clear the map
            this.clearAllMapData();

            // Handle the geo objects
            const types = Object.keys(this.geoObjectsByType());

            // Order the types by the order defined in their style config
            this.orderedTypes = types.sort((a, b) => {
                return (this.resolvedStyles[a]?.order ?? 999) - (this.resolvedStyles[b]?.order ?? 999);
            });

            this.calculateTypeLegend();

            this.mapGeoObjects();

            // if (this.zoomMap) {
            this.zoomToAll();
            // }

            // this.renderHighlights();

            // this.renderedObjects = this.allGeoObjects().map(obj => obj.properties.uri);
        }
    }


    renderVectorLayers(): void {
        if (this.initialized) {
            // Clear the map
            this.clearVectorLayers();

            // Handle the vector layers
            this.mapVectorLayers();
        }
    }

    calculateTypeLegend() {
        let oldTypeLegend = JSON.parse(JSON.stringify(this.typeLegend));
        this.typeLegend = {};

        this.orderedTypes.forEach(type => {
            this.typeLegend[type] = {
                label: this.labelForType(type),
                color: this.resolvedStyles[type].color,
                visible: (oldTypeLegend[type] == null ? true : oldTypeLegend[type].visible),
                included: (oldTypeLegend[type] == null ? true : oldTypeLegend[type].included)
            }
        });
    }

    toggleTypeLegend(legend: any): void {
        legend.visible = !legend.visible;
        this.render();
    }

    labelForType(typeUri: string): string {
        if (this.resolvedStyles[typeUri].label) {
            return this.resolvedStyles[typeUri].label as string;
        } else {
            return ExplorerComponent.uriToLabel(typeUri);
        }
    }

    public static uriToLabel(uri: string): string {
        let i = uri.lastIndexOf("#");
        if (i == -1) return uri;

        return uri.substring(i + 1);
    }

    getTypeLegend() { return this.typeLegend; }

    clearAllMapData() {
        if (!this.map) return;

        this.map!.getStyle().layers.forEach(layer => {
            if (this.map!.getLayer(layer.id) && this.baseLayers[0].id !== layer.id) {
                const source = this.map!.getSource((layer as any).source);

                if (source && source.id !== 'data' && source.type !== "vector") {
                    this.map!.removeLayer(layer.id);
                }
            }
        });

        Object.keys(this.map!.getStyle().sources).forEach(sourceId => {
            const source = this.map!.getSource(sourceId);

            if (sourceId !== 'mapbox'
                && sourceId !== 'data'
                && source
                && source.type !== "vector") {
                this.map!.removeSource(sourceId);
            }
        });
    }

    clearVectorLayers() {
        if (!this.map) return;

        this.map!.getStyle().layers.forEach(layer => {
            if (this.map!.getLayer(layer.id) && this.baseLayers[0].id !== layer.id) {
                if (this.map!.getSource((layer as any).source)?.type === "vector") {
                    this.map!.removeLayer(layer.id);
                }
            }
        });

        Object.keys(this.map!.getStyle().sources).forEach(source => {
            if (this.map!.getSource(source) && source !== 'mapbox' && this.map!.getSource(source)?.type === "vector") {
                this.map!.removeSource(source);
            }
        });
    }

    mapVectorLayers() {

        if (!this.map) return;

        const layers = this.map!.getStyle().layers;

        const baseLayer = layers.length > 1 ? layers[1].id : null;

        // Assuming the base layer is the first layer on the map
        this.vectorLayers$.pipe(take(1)).subscribe(layers => {
            [...layers].filter(l => l.enabled).forEach(layer => {

                this.map!.addSource(layer.id, {
                    type: "vector",
                    tiles: [
                        layer.url
                    ],
                    promoteId: layer.codeProperty
                });

                // Add the hierarchy label layer
                this.map!.addLayer({
                    "id": layer.id + "-label",
                    "source": layer.id,
                    "source-layer": layer.sourceLayer,
                    "type": "symbol",
                    "paint": {
                        "text-color": "black",
                        "text-halo-color": "#fff",
                        "text-halo-width": 2
                    },
                    "layout": {
                        "text-field": ["get", layer.labelProperty],
                        "text-font": ["NotoSansRegular"],
                        "text-offset": [0, 0.6],
                        "text-anchor": "top",
                        "text-size": 12,
                    },
                }, baseLayer as string);

                if (layer.geometryType === "Polygon") {
                    // Add the hierarchy polygon layer
                    this.map!.addLayer({
                        "id": layer.id + "-shape",
                        "source": layer.id,
                        "source-layer": layer.sourceLayer,
                        "type": "fill",
                        "paint": {
                            'fill-color': [
                                "case",
                                ["boolean", ["feature-state", "selected"], false],
                                SELECTED_COLOR,
                                layer.color
                            ],
                            "fill-opacity": 0.8,
                            "fill-outline-color": "black"
                        }
                    }, layer.id + "-label");
                }
                else if (layer.geometryType === "Line") {
                    // Add the hierarchy polygon layer
                    this.map!.addLayer({
                        "id": layer.id + "-shape",
                        "source": layer.id,
                        "source-layer": layer.sourceLayer,
                        "type": "line",
                        "paint": {
                            'line-color': [
                                "case",
                                ["boolean", ["feature-state", "selected"], false],
                                SELECTED_COLOR,
                                layer.color
                            ]
                        }
                    }, layer.id + "-label");
                }
                else if (layer.geometryType === "Point") {
                    // Add the hierarchy polygon layer
                    this.map!.addLayer({
                        "id": layer.id + "-shape",
                        "source": layer.id,
                        "source-layer": layer.sourceLayer,
                        "type": "circle",
                        "paint": {
                            "circle-radius": 20,
                            "circle-color": [
                                "case",
                                ["boolean", ["feature-state", "selected"], false],
                                SELECTED_COLOR,
                                layer.color
                            ],
                            "circle-stroke-width": 5,
                            "circle-stroke-color": "#FFFFFF"
                        }
                    }, layer.id + "-label");
                }
                else {
                    console.log('Unknown geometry type', layer)
                }

            });
        });

    }

    mapGeoObjects() {
        // setTimeout(() => {

        // The layers are organized by the type, so we have to group geoObjects by type and create a layer for each type
        let gosByType = this.geoObjectsByType();

        let allGeoObjects = this.allGeoObjects();

        for (let i = this.orderedTypes.length - 1; i >= 0; --i) {
            let type = this.orderedTypes[i];
            let geoObjects = gosByType[type];

            if (geoObjects.length == 0) continue;
            if (geoObjects[0].geometry == null) continue; // TODO : Find this out at the type level...
            if (!this.typeLegend[type].visible) continue;

            let geojson: any = {
                type: "FeatureCollection",
                features: []
            }

            for (let i = 0; i < allGeoObjects.length; ++i) {
                if (allGeoObjects[i].properties.type !== type) continue;

                let geoObject = allGeoObjects[i];

                geojson.features.push(geoObject);
            }

            this.map?.addSource(type, {
                type: "geojson",
                data: geojson,
                promoteId: 'uri' // A little surprised at mapbox here, but without this param it won't use the id property for the feature id
            });

            // Label layer
            this.map?.addLayer({
                id: type + "-LABEL",
                source: type,
                type: "symbol",
                paint: {
                    "text-color": "black",
                    "text-halo-color": "#fff",
                    "text-halo-width": 2
                },
                layout: {
                    "text-field": ["get", "label"],
                    "text-font": ["NotoSansRegular"],
                    "text-offset": [0, 0.6],
                    "text-anchor": "top",
                    "text-size": 12
                }
            });

            // this.addHighlightLayers(type, geoObjects[0].geometry.type.toUpperCase());
            this.map?.addLayer(this.layerConfig(type, geoObjects[0].geometry.type.toUpperCase()), type + "-LABEL");
        }
        // },10);
    }

    private addHighlightLayers(type: string, geometryType: string) {
        if (geometryType === "MULTIPOLYGON" || geometryType === "POLYGON") {
            this.map!.addLayer({
                "id": "hover-" + type,
                "type": "fill",
                "source": type,
                "paint": {
                    'fill-color': [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        SELECTED_COLOR,
                        HOVER_COLOR
                    ],
                    'fill-opacity': 0.5,
                    "fill-outline-color": "black"
                },
                filter: ["all",
                    ["==", "uri", "NONE"] // start with a filter that doesn"t select anything
                ]
            });
        } else if (geometryType === "POINT" || geometryType === "MULTIPOINT") {
            this.map!.addLayer({
                "id": "hover-" + type,
                "type": "circle",
                "source": type,
                "paint": {
                    "circle-radius": 10,
                    "circle-color": [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        SELECTED_COLOR,
                        HOVER_COLOR
                    ],
                    "circle-stroke-width": 2,
                    "circle-stroke-color": "#FFFFFF"
                },
                filter: ["all",
                    ["==", "uri", "NONE"] // start with a filter that doesn"t select anything
                ]
            });
        } else if (geometryType === "LINE" || geometryType === "MULTILINE" || geometryType === "MULTILINESTRING") {
            this.map!.addLayer({
                "id": "hover-" + type,
                "type": "line",
                "source": type,
                "paint": {
                    "line-color": [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        SELECTED_COLOR,
                        HOVER_COLOR
                    ],
                    "line-width": 3,
                },
                filter: ["all",
                    ["==", "uri", "NONE"] // start with a filter that doesn"t select anything
                ]
            });
        }
    }

    private layerConfig(type: string, geometryType: string): any {
        let layerConfig: any = {
            id: type,
            source: type
        };

        if (geometryType === "MULTIPOLYGON" || geometryType === "POLYGON") {
            layerConfig.paint = {
                'fill-color': [
                    "case",
                    ["boolean", ["feature-state", "selected"], false],
                    SELECTED_COLOR,
                    this.typeLegend[type].color
                ],
                'fill-opacity': 0.8,
                "fill-outline-color": "black"
            };
            layerConfig.type = "fill";
        } else if (geometryType === "POINT" || geometryType === "MULTIPOINT") {
            layerConfig.paint = {
                "circle-radius": 10,
                "circle-color": [
                    "case",
                    ["boolean", ["feature-state", "selected"], false],
                    SELECTED_COLOR,
                    this.typeLegend[type].color
                ],
                "circle-stroke-width": 2,
                "circle-stroke-color": "#FFFFFF"
            };
            layerConfig.type = "circle";
        } else if (geometryType === "LINE" || geometryType === "MULTILINE" || geometryType === "MULTILINESTRING") {
            layerConfig.layout = {
                "line-join": "round",
                "line-cap": "round"
            }
            layerConfig.paint = {
                "line-color": [
                    "case",
                    ["boolean", ["feature-state", "selected"], false],
                    SELECTED_COLOR,
                    this.typeLegend[type].color
                ],
                "line-width": 3
            }
            layerConfig.type = "line";
        } else {
            // eslint-disable-next-line no-console
            console.log("Unexpected geometry type [" + geometryType + "]");
        }

        return layerConfig;
    }

    allGeoObjects(): GeoObject[] {
        // let all = this.geoObjects.concat(this.neighbors);

        // if (this.selectedObject)
        //     all.push(this.selectedObject);

        // // Enforce each GeoObject only occurs once
        // const seen = new Set<string>();
        // return all.filter(obj => seen.has(obj.properties.uri) ? false : seen.add(obj.properties.uri));

        return this.geoObjects;
    }

    geoObjectsByType(): { [key: string]: GeoObject[] } {
        let gos: { [key: string]: GeoObject[] } = {};
        var allGeoObjects = this.allGeoObjects();

        for (let i = 0; i < allGeoObjects.length; ++i) {
            let geoObject = allGeoObjects[i];

            if (gos[geoObject.properties.type] === undefined) {
                gos[geoObject.properties.type] = [];
            }

            gos[geoObject.properties.type].push(geoObject);
        }

        return gos;
    }


    public getObjectUrl(go: GeoObject): string {
        return ExplorerComponent.getObjectUrl(go);
    }

    public static getObjectUrl(go: GeoObject): string {
        return go.properties.uri;
    }

    /*
     * Fit the map to the bounds of all of the layers
     */
    zoomToAll() {
        if (this.allGeoObjects().length > 0) {

            const layerBounds = this.orderedTypes.map(type => {
                // TODO: Is there a better way to get the layer data from the map?
                const source = this.map?.getSource(type);

                if (source instanceof GeoJSONSource) {
                    const data = ((source as GeoJSONSource)._data) as AllGeoJSON;
                    return bboxPolygon(bbox(data))
                }

                return null;
            }).filter(a => a != null)

            if (layerBounds.length == 0) return;

            const allBounds = bbox(layerBounds.reduce((a: any, b: any) => {
                if (a == null) {
                    return b;
                }

                if (b == null) {
                    return a;
                }

                try {
                    return union(a.geometry, b.geometry) as any
                }
                catch (e) {
                    return b.geometry
                }
            }, null)) as LngLatBoundsLike

            this.map?.fitBounds(allBounds, { padding: 50 })
        }
    }

    /*
     * Zooms to a specific GeoObject
     */
    zoomTo(uri: string) {
        let geoObject = this.allGeoObjects().find(go => go.properties.uri === uri);
        if (geoObject == null) return;

        let geojson = geoObject.geometry as any;

        const geometryType = geojson.type.toUpperCase();

        if (geometryType === "MULTIPOINT" || geometryType === "POINT") {
            let coords = geojson.coordinates;

            if (coords) {
                let bounds = new LngLatBounds();
                coords.forEach((coord: any) => {
                    bounds.extend(coord);
                });

                let center = bounds.getCenter();
                let pt = new LngLat(center.lng, center.lat);

                // this.map?.flyTo({
                //     center: pt,
                //     zoom: 9,
                //     essential: true
                // });
            }
        } else if (geometryType === "MULTIPOLYGON" || geometryType === "MIXED") {
            let coords = geojson.coordinates;

            if (coords) {
                let bounds = new LngLatBounds();
                coords.forEach((polys: any) => {
                    polys.forEach((subpoly: any) => {
                        subpoly.forEach((coord: any) => {
                            bounds.extend(coord);
                        });
                    });
                });

                // this.map?.fitBounds(bounds, {
                //     padding: 20
                // });
            }
        } else if (geometryType === "POLYGON") {
            let coords = geojson.coordinates;

            if (coords) {
                let bounds = new LngLatBounds();
                coords.forEach((polys: any) => {
                    polys.forEach((coord: any) => {
                        bounds.extend(coord);
                    });
                });

                // this.map?.fitBounds(bounds, {
                //     padding: 20
                // });
            }
        } else if (geometryType === "LINE" || geometryType === "MULTILINE") {
            let coords = geojson.coordinates;

            if (coords) {
                let bounds = new LngLatBounds();
                coords.forEach((lines: any) => {
                    lines.forEach((subline: any) => {
                        subline.forEach((coord: any) => {
                            bounds.extend(coord);
                        });
                    });
                });

                // this.map?.fitBounds(bounds, {
                //     padding: 20
                // });
            }
        } else if (geometryType === "MULTILINESTRING") {
            let coords = geojson.coordinates;

            if (coords) {
                let bounds = new LngLatBounds();
                coords.forEach((lines: any) => {
                    lines.forEach((lngLat: any) => {
                        bounds.extend(lngLat);
                    });
                });

                // this.map?.fitBounds(bounds, {
                //     padding: 20
                // });
            }
        }
    }

    drop(event: CdkDragDrop<string[]>) {
        moveItemInArray(this.orderedTypes, event.previousIndex, event.currentIndex);

        for (let i = 0; i < this.orderedTypes.length; ++i) {
            this.map?.moveLayer(this.orderedTypes[i], i > 0 ? this.orderedTypes[i - 1] : undefined);
            this.map?.moveLayer(this.orderedTypes[i] + "-LABEL", i > 0 ? this.orderedTypes[i - 1] + "-LABEL" : undefined);
        }
    }

    initializeMap() {
        const layer = this.baseLayers[0];

        const mapConfig: any = {
            container: "map",
            center: [-97.14998834894477, 49.71651508811458],
            zoom: 8,
            // bounds: [[-97.14704, 49.8844], [-66.9326, 49.5904]], // USA
            // fitBoundsOptions: { padding: 100 },
            style: {
                version: 8,
                name: layer.name,
                metadata: {
                    "mapbox:autocomposite": true
                },
                sources: {
                    mapbox: {
                        'type': 'raster',
                        'tiles': [
                            'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
                        ],
                        'tileSize': 512
                    }
                },
                glyphs: environment.apiUrl + "glyphs/{fontstack}/{range}.pbf",
                layers: [
                    {
                        id: layer.id,
                        type: "raster",
                        source: "mapbox",
                        'minzoom': 0,
                        'maxzoom': 22
                        // "source-layer": "mapbox_satellite_full"
                    }
                ]
            },
            attributionControl: false
        };

        mapConfig.logoPosition = "bottom-right";

        this.map = new Map(mapConfig);

        this.map!.on("load", () => {
            this.initMap();

            // this.renderVectorLayers();


            const geojson: any = {
                type: 'FeatureCollection',
                features: []
            }

            this.map?.addSource('data', {
                type: 'geojson',
                data: geojson
            });

            this.map!.addLayer({
                'id': 'data-label',
                'source': 'data',
                'type': 'symbol',
                'paint': {
                    'text-color': 'black',
                    'text-halo-color': '#fff',
                    'text-halo-width': 2
                },
                'layout': {
                    'text-field': ['get', 'value'],
                    'text-font': ['NotoSansRegular'],
                    'text-offset': [0, 0.6],
                    'text-anchor': 'top',
                    'text-size': 12,
                },
            });

            this.map!.addLayer({
                id: 'data-point',
                type: 'circle', // Layer type (e.g., circle, line, fill)
                source: 'data',
                paint: {
                    'circle-radius': 7,
                    'circle-color': SELECTED_COLOR
                },
                'filter': ['==', '$type', 'Point']
            }, 'data-label');

            this.map!.addLayer({
                'id': 'data-shape',
                'source': 'data',
                'type': 'fill',
                'paint': {
                    'fill-color': [
                        'interpolate',
                        ['linear'],
                        ['get', 'value'],
                        0, GREEN,
                        10, RED
                    ],
                    'fill-opacity': 0.8,
                    'fill-outline-color': 'black'
                },
                'filter': ['==', '$type', 'Polygon']
            }, 'data-point');

            this.initialized = true;
        });

        // this.map.on('mousemove', this.highlightSelectedLayerOnMouseMove);
    }

    // highlightSelectedLayerOnMouseMove = debounce((e: any) => {
    //     const features = this.getSortedFeature(e);
    //     const feature = features.find(f => f.properties['uri'] != null);

    //     if (feature) {
    //         const uri = feature.properties['uri'];
    //         const highlightedObject = this.allGeoObjects().find(go => go.properties.uri === uri);
    //         this.store.dispatch(ExplorerActions.highlightGeoObject({ object: highlightedObject! }));
    //         this.map!.getCanvas().style.cursor = 'pointer';
    //     } else {
    //         // Reset if no valid feature is found
    //         if (this.highlightedObject) {
    //             this.store.dispatch(ExplorerActions.highlightGeoObject(null));
    //             this.map!.getCanvas().style.cursor = '';
    //         }
    //     }
    // }, 5);

    getSortedFeature(e: any): MapGeoJSONFeature[] {
        const features = this.map!.queryRenderedFeatures(e.point);

        // Get the map's layer order
        const layerOrder = this.map!.getStyle().layers!.map(layer => layer.id);

        // Sort features based on layer order, but push label layers to the bottom
        features.sort((a, b) => {
            const aIsLabel = a.layer.id.endsWith('-LABEL');
            const bIsLabel = b.layer.id.endsWith('-LABEL');

            if (aIsLabel && !bIsLabel) return 1;  // Move labels down
            if (!aIsLabel && bIsLabel) return -1; // Move non-labels up

            // Otherwise, sort by layer order (higher index = top-most)
            return layerOrder.indexOf(b.layer.id) - layerOrder.indexOf(a.layer.id);
        });

        return features;
    }



    initMap(): void {
        // Add zoom and rotation controls to the map.
        this.map!.addControl(new AttributionControl({ compact: true }), "bottom-right");
        this.map!.addControl(new NavigationControl({ visualizePitch: true }), "bottom-right");

        this.map!.on('click', (e) => {
            this.handleMapClickEvent(e);
        });
    }

    handleMapClickEvent(e: any): void {
        // this.vectorLayers$.pipe(take(1)).subscribe(vectorLayers => {

        //     // Clear the feature state of all vector layers
        //     vectorLayers.forEach(layer => {
        //         if (layer.enabled) {
        //             this.map!.removeFeatureState({ source: layer.id, sourceLayer: layer.sourceLayer });
        //         }
        //     })

        //     const features = this.getSortedFeature(e);

        //     if (features.length > 0) {
        //         const feature = features[0];

        //         const source = this.map!.getSource(feature.source);

        //         // Get the layer definition
        //         if (source?.type === 'vector') {
        //         }
        //         else {
        //         }
        //     } else {
        //         // this.store.dispatch(ExplorerActions.selectGeoObject(null));
        //     }
        // })
    }

    renderHighlights() {
        if (this.selectedObject != null) {
            const index = this.orderedTypes.findIndex(t => t === this.selectedObject!.properties.type);

            if (index !== -1) {
                this.map!.setFeatureState({ source: this.selectedObject.properties.type, id: this.selectedObject.id }, { selected: true });
            }
        }
    }

    highlightObject(uri?: string) {
        if (uri != null && this.selectedObject != null && uri == this.selectedObject.properties.uri) return;

        let oldHighlight = this.highlightedObject;
        let newHighlight = (uri == null) ? null : this.allGeoObjects().find(go => go.properties.uri === uri);

        if (oldHighlight != null) {
            this.map!.setFilter("hover-" + oldHighlight.properties.type, ["all", ["==", "uri", "NONE"]]);
        }

        if (newHighlight != null) {
            this.map!.setFilter("hover-" + newHighlight.properties.type, ["all", ["==", "uri", newHighlight.id]]);
        }

        this.highlightedObject = newHighlight;
    }

    selectObject(geoObject: GeoObject | null, zoomTo = false): void {

        let previousSelected = this.selectedObject;

        if (geoObject != null) {
            // If its already selected do nothing
            if (this.selectedObject != null && this.selectedObject.properties.uri === geoObject.properties.uri) return;

            let go = this.allGeoObjects().find(go => go.properties.uri === geoObject.properties.uri);

            this.selectedObject = geoObject;

            if (go == null) {
                this.render();
            }

            this.highlightObject();

            // The geo object does exist on the map
            // if (go != null) {
            // if (zoomTo)
            //     this.zoomTo(this.selectedObject.properties.uri);

            this.renderHighlights();
            // }
        } else {
            this.selectedObject = undefined;
        }

        if (previousSelected != null) {
            this.map!.setFeatureState({ source: previousSelected.properties.type, id: previousSelected.id }, { selected: false });
        }
    }

    toggleVectorLayer(layer: VectorLayer): void {
        const newLayer = { ...layer };
        newLayer.enabled = !newLayer.enabled

        this.store.dispatch(ExplorerActions.setVectorLayer({ layer: newLayer }));
    }

    dggsjsonToFeatures(dggsjson: DggsJson): any {
        const features = [];

        const dggrs = this.dggal!.createDGGRS(dggsjson.dggrs);

        try {
            console.log('Processing zone response: ' + dggsjson.zoneId)
            // const low = BigInt.asUintN(64, 12130488n);

            // console.log("Max depth", dggrs.getMaxDepth());

            const parent = dggrs.getZoneFromTextID(dggsjson.zoneId);

            // console.log('Parent level', dggrs.getZoneLevel(parent));

            // const first = dggrs.getFirstSubZone(parent, 5);

            // console.log('First subzone', first);

            // console.log('Index', dggrs.getSubZoneIndex(parent, first));
            // console.log('At Index', dggrs.getSubZoneAtIndex(parent, 5, BigInt("0")));


            if (dggsjson.values.length > 0 && dggsjson.depths.length > 0) {
                const propertyMap = dggsjson.values[0].properties;
                const keys = Object.keys(propertyMap);
                const relativeDepth = parseInt(dggsjson.depths[0]);

                if (keys.length > 0) {
                    const propertyData = propertyMap[keys[0]];
                    const data = propertyData[0].data;
                    const zones = dggrs.getSubZones(parent, relativeDepth);

                    // if (data.length !== zones.length) {
                    //     console.log('Subzone count mismatch for zone [' + dggsjson.zoneId + ']: ' + data.length + ", " + zones.length)

                    //     console.log('Data: ', data)
                    //     console.log('Zones: ', zones)

                    //     console.log('First subzone', dggrs.getFirstSubZone(parent, 5));
                    //     console.log('At Index', dggrs.getSubZoneAtIndex(parent, 5, BigInt(0)));
                    // }


                    for (let i = 0; i < data.length; i++) {

                        if (data[i] != null) {
                            const zone = dggrs.getSubZoneAtIndex(parent, 5, BigInt(i));

                            if (zone != null) {

                                // console.log('Creating geometry for zone: ' + dggrs.getZoneTextID(zone))

                                // const vertices = dggrs.getZoneRefinedWGS84Vertices(zone, 0);
                                const vertices = dggrs.getZoneWGS84Vertices(zone);


                                const coordsDeg = (Array.isArray(vertices) ? vertices : []).map(v => [v.lon * 180 / Math.PI, v.lat * 180 / Math.PI]);

                                if (coordsDeg.length > 0) {
                                    const first = coordsDeg[0];
                                    const last = coordsDeg[coordsDeg.length - 1];

                                    if (first[0] !== last[0] || first[1] !== last[1]) {
                                        coordsDeg.push([first[0], first[1]]);
                                    }
                                } else {
                                    console.log('No vertices returned for zone');
                                    return;
                                }

                                try {
                                    const feature = turf.polygon([coordsDeg], { value: data[i] });

                                    features.push(feature);
                                }
                                catch (e) {
                                    console.log('Unable to create geometry from coordinates', coordsDeg)
                                }

                            }
                        }
                    }
                }
            }

            return features;
        }
        catch (e) {
            console.log('Error creating geojson from dggsjosn response', e);
        }
        finally {
            dggrs.delete();

        }

    }
}
