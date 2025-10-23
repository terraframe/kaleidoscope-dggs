import { Component, HostListener, inject, Input } from '@angular/core';
import { FormsModule } from '@angular/forms'; // <-- Import FormsModule
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Store } from '@ngrx/store';
import { combineLatest, Observable, Subscription, take } from 'rxjs';
import { v4 as uuidv4 } from 'uuid';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faEraser, faUpRightAndDownLeftFromCenter, faUser } from '@fortawesome/free-solid-svg-icons';

import { ChatService } from '../service/chat-service.service';
import { ChatMessage } from '../models/chat.model';
import { ChatActions, getMessages, getSessionId } from '../state/chat.state';
import { ErrorService } from '../service/error-service.service';
import { ExplorerActions, getWorkflowData, getWorkflowStep, WorkflowStep } from '../state/explorer.state';
import { MessageService } from '../service/message.service';

@Component({
  selector: 'aichat',
  imports: [CommonModule, FormsModule, ButtonModule, ProgressSpinnerModule, FontAwesomeModule, TooltipModule],
  templateUrl: './aichat.component.html',
  styleUrl: './aichat.component.scss'
})
export class AichatComponent {
  icon = faEraser;
  public messageUserIcon = faUser;
  public messageSenderIcon = faUpRightAndDownLeftFromCenter;
  private store = inject(Store);

  message: string = '';

  messages$: Observable<ChatMessage[]> = this.store.select(getMessages);
  workflowStep$: Observable<WorkflowStep> = this.store.select(getWorkflowStep);
  workflowData$: Observable<any> = this.store.select(getWorkflowData);

  public loading: boolean = false;
  public mapLoading: boolean = false;

  public renderedMessages: ChatMessage[] = [];

  public minimized: boolean = false;

  constructor(
    private chatService: ChatService,
    private messageService: MessageService,
    private errorService: ErrorService) {

    this.messages$.pipe(takeUntilDestroyed()).subscribe(messages => {
      this.renderedMessages = [...messages].reverse();
    });


    combineLatest([
      this.workflowStep$,
      this.workflowData$
    ]).pipe(takeUntilDestroyed()).subscribe(([step, data]) => {
      if (step === WorkflowStep.AiChatAndResults && data != null) {

        if (data.action === 'NAME_RESOLUTION') {
          const message: ChatMessage = {
            id: uuidv4(),
            role: 'USER',
            messageType: 'LOCATION_RESOLVED',
            text: data.label + ' (' + data.code + ')',
            loading: false,
            data: {
              toolUseId: data.toolUseId,
              uri: data.uri
            }
          };

          this.send(message);
        }
      }
      this.minimized = step == WorkflowStep.MinimizeChat;
    });
  }

  ngOnDestroy(): void {
  }

  send(message: ChatMessage): void {

    this.message = '';

    this.store.dispatch(ChatActions.addMessage(message));

    const system: ChatMessage = {
      id: uuidv4(),
      role: 'SYSTEM',
      messageType: 'BASIC',
      text: '',
      loading: true
    };

    this.loading = true;

    this.chatService.query([...this.renderedMessages].reverse())
      .then((message) => this.messageService.process(system, message))
      .catch(error => {
        this.errorService.handleError(error)

        this.store.dispatch(ChatActions.updateMessage({
          ...system,
          text: 'An error occurred',
          loading: false,
          messageType: 'ERROR',
        }));

      }).finally(() => {
        this.loading = false;
      });

    this.store.dispatch(ChatActions.addMessage(system));
  }

  sendMessage(): void {
    if (this.message.trim()) {
      if (this.minimized)
        this.minimizeChat();

      const message: ChatMessage = {
        id: uuidv4(),
        role: 'USER',
        messageType: 'BASIC',
        text: this.message,
        loading: false
      };

      this.send(message);
    }
  }

  minimizeChat() {
    if (!this.minimized) {
      this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.MinimizeChat }));
      this.minimized = true;
    }
    else {
      this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.AiChatAndResults }));
      this.minimized = false;
    }
  }

  askNewQuestion() {
    this.clear();
  }

  mapIt(message: ChatMessage) {
    // this.messages$.pipe(take(1)).subscribe(messages => {

    //   const index = messages.findIndex(m => m.id === message.id);

    //   if (index !== -1) {
    //     let history = [...messages];
    //     history.splice(index);
    //     history = history.filter(m => m.purpose === 'standard');

    //     this.mapLoading = true;

    //     this.chatService.getLocations(history, 0, 100).then((page) => {

    //       if (page.count == 0) {
    //         this.messageService.add({
    //           key: 'explorer',
    //           severity: 'info',
    //           summary: 'Info',
    //           detail: "The query did not return any results!",
    //           sticky: true
    //         })
    //       }

    //       this.store.dispatch(ExplorerActions.selectGeoObject(null));

    //       this.store.dispatch(ExplorerActions.setPage({
    //         page,
    //         zoomMap: true
    //       }));

    //       if (message.ambiguous)
    //         this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.DisambiguateObject }));

    //     }).catch(error => this.errorService.handleError(error)).finally(() => {
    //       this.mapLoading = false;
    //     })
    //   }
    // });
  }

  setWorkflowStepDisambiguate(message: ChatMessage) {

    this.workflowData$.pipe(take(1)).subscribe(data => {

      if (data.page != null) {
        this.store.dispatch(ExplorerActions.setPage({
          page: data.page,
        }));
      }

      this.store.dispatch(ExplorerActions.selectGeoObject(null));

      this.store.dispatch(ExplorerActions.mergeWorkflowStep({
        step: WorkflowStep.DisambiguateObject
      }));
    });



    // this.mapLoading = true;

    // this.explorerService.fullTextLookup(message.location!).then((page) => {

    //   this.store.dispatch(ExplorerActions.setPage({
    //     page,
    //     zoomMap: true
    //   }));

    //   this.store.dispatch(ExplorerActions.selectGeoObject(null));

    //   this.store.dispatch(ExplorerActions.setWorkflowStep({ step: WorkflowStep.DisambiguateObject }));

    // }).catch(error => this.errorService.handleError(error)).finally(() => {
    //   this.mapLoading = false;
    // })
  }

  clear(): void {
    this.store.dispatch(ExplorerActions.setPage({
      page: {
        locations: [],
        limit: 100,
        offset: 0,
        count: 0
      }
    }));
    this.store.dispatch(ChatActions.setMessageAndSession({ messages: [], sessionId: uuidv4() }));
  }

  @HostListener('document:keydown.enter', ['$event'])
  handleEnterKey(event: KeyboardEvent) {
    if (!this.loading) {
      this.sendMessage();
    }
  }

  select(event: Event, uri: string): void {
    // event.stopPropagation();
    // this.mapLoading = true;

    // this.explorerService.getAttributes(uri, true)
    //   .then(geoObject => {
    //     this.store.dispatch(ExplorerActions.selectGeoObject({ object: geoObject, zoomMap: true }));
    //   })
    //   .catch(error => this.errorService.handleError(error)).finally(() => {
    //     this.mapLoading = false;
    //   })
  }
}
