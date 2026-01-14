
import axios from "./axios";
import type { ProgramState } from "../domain/programs/program.enums";

export type ProgramViewResponse = {
id: number | null;
name: string;
description: string | null;
startDate: string | null;
endDate: string | null;
state: string;


programmerIds?: number[];
staffIds?: number[];

creatorUserId?: number;
};

export type ProgramResponse = ProgramViewResponse;

export type CreateProgramRequest = {
name: string;
description: string;
startDate: string;
endDate: string;
};


export type UpdateProgramRequest = {
name?: string;
description?: string;
startDate?: string | null;
endDate?: string | null;
};

export type ChangeProgramStateRequest = { nextState: string };

export const programsApi = {

create: (data: CreateProgramRequest) => axios.post<void>("/api/programs", data),


  update: (id: number, data: UpdateProgramRequest) =>
    axios.put<void>(`/api/programs/${id}`, data),


  delete: (id: number) => axios.delete<void>(`/api/programs/${id}`),


  search: (params?: {
    name?: string;
    description?: string;
    from?: string;
    to?: string;
    filmTitle?: string;
    auditorium?: string;
    offset?: number;
    limit?: number;
  }) => axios.get<ProgramViewResponse[]>("/api/programs", { params }),


  view: (id: number) => axios.get<ProgramViewResponse>(`/api/programs/${id}`),


  changeState: (programId: number, nextState: ProgramState) =>
    axios.put<ProgramResponse>(`/api/programs/${programId}/state`, {
      nextState,
    } satisfies ChangeProgramStateRequest),


  addProgrammer: (programId: number, userId: number) =>
    axios.post<void>(`/api/programs/${programId}/programmers/${userId}`, null),

  addStaff: (programId: number, userId: number) =>
    axios.post<void>(`/api/programs/${programId}/staff/${userId}`, null),
};
