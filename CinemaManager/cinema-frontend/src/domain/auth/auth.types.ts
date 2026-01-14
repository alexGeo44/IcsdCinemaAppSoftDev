
export enum BaseRole {
    USER = "USER",
    ADMIN = "ADMIN",
}

export interface UserResponse {
    id: number;
userName: string;
fullName: string;
role: BaseRole;
active: boolean;
}

export type User = UserResponse;

export interface AuthResponse {
token: string;
user: UserResponse;
}



export interface TokenInfoResponse {
userId: number;
role: BaseRole;
valid?: boolean;
expired?: boolean;
owner?: boolean;
message?: string;
}
