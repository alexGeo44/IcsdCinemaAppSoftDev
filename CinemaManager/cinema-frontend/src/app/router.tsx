
import { createBrowserRouter } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import { AuthGuard } from "./guards/AuthGuard";
import { RoleGuard } from "./guards/RoleGuard";
import { BaseRole, type UserResponse } from "../domain/auth/auth.types";
import DashboardPage from "../features/common/DashboardPage";

// auth / common
import LoginPage from "../features/auth/LoginPage";
import RegisterPage from "../features/auth/RegisterPage";
import ForbiddenPage from "../features/common/ForbiddenPage";

// programs
import ProgramListPage from "../features/programs/ProgramListPage";
import ProgramDetailsPage from "../features/programs/ProgramDetailsPage";
import ProgramCreatePage from "../features/programs/ProgramCreatePage";
import ProgramEditPage from "../features/programs/ProgramEditPage";

// screenings
import ProgramScreeningsPublicPage from "../features/screenings/ProgramScreeningsPublicPage";
import ScreeningDetailsPage from "../features/screenings/ScreeningDetailsPage";
import ScreeningCreatePage from "../features/screenings/ScreeningCreatePage";
import ScreeningEditPage from "../features/screenings/ScreeningEditPage";
import MyScreeningsPage from "../features/screenings/MyScreeningsPage";
import StaffReviewPage from "../features/screenings/StaffReviewPage";
import ProgramScreeningsPage from "../features/screenings/ProgramScreeningsPage";

// users / admin
import AccountSettingsPage from "../features/users/AccountSettingsPage";
import UserManagementPage from "../features/users/UserManagementPage";
import AuditLogPage from "../features/users/AuditLogPage";


// helpers


function withAppShell(element: JSX.Element) {
  return <AppLayout>{element}</AppLayout>;
}

function withAuthShell(element: JSX.Element) {
  return (
    <AuthGuard>
      <AppLayout>{element}</AppLayout>
    </AuthGuard>
  );
}

function withAuthShellRole(roles: BaseRole[], element: JSX.Element) {
  return (
    <AuthGuard>
      <RoleGuard allow={roles}>
        <AppLayout>{element}</AppLayout>
      </RoleGuard>
    </AuthGuard>
  );
}

function withAuthShellAllowIf(
  allowIf: (role?: BaseRole, user?: UserResponse | null) => boolean,
  element: JSX.Element
) {
  return (
    <AuthGuard>
      <RoleGuard allowIf={allowIf}>
        <AppLayout>{element}</AppLayout>
      </RoleGuard>
    </AuthGuard>
  );
}


const isCinemaRouteUser = (_role?: BaseRole, user?: UserResponse | null) => {
  if (!user) return false;

  const raw = String((user as any).role ?? "")
    .trim()
    .toUpperCase()
    .replace(/^ROLE_+/, "");

  // admin is NOT cinema-domain superuser
  if (raw === "ADMIN") return false;

  // accept common cinema actor roles
  return raw === "USER" || raw === "PROGRAMMER" || raw === "STAFF";
};



export const router = createBrowserRouter([

  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  { path: "/forbidden", element: <ForbiddenPage /> },


  { path: "/", element: withAppShell(<DashboardPage />) },


  { path: "/programs", element: withAppShell(<ProgramListPage />) },
  { path: "/programs/:id", element: withAppShell(<ProgramDetailsPage />) },


  { path: "/screenings/by-program", element: withAppShell(<ProgramScreeningsPublicPage />) },
  { path: "/screenings/:id", element: withAppShell(<ScreeningDetailsPage />) },


  { path: "/programs/new", element: withAuthShellAllowIf(isCinemaRouteUser, <ProgramCreatePage />) },
  { path: "/programs/:id/edit", element: withAuthShellAllowIf(isCinemaRouteUser, <ProgramEditPage />) },

  { path: "/screenings/new", element: withAuthShellAllowIf(isCinemaRouteUser, <ScreeningCreatePage />) },
  { path: "/screenings/:id/edit", element: withAuthShellAllowIf(isCinemaRouteUser, <ScreeningEditPage />) },

  { path: "/my-screenings", element: withAuthShellAllowIf(isCinemaRouteUser, <MyScreeningsPage />) },
  { path: "/account", element: withAuthShell(<AccountSettingsPage />) },


  { path: "/staff/review", element: withAuthShellAllowIf(isCinemaRouteUser, <StaffReviewPage />) },
  { path: "/programmer/screenings", element: withAuthShellAllowIf(isCinemaRouteUser, <ProgramScreeningsPage />) },


  { path: "/admin/users", element: withAuthShellRole([BaseRole.ADMIN], <UserManagementPage />) },
  { path: "/admin/audit-log", element: withAuthShellRole([BaseRole.ADMIN], <AuditLogPage />) },


  { path: "*", element: withAppShell(<div>Not found</div>) },
]);
