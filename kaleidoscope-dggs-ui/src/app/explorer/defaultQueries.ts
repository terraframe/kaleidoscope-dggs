import { StyleConfig } from "../models/style.model";


export const GREEN = "#228B22";
export const RED = "#FF0000";

export const SELECTED_COLOR = "#ffff00";
export const HOVER_COLOR = "#ffff99";

const lpgvs: string = "http://terraframe.ai#";

export const defaultStyles = {
  [lpgvs + 'Subdivision']: { color: '#F2799D', order: 0 },
  [lpgvs + 'Division']: { color: '#D5F279', order: 0 },
  [lpgvs + 'DisseminationArea']: { color: '#C0F279', order: 6, label: "Dissemination Area" },
};