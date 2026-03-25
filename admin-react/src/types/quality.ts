export interface QualityResult {
  id: string;
  sessionId: string;
  customerName: string;
  score: number;
  passed: boolean;
  reason?: string;
  violations: string[];
  reviewStatus: 'pending' | 'approved' | 'rejected';
  reviewComment?: string;
}
