import { Component, AfterViewInit, TemplateRef, ViewChild, inject, OnInit, OnDestroy } from '@angular/core';
import { Map, NavigationControl, AttributionControl, LngLatBounds, LngLat, GeoJSONSource, LngLatBoundsLike, MapGeoJSONFeature, Source, Feature } from "maplibre-gl";
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PanelModule } from 'primeng/panel';
import { ToastModule } from 'primeng/toast';
import { CheckboxModule } from 'primeng/checkbox';
import { Observable, Subscription, take, withLatestFrom } from 'rxjs';
import { Store } from '@ngrx/store';
import { v4 as uuidv4 } from 'uuid';

import { GeoObject } from '../models/geoobject.model';
import { StyleConfig } from '../models/style.model';

import { AttributePanelComponent } from '../attribute-panel/attribute-panel.component';
import { AichatComponent } from '../aichat/aichat.component';
import { ResultsTableComponent } from '../results-table/results-table.component';
import { ConfigurationService } from '../service/configuration-service.service';
import { defaultQueries, SELECTED_COLOR, HOVER_COLOR } from './defaultQueries';
import { AllGeoJSON, bbox, bboxPolygon, center, union } from '@turf/turf';
import { ExplorerService } from '../service/explorer.service';
import { ErrorService } from '../service/error-service.service';
import { ExplorerActions, getNeighbors, getObjects, getStyles, getVectorLayers, getZoomMap, highlightedObject, selectedObject, getWorkflowStep, WorkflowStep, getPage, getZones, getBbox, getWorkflowData } from '../state/explorer.state';
import { TabsModule } from 'primeng/tabs';
import { debounce } from 'lodash';
import { VectorLayer } from '../models/vector-layer.model';
import { environment } from '../../environments/environment';
import { faArrowLeft, faArrowRight, faDownLeftAndUpRightToCenter, faUpRightAndDownLeftFromCenter } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ButtonModule } from 'primeng/button';
import { ChatMessage, LocationPage } from '../models/chat.model';
import { TooltipModule } from 'primeng/tooltip';
import { ChatActions } from '../state/chat.state';
import { ChatService } from '../service/chat-service.service';

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

    public static GEO = "http://www.opengis.net/ont/geosparql#";

    public static GEO_FEATURE = ExplorerComponent.GEO + "Feature";

    public static GEO_WKT_LITERAL = ExplorerComponent.GEO + "wktLiteral";

    private store = inject(Store);

    zones$: Observable<Feature[]> = this.store.select(getZones);

    bbox$: Observable<LngLatBoundsLike | null> = this.store.select(getBbox);

    zoomMap$: Observable<boolean> = this.store.select(getZoomMap);

    // geoObjects$: Observable<GeoObject[]> = this.store.select(getObjects);

    // geoObjects: GeoObject[] = [];

    // renderedObjects: string[] = [];

    // onMapObjectsChange: Subscription;

    // neighbors$: Observable<GeoObject[]> = this.store.select(getNeighbors);

    // neighbors: GeoObject[] = [];

    styles$: Observable<StyleConfig> = this.store.select(getStyles);

    selectedObject$: Observable<GeoObject | null> = this.store.select(selectedObject);

    onSelectedObjectChange: Subscription;

    // highlightedObject$: Observable<GeoObject | null> = this.store.select(highlightedObject);

    // onHighlightedObjectChange: Subscription;

    workflowStep$: Observable<WorkflowStep> = this.store.select(getWorkflowStep);

    workflowData$: Observable<any> = this.store.select(getWorkflowData);

    page$: Observable<LocationPage> = this.store.select(getPage);

    onPageChange: Subscription;

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
            name: "Satellite",
            label: "Satellite",
            id: "satellite-v9",
            sprite: "mapbox://sprites/mapbox/satellite-v9",
            url: "mapbox://mapbox.satellite",
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

    constructor(
        private configurationService: ConfigurationService,
        private chatService: ChatService,
        private errorService: ErrorService
    ) {

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

        this.onSelectedObjectChange = this.selectedObject$.pipe(withLatestFrom(this.zoomMap$, this.styles$)).subscribe(([object, zoomMap, styles]) => {
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

        this.onPageChange = this.page$.subscribe(page => {
            this.page = page;
        });

        this.zones$.subscribe(zones => {

            if (this.initialized) {
                const geojson: any = {
                    type: "FeatureCollection",
                    features: zones
                }

                const source = this.map?.getSource('data') as GeoJSONSource;
                source.setData(geojson);
            }
        })

        this.bbox$.subscribe(bbox => {
            if (this.initialized && bbox != null) {
                this.map?.fitBounds(bbox, { padding: 50 });
            }
        });
    }

    ngOnInit(): void {
        this.configurationService.get().then(configuration => {
            this.store.dispatch(ExplorerActions.setConfiguration(configuration));
        }).catch(error => this.errorService.handleError(error));
    }

    ngOnDestroy(): void {
        // this.onMapObjectsChange.unsubscribe();
        // this.onVectorLayersChange.unsubscribe();
        // this.onSelectedObjectChange.unsubscribe();
        // this.onHighlightedObjectChange.unsubscribe();
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
        this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.AiChatAndResults }));
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

        if (this.selectedObject != null) {

            const message: ChatMessage = {
                id: uuidv4(),
                sender: 'user',
                text: this.selectedObject?.properties.code,
                loading: false,
                purpose: 'standard'
            };

            this.store.dispatch(ChatActions.addMessage(message));

            const system: ChatMessage = {
                id: uuidv4(),
                sender: 'system',
                text: '',
                loading: true,
                purpose: 'standard'
            };

            this.store.dispatch(ChatActions.addMessage(system));

            this.loading = true;

            this.workflowData$.pipe(take(1)).subscribe(data => {

                this.chatService.zones(this.selectedObject?.properties.uri, data.category).then((message) => {

                    if (message.type === 'ZONES') {
                        if (message.collection != null) {
                            this.store.dispatch(ExplorerActions.setZones({ collection: message.collection }));
                        }

                        this.store.dispatch(ChatActions.updateMessage({
                            ...system,
                            text: "See on map!",
                            loading: false,
                            data: message.collection
                        }));
                    }

                    this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.AiChatAndResults }));

                }).catch((error: any) => {
                    this.errorService.handleError(error)

                    this.store.dispatch(ChatActions.updateMessage({
                        ...system,
                        text: 'An error occurred',
                        loading: false,
                        purpose: 'info'
                    }));

                }).finally(() => {
                    this.loading = false;
                })
            });
        }
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
            // // Clear the map
            // this.clearAllMapData();

            // // Handle the geo objects
            // const types = Object.keys(this.geoObjectsByType());

            // // Order the types by the order defined in their style config
            // this.orderedTypes = types.sort((a, b) => {
            //     return (this.resolvedStyles[a]?.order ?? 999) - (this.resolvedStyles[b]?.order ?? 999);
            // });

            // this.calculateTypeLegend();

            // this.mapGeoObjects();

            // if (this.zoomMap) {
            //     this.zoomToAll();
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
        var oldTypeLegend = JSON.parse(JSON.stringify(this.typeLegend));
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
                if (this.map!.getSource((layer as any).source)?.type !== "vector") {
                    this.map!.removeLayer(layer.id);
                }
            }
        });

        Object.keys(this.map!.getStyle().sources).forEach(source => {
            if (this.map!.getSource(source) && source !== 'mapbox' && this.map!.getSource(source)?.type !== "vector") {
                this.map!.removeSource(source);
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
                            "circle-radius": 10,
                            "circle-color": [
                                "case",
                                ["boolean", ["feature-state", "selected"], false],
                                SELECTED_COLOR,
                                layer.color
                            ],
                            "circle-stroke-width": 2,
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
        // Find the index of the first symbol layer in the map style
        const layers = this.map?.getStyle().layers;

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
            }, "data-shape");

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
                    'fill-opacity': 0.5
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
                'fill-opacity': 0.8
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

        return [];
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
                            'https://api.mapbox.com/v4/mapbox.satellite/{z}/{x}/{y}@2x.jpg90?access_token=' + "pk.eyJ1IjoianVzdGlubGV3aXMiLCJhIjoiY2l0YnlpdWRkMDlkNjJ5bzZuMTR3MHZ3YyJ9.Ad0fQd8onRSYR9QZP6VyUw"
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
                // 'filter': ['==', '$type', 'Point']
            }, 'data-label');

            this.map!.addLayer({
                'id': 'data-shape',
                'source': 'data',
                'type': 'fill',
                'paint': {
                    'fill-color': SELECTED_COLOR,
                    'fill-opacity': 0.8,
                    'fill-outline-color': 'black'
                },
                // 'filter': ['==', '$type', 'Polygon']
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
}
