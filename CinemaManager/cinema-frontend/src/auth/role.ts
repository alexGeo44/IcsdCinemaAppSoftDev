
import { BaseRole } from "../domain/auth/auth.types";

type RoleLike = BaseRole | string | null | undefined;

export function normalizeRole(role?: RoleLike): BaseRole | undefined {
  if (role == null) return undefined;


  let r = String(role).trim().toUpperCase();


  while (r.startsWith("ROLE_")) r = r.slice(5);


  return (Object.values(BaseRole) as string[]).includes(r) ? (r as BaseRole) : undefined;
}


export function hasRole(userRole: RoleLike, allowed: BaseRole | BaseRole[]) {
  const r = normalizeRole(userRole);
  if (!r) return false;
  return Array.isArray(allowed) ? allowed.includes(r) : r === allowed;
}
