import { v4 as uuidv4 } from 'uuid';
import { createReducer, on, createActionGroup, props, createFeatureSelector, createSelector } from '@ngrx/store';
import { ChatMessage } from '../models/chat.model';
import { MockUtil } from '../mock-util';

/*
*/
const initialMessage = ``;

export const ChatActions = createActionGroup({
    source: 'chat',
    events: {
        'Add Message': props<ChatMessage>(),
        'Update Message': props<ChatMessage>(),
        'setMessageAndSession': props<{ messages: ChatMessage[], sessionId: string }>(),
    },
});

export interface ChatStateModel {
    sessionId: string;
    messages: ChatMessage[];
}

export const initialState: ChatStateModel = {
    messages: [{ id: '1', sender: 'system', text: initialMessage, ambiguous: false, purpose: 'info' }],
    sessionId: uuidv4()
}

// if (environment.mockRequests)
initialState.messages = MockUtil.messages.map(m => m);


export const chatReducer = createReducer(
    initialState,
    on(ChatActions.addMessage, (state, message) => {

        const messages = [...state.messages];
        messages.push(message);

        return { ...state, messages }
    }),
    on(ChatActions.updateMessage, (state, message) => {

        const messages = [...state.messages];

        const index = messages.findIndex(m => m.id === message.id)

        if (index != -1) {
            messages[index] = message;
            return { ...state, messages }
        }

        return { ...state }
    }),
    on(ChatActions.setMessageAndSession, (state, wrapper) => {
        return { ...state, messages: wrapper.messages, sessionId: wrapper.sessionId }
    }),
);

const selector = createFeatureSelector<ChatStateModel>('chat');

export const getMessages = createSelector(selector, (s) => {
    return s.messages;
});

export const getSessionId = createSelector(selector, (s) => {
    return s.sessionId;
});
