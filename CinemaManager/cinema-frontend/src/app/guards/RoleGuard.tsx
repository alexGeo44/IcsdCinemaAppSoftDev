
import { Navigate, useLocation } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import type { UserResponse } from "../../domain/auth/auth.types";
import { BaseRole } from "../../domain/auth/auth.types";
import { normalizeRole } from "../../auth/role";

export function RoleGuard({
  allow,
  allowIf,
  children,
}: {
  allow?: BaseRole[];

  allowIf?: (role?: BaseRole, user?: UserResponse | null) => boolean;
  children: JSX.Element;
}) {
  const user = authStore((s) => s.user);
  const bootstrapped = authStore((s) => s.bootstrapped);
  const location = useLocation();


  if (!bootstrapped) {
    return (
      <div className="h-screen flex items-center justify-center text-sm text-slate-400">
        Checking permissions…
      </div>
    );
  }


  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  const role = normalizeRole(user.role);

  const ok =
    typeof allowIf === "function"
      ? allowIf(role, user)
      : Array.isArray(allow)
      ? !!role && allow.includes(role)
      : true;

  if (!ok) return <Navigate to="/forbidden" replace />;

  return children;
}
