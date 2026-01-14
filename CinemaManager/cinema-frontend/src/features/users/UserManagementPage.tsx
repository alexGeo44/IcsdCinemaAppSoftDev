import { useEffect, useMemo, useState } from "react";
import { usersApi } from "../../api/users.api";
import type { User } from "../../domain/users/user.types";
import { authStore } from "../../auth/auth.store";
import { BaseRole } from "../../domain/auth/auth.types";

type StatusFilter = "all" | "active" | "inactive";

export default function UserManagementPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");

  const currentUser = authStore((s) => s.user);

  const refresh = async () => {
    try {
      setLoading(true);
      const res = await usersApi.list();
      setUsers(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const handleActivate = async (user: User) => {
    if (!currentUser) {
      window.alert("Δεν είσαι συνδεδεμένος.");
      return;
    }

    const ok = window.confirm(
      `Να ενεργοποιηθεί ο χρήστης "${user.userName}" ;`
    );
    if (!ok) return;

    await usersApi.activate(user.id);
    await refresh();
  };

  const handleDeactivate = async (user: User) => {
    if (!currentUser) {
      window.alert("Δεν είσαι συνδεδεμένος.");
      return;
    }


    if (currentUser.role === BaseRole.ADMIN && user.role === BaseRole.ADMIN) {
      window.alert("Οι admin δεν μπορούν να απενεργοποιούν άλλους admin.");
      return;
    }


    if (currentUser.id === user.id) {
      window.alert("Δεν μπορείς να απενεργοποιήσεις τον εαυτό σου.");
      return;
    }

    const ok = window.confirm(
      `Να απενεργοποιηθεί ο χρήστης "${user.userName}" ;`
    );
    if (!ok) return;

    await usersApi.deactivate(user.id);
    await refresh();
  };

  const handleDelete = async (user: User) => {
    if (!currentUser) {
      window.alert("Δεν είσαι συνδεδεμένος.");
      return;
    }


    if (currentUser.role === BaseRole.ADMIN && user.role === BaseRole.ADMIN) {
      window.alert("Οι admin δεν μπορούν να διαγράφουν άλλους admin.");
      return;
    }


    if (currentUser.id === user.id) {
      window.alert("Δεν μπορείς να διαγράψεις τον εαυτό σου.");
      return;
    }

    const ok = window.confirm(
      `Ο χρήστης "${user.userName}" θα διαγραφεί οριστικά.\nΣίγουρα;`
    );
    if (!ok) return;

    await usersApi.delete(user.id);
    await refresh();
  };

  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchesSearch =
        search.trim().length === 0 ||
        u.userName.toLowerCase().includes(search.toLowerCase()) ||
        u.fullName.toLowerCase().includes(search.toLowerCase());

      const matchesStatus =
        statusFilter === "all" ||
        (statusFilter === "active" && u.active) ||
        (statusFilter === "inactive" && !u.active);

      return matchesSearch && matchesStatus;
    });
  }, [users, search, statusFilter]);

  return (
    <div className="min-h-full bg-slate-950/90 py-8">
      <div className="max-w-6xl mx-auto px-4">
        {}
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between mb-6">
          <div>
            <h1 className="text-2xl font-semibold text-slate-50">
              User Management
            </h1>
            <p className="text-sm text-slate-400">
              Προβολή, ενεργοποίηση, απενεργοποίηση και διαγραφή χρηστών.
            </p>
          </div>

          <div className="flex items-center gap-3 text-sm">
            <span className="px-3 py-1 rounded-full bg-slate-800 text-slate-200">
              Σύνολο:{" "}
              <span className="font-semibold text-sky-400">
                {users.length}
              </span>
            </span>
            <span className="px-3 py-1 rounded-full bg-slate-800 text-slate-200">
              Ενεργοί:{" "}
              <span className="font-semibold text-emerald-400">
                {users.filter((u) => u.active).length}
              </span>
            </span>
          </div>
        </div>

        {}
        <div className="bg-slate-900/80 border border-slate-800 rounded-xl shadow-xl shadow-slate-900/40 overflow-hidden">
          {}
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between px-4 py-3 border-b border-slate-800">
            <div className="relative w-full sm:w-72">
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Αναζήτηση με username ή όνομα..."
                className="w-full rounded-lg bg-slate-900 border border-slate-700 px-3 py-2 pl-9 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500/70 focus:border-sky-500"
              />
              <span className="pointer-events-none absolute left-2.5 top-2.5 text-slate-500 text-xs">

              </span>
            </div>

            <div className="flex gap-2 text-xs">
              <button
                type="button"
                onClick={() => setStatusFilter("all")}
                className={`px-3 py-1.5 rounded-full border ${
                  statusFilter === "all"
                    ? "border-sky-500 bg-sky-500/10 text-sky-300"
                    : "border-slate-700 bg-slate-900 text-slate-300 hover:border-slate-500"
                }`}
              >
                Όλοι
              </button>
              <button
                type="button"
                onClick={() => setStatusFilter("active")}
                className={`px-3 py-1.5 rounded-full border ${
                  statusFilter === "active"
                    ? "border-emerald-500 bg-emerald-500/10 text-emerald-300"
                    : "border-slate-700 bg-slate-900 text-slate-300 hover:border-slate-500"
                }`}
              >
                Ενεργοί
              </button>
              <button
                type="button"
                onClick={() => setStatusFilter("inactive")}
                className={`px-3 py-1.5 rounded-full border ${
                  statusFilter === "inactive"
                    ? "border-rose-500 bg-rose-500/10 text-rose-300"
                    : "border-slate-700 bg-slate-900 text-slate-300 hover:border-slate-500"
                }`}
              >
                Ανενεργοί
              </button>
            </div>
          </div>

          {}
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-900/90 border-b border-slate-800 text-xs uppercase tracking-wide text-slate-400">
                <tr>
                  <th className="py-3 px-4 text-left">ID</th>
                  <th className="py-3 px-4 text-left">Username</th>
                  <th className="py-3 px-4 text-left">Full name</th>
                  <th className="py-3 px-4 text-left">Role</th>
                  <th className="py-3 px-4 text-left">Status</th>
                  <th className="py-3 px-4 text-right w-56">Actions</th>
                </tr>
              </thead>

              <tbody>
                {loading && (
                  <tr>
                    <td
                      colSpan={6}
                      className="py-6 text-center text-slate-400"
                    >
                      Φόρτωση χρηστών...
                    </td>
                  </tr>
                )}

                {!loading && filteredUsers.length === 0 && (
                  <tr>
                    <td
                      colSpan={6}
                      className="py-6 text-center text-slate-500"
                    >
                      Δεν βρέθηκαν χρήστες με τα τρέχοντα φίλτρα.
                    </td>
                  </tr>
                )}

                {!loading &&
                  filteredUsers.map((u) => {
                    const isSelf = currentUser && currentUser.id === u.id;
                    const isAdminAdmin =
                      currentUser &&
                      currentUser.role === BaseRole.ADMIN &&
                      u.role === BaseRole.ADMIN;

                    const canModify = !isSelf && !isAdminAdmin;

                    return (
                      <tr
                        key={u.id}
                        className="border-t border-slate-800/80 hover:bg-slate-900/70 transition-colors"
                      >
                        <td className="py-2.5 px-4 text-slate-400 text-xs">
                          #{u.id}
                        </td>
                        <td className="py-2.5 px-4 text-slate-100 font-medium">
                          {u.userName}
                        </td>
                        <td className="py-2.5 px-4 text-slate-200">
                          {u.fullName}
                        </td>
                        <td className="py-2.5 px-4">
                          <span className="inline-flex items-center rounded-full bg-slate-800/70 px-2.5 py-0.5 text-[11px] font-medium text-slate-200">
                            {u.role}
                          </span>
                        </td>
                        <td className="py-2.5 px-4">
                          <span
                            className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-semibold ${
                              u.active
                                ? "bg-emerald-500/10 text-emerald-300 border border-emerald-500/60"
                                : "bg-rose-500/10 text-rose-300 border border-rose-500/60"
                            }`}
                          >
                            <span
                              className={`mr-1.5 h-1.5 w-1.5 rounded-full ${
                                u.active ? "bg-emerald-400" : "bg-rose-400"
                              }`}
                            />
                            {u.active ? "Active" : "Inactive"}
                          </span>
                        </td>
                        <td className="py-2.5 px-4 text-right">
                          <div className="inline-flex gap-2">
                            {u.active ? (
                              <button
                                type="button"
                                disabled={!canModify}
                                onClick={() => handleDeactivate(u)}
                                className={`px-3 py-1.5 rounded-md border text-[11px] font-medium ${
                                  canModify
                                    ? "border-amber-500/70 bg-amber-500/10 text-amber-200 hover:bg-amber-500/20"
                                    : "border-slate-700 bg-slate-900 text-slate-500 cursor-not-allowed"
                                }`}
                              >
                                Deactivate
                              </button>
                            ) : (
                              <button
                                type="button"
                                disabled={!canModify}
                                onClick={() => handleActivate(u)}
                                className={`px-3 py-1.5 rounded-md border text-[11px] font-medium ${
                                  canModify
                                    ? "border-emerald-500/70 bg-emerald-500/10 text-emerald-200 hover:bg-emerald-500/20"
                                    : "border-slate-700 bg-slate-900 text-slate-500 cursor-not-allowed"
                                }`}
                              >
                                Activate
                              </button>
                            )}

                            <button
                              type="button"
                              disabled={!canModify}
                              onClick={() => handleDelete(u)}
                              className={`px-3 py-1.5 rounded-md border text-[11px] font-medium ${
                                canModify
                                  ? "border-rose-500/70 bg-rose-500/10 text-rose-200 hover:bg-rose-500/20"
                                  : "border-slate-700 bg-slate-900 text-slate-500 cursor-not-allowed"
                              }`}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
