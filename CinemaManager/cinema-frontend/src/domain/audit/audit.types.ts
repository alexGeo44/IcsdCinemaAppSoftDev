export interface AuditLog {
    actorUserId: number | null;
action: string;
target: string;
timestamp: string;
}
