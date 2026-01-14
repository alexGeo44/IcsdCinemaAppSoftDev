
import axios from "./axios";
import type { ScreeningState } from "../domain/screenings/screening.enums";
import type { Screening } from "../domain/screenings/screening.types";

export type CreateOrUpdateScreeningRequest = {
title: string;
genre?: string;
description?: string;

};

export const screeningsApi = {

create: (programId: number, data: CreateOrUpdateScreeningRequest) =>
    axios.post<Screening>("/api/screenings", data, { params: { programId } }),


  view: (id: number) => axios.get<Screening>(`/api/screenings/${id}`),


  update: (screeningId: number, data: CreateOrUpdateScreeningRequest) =>
    axios.put<void>(`/api/screenings/${screeningId}`, data),


  submit: (screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/submit`, null),



  withdraw: (screeningId: number) =>
    axios.delete<void>(`/api/screenings/${screeningId}`),


  assignHandler: (screeningId: number, staffId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/handler/${staffId}`, null),


  review: (screeningId: number, score: number, comments?: string) =>
    axios.put<void>(`/api/screenings/${screeningId}/review`, null, {
      params: { score, comments },
    }),


  approve: (screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/approve`, null),


  finalSubmit: (screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/final-submit`, null),


  schedule: (screeningId: number, date: string, room: string) =>
    axios.put<void>(`/api/screenings/${screeningId}/schedule`, null, {
      params: { date, room },
    }),


  reject: (screeningId: number, reason: string) =>
    axios.put<void>(`/api/screenings/${screeningId}/reject`, null, {
      params: { reason },
    }),


  byProgram: (params: {
    programId: number;
    title?: string;
    genre?: string;
    from?: string;
    to?: string;
    state?: ScreeningState;
    offset?: number;
    limit?: number;
    timetable?: boolean;
  }) => axios.get<Screening[]>("/api/screenings/by-program", { params }),


  bySubmitter: (params?: {
    submitterId?: number;
    state?: ScreeningState;
    offset?: number;
    limit?: number;
  }) => axios.get<Screening[]>("/api/screenings/by-submitter", { params }),


  byStaff: (params?: { offset?: number; limit?: number }) =>
    axios.get<Screening[]>("/api/screenings/by-staff", { params }),
};
