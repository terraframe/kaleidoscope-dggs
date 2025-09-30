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
            console.log(message.content);

            this.store.dispatch(ChatActions.updateMessage({
                ...system,
                text: message.content!,
                loading: false,
                data: message.collection
            }));
        }
        else if (message.type === 'ZONES') {
            this.store.dispatch(ExplorerActions.setZones({ collection: message.collection! }));

            this.store.dispatch(ChatActions.updateMessage({
                ...system,
                text: "See the results on the map!",
                loading: false,
                data: message.collection
            }));
        }
        else if (message.type === 'DISAMBIGUATE') {
            this.store.dispatch(ExplorerActions.setPage({ page: message.page! }));
            this.store.dispatch(ExplorerActions.setWorkflowStep({
                step: WorkflowStep.DisambiguateObject, data: {
                    category: message.category,
                    datetime: message.datetime
                }
            }));
            this.store.dispatch(ExplorerActions.selectGeoObject(null));

            this.store.dispatch(ChatActions.updateMessage({
                ...system,
                text: "There are multiple locations. Please select one.",
                loading: false,
                ambiguous: true,
                data: {
                    page: message.page,
                    category: message.category,
                    datetime: message.datetime
                }
            }));
        }

    }
}
