import { ChatMessage, LocationPage } from "./models/chat.model";
import { Configuration } from "./models/configuration.model";
import { ExplorerInit } from "./service/explorer.service";

export class MockUtil {
  public static message: ChatMessage =
    {
      id: '1',
      role: 'USER',
      text: "",
      messageType: 'BASIC'
    }

  public static messages: ChatMessage[] = [];

  public static explorerInit: ExplorerInit = {

  }

  public static locations: LocationPage = {
    locations: [
    ],
    limit: 17,
    offset: 0,
    count: 17
  };

  public static styles: Configuration = {
    layers: [],
    token: "",
    styles: {
      "https://localhost:4200/lpg/graph_801104/0#RealProperty": {
        "color": "#79F294",
        "order": 0
      },
      "https://localhost:4200/lpg/graph_801104/0#ChannelReach": {
        "color": "#79DAF2",
        "order": 4
      },
      "https://localhost:4200/lpg/graph_801104/0#Reservoir": {
        "color": "#CAEEFB",
        "order": 5
      },
      "https://localhost:4200/lpg/graph_801104/0#RecreationArea": {
        "color": "#F2E779",
        "order": 3
      },
      "https://localhost:4200/lpg/graph_801104/0#LeveedArea": {
        "color": "#C379F2",
        "order": 4
      },
      "https://localhost:4200/lpg/graph_801104/0#SchoolZone": {
        "color": "#FBE3D6",
        "order": 6
      },
      "https://localhost:4200/lpg/graph_801104/0#Project": {
        "color": "#C0F279",
        "order": 6
      },
      "https://localhost:4200/lpg/graph_801104/0#LeveeArea": {
        "color": "#D1D1D1",
        "order": 4
      },
      "https://localhost:4200/lpg/graph_801104/0#School": {
        "color": "#F2A579",
        "order": 0
      },
      "https://localhost:4200/lpg/graph_801104/0#Levee": {
        "color": "#F279E0",
        "order": 0
      },
      "https://localhost:4200/lpg/graph_801104/0#Hospital": {
        "color": "#F2799D",
        "order": 0
      },
      "https://localhost:4200/lpg/graph_801104/0#Dam": {
        "color": "#D5F279",
        "order": 0
      },
      "https://localhost:4200/lpg/graph_801104/0#River": {
        "color": "#7999F2",
        "order": 2
      },
      "https://localhost:4200/lpg/graph_801104/0#Watershed": {
        "color": "#79F2C9",
        "order": 4
      },
      "https://localhost:4200/lpg/graph_801104/0#ChannelArea": {
        "color": "#156082",
        "order": 4
      },
      "https://localhost:4200/lpg/graph_801104/0#ChannelLine": {
        "color": "#79F2A0",
        "order": 1
      },
      "https://localhost:4200/lpg/graph_801104/0#UsaceRecreationArea": {
        "color": "#F2BE79",
        "order": 3
      },
      "http://dime.usace.mil/ontologies/cwbi-concept#Program": {
        "color": "#FF5733",
        "order": 0
      },
      "https://localhost:4200/lpg/graph_801104/0#WaterLock": {
        "color": "#79F2E2",
        "order": 0
      }
    }
  }




}
