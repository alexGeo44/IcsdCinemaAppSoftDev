import { ScreeningState } from "./screening.enums";

export interface Screening {
id: number;
programId: number;


title: string;
genre?: string | null;
room?: string | null;
scheduledTime?: string | null;


submitterId?: number;
description?: string | null;

state?: ScreeningState;

staffMemberId?: number | null;
submittedTime?: string | null;
reviewedTime?: string | null;


startTime?: string | null;
endTime?: string | null;
}
