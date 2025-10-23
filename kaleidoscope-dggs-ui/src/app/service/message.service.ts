import { inject, Injectable } from '@angular/core';
import { ChatMessage, Message } from '../models/chat.model';
import { Store } from '@ngrx/store';
import { ChatActions } from '../state/chat.state';
import { ExplorerActions, WorkflowStep } from '../state/explorer.state';

@Injectable({
    providedIn: 'root'
})
export class MessageService {

    private store = inject(Store);


    constructor() { }

    process(system: ChatMessage, message: Message): void {

        if (message.type === 'BASIC') {
            this.store.dispatch(ChatActions.updateMessage({
                ...system,
                text: message.content!,
                loading: false,
                data: message.collection
            }));
        }
        else if (message.type === 'DGGS_JSON') {
            this.store.dispatch(ExplorerActions.setDggsjson({ dggsjson: message.zones! }));

            this.store.dispatch(ChatActions.updateMessage({
                ...system,
                text: "See the results on the map!",
                loading: false
            }));
        }
        else if (message.type === 'ZONES') {
            this.store.dispatch(ExplorerActions.setZones({ collection: message.collection! }));

            this.store.dispatch(ChatActions.updateMessage({
                ...system,
                text: "See the results on the map!",
                loading: false
            }));
        }
        else if (message.type === 'DISAMBIGUATE') {
            this.store.dispatch(ExplorerActions.setPage({ page: message.page! }));
            this.store.dispatch(ExplorerActions.setWorkflowStep({
                step: WorkflowStep.DisambiguateObject, data: {
                    page: message.page,
                    toolUseId: message.toolUseId,
                    locationName: message.locationName
                }
            }));
            this.store.dispatch(ExplorerActions.selectGeoObject(null));

            this.store.dispatch(ChatActions.updateMessage({
                ...system,
                text: "There are multiple locations. Please select one.",
                loading: false,
                ambiguous: true,
                messageType: 'NAME_RESOLUTION',
                data: {
                    toolUseId: message.toolUseId,
                    locationName: message.locationName
                }
            }));
        }
        else if (message.type === 'FEATURES') {
            this.store.dispatch(ExplorerActions.setPage({ page: message.page!, hasPopulation: (message.population != null) }));
            this.store.dispatch(ExplorerActions.selectGeoObject(null));

            if (message.zones != null) {
                this.store.dispatch(ExplorerActions.setDggsjson({ dggsjson: message.zones! }));
            }

            if (message.population != null) {
                this.store.dispatch(ChatActions.updateMessage({
                    ...system,
                    text: "The total population impacted is " + message.population.toLocaleString('en-US'),
                    loading: false
                }));
            }
            else {
                this.store.dispatch(ChatActions.updateMessage({
                    ...system,
                    text: "See the results on the map!",
                    loading: false
                }));
            }
        }

    }
}
