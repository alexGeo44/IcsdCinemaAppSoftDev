
import { BaseRole } from "../domain/auth/auth.types";
import { normalizeRole } from "./role";

export type Role = BaseRole | string | null | undefined;


export const isAdmin = (role?: Role) => normalizeRole(role) === BaseRole.ADMIN;
export const isUser = (role?: Role) => normalizeRole(role) === BaseRole.USER;


export const isCinemaUser = (role?: Role) => isUser(role);


export const canViewMyScreenings = (role?: Role) => isCinemaUser(role) && !isAdmin(role);


export const canCreateProgram = (role?: Role) => isCinemaUser(role) && !isAdmin(role);


export const canCreateScreening = (role?: Role) => isCinemaUser(role) && !isAdmin(role);


export const canProgrammerScreenings = (role?: Role) => isCinemaUser(role) && !isAdmin(role);
export const canReviewScreenings = (role?: Role) => isCinemaUser(role) && !isAdmin(role);


export type ProgramLike = {
programmerIds?: Array<number | string>;
programmers?: Array<number | string>;
staffIds?: Array<number | string>;
staff?: Array<number | string>;
creatorUserId?: number | string;
state?: string;
};

function toNum(x: unknown): number | null {
  const n = typeof x === "number" ? x : typeof x === "string" ? Number(x) : NaN;
  return Number.isFinite(n) ? n : null;
}

function uniq(nums: number[]) {
  return Array.from(new Set(nums));
}

export function getProgrammerIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  const raw = [...(p.programmerIds ?? []), ...(p.programmers ?? [])];
  const ids = raw.map(toNum).filter((x): x is number => x != null);
  return uniq(ids);
}

export function getStaffIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  const raw = [...(p.staffIds ?? []), ...(p.staff ?? [])];
  const ids = raw.map(toNum).filter((x): x is number => x != null);
  return uniq(ids);
}


export function isCreatorOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  const cid = toNum(p.creatorUserId);
  return cid != null && cid === userId;
}


export function isProgrammerOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  return getProgrammerIds(p).includes(userId);
}

export function isStaffOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  return getStaffIds(p).includes(userId);
}


export function canManageProgramInProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  return isCreatorOfProgram(p, userId);
}


export function canAccessProgrammerArea(p: ProgramLike | null | undefined, userId?: number | null) {
  return isCreatorOfProgram(p, userId) || isProgrammerOfProgram(p, userId);
}


export function canCreateScreeningInProgram(
  role: Role | undefined,
  p: ProgramLike | null | undefined,
  userId?: number | null
) {
  if (!userId) return false;
  if (isAdmin(role)) return false;
  if (!isCinemaUser(role)) return false;


  if (!p) return true;


  return !isCreatorOfProgram(p, userId);
}


export type ScreeningLike = {
  submitterId?: number | null;
  staffMemberId?: number | null;
  state?: string;
};

export function isOwnerOfScreening(s: ScreeningLike | null | undefined, userId?: number | null) {
  if (!s || !userId) return false;
  return s.submitterId === userId;
}

export function isAssignedStaffOfScreening(s: ScreeningLike | null | undefined, userId?: number | null) {
  if (!s || !userId) return false;
  return s.staffMemberId != null && s.staffMemberId === userId;
}
