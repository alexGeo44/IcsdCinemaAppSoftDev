
import axios from "./axios";
import { User } from "../domain/users/user.types";

export const usersApi = {

list: () => axios.get<User[]>("/api/admin/users"),


  changePassword: (data: { oldPassword: string; newPassword: string; newPasswordRepeat: string }) =>
    axios.put<void>("/api/me/password", data),


  deactivateMe: () => axios.put<void>("/api/me/deactivate", null),


  deleteMe: () => axios.delete<void>("/api/me"),


  activate: (id: number) => axios.put<void>(`/api/admin/users/${id}/activate`, null),
  deactivate: (id: number) => axios.put<void>(`/api/admin/users/${id}/deactivate`, null),
  delete: (id: number) => axios.delete<void>(`/api/admin/users/${id}`),
};
