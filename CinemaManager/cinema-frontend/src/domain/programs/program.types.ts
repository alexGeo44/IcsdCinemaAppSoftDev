import { ProgramState } from "./program.enums";

export type ISODate = `${number}-${number}-${number}`;

export interface Program {
id: number;
name: string;
description: string | null;
startDate: ISODate | null;
endDate: ISODate | null;
state: ProgramState;


programmerIds?: number[];
staffIds?: number[];
creatorUserId?: number;
}


export { ProgramState };
