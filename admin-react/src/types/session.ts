export interface RecordingSession {
  id: string;
  token: string;
  managerName: string;
  productName: string;
  status: 'pending' | 'in_progress' | 'completed';
}
