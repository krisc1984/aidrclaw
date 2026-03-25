export interface Recording {
  id: string;
  sessionId: string;
  customerName: string;
  product: string;
  startTime: string;
  duration: number;
  videoUrl: string;
  qualityScore?: number;
  qualityPassed?: boolean;
}
