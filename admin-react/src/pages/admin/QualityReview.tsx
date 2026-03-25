import { useState } from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';

interface QualityResult {
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

const MOCK_RESULTS: QualityResult[] = [
  { 
    id: '1', 
    sessionId: 'session-001', 
    customerName: '张三',
    score: 85,
    passed: true,
    reason: '话术完整，无违规词',
    violations: [],
    reviewStatus: 'pending',
  },
  { 
    id: '2', 
    sessionId: 'session-002', 
    customerName: '李四',
    score: 65,
    passed: false,
    reason: '使用违规词：保本',
    violations: ['保本'],
    reviewStatus: 'pending',
  },
];

export const QualityReview = () => {
  const [results] = useState<QualityResult[]>(MOCK_RESULTS);
  const [reviewComment, setReviewComment] = useState('');
  const [openDialog, setOpenDialog] = useState<string | null>(null);
  
  const openReview = (id: string) => setOpenDialog(id);
  const closeReview = () => setOpenDialog(null);
  
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">质检复核</h1>
      
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>会话 ID</TableHead>
            <TableHead>客户名称</TableHead>
            <TableHead>质检分数</TableHead>
            <TableHead>结果</TableHead>
            <TableHead>违规词</TableHead>
            <TableHead>复核状态</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {results.map(result => (
            <TableRow key={result.id}>
              <TableCell className="font-mono text-sm">{result.sessionId}</TableCell>
              <TableCell>{result.customerName}</TableCell>
              <TableCell>{result.score}</TableCell>
              <TableCell>
                <Badge variant={result.passed ? 'default' : 'destructive'}>
                  {result.passed ? '通过' : '不通过'}
                </Badge>
              </TableCell>
              <TableCell>{result.violations.join(', ') || '-'}</TableCell>
              <TableCell>
                <Badge variant={
                  result.reviewStatus === 'approved' ? 'default' :
                  result.reviewStatus === 'rejected' ? 'destructive' : 'secondary'
                }>
                  {result.reviewStatus === 'pending' ? '待复核' : 
                   result.reviewStatus === 'approved' ? '已通过' : '已拒绝'}
                </Badge>
              </TableCell>
              <TableCell>
                <Button 
                  variant="outline" 
                  size="sm"
                  disabled={result.reviewStatus !== 'pending'}
                  onClick={() => openReview(result.id)}
                >
                  复核
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      
      {openDialog && (
        <DialogContent>
          <DialogHeader>
            <DialogTitle>质检复核 - {openDialog}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <div><span className="font-bold">质检分数：</span>85</div>
              <div><span className="font-bold">结果：</span>通过</div>
              <div><span className="font-bold">原因：</span>话术完整</div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">复核意见</label>
              <Textarea 
                placeholder="请输入复核意见" 
                value={reviewComment}
                onChange={e => setReviewComment(e.target.value)}
                rows={4}
              />
            </div>
            <div className="flex gap-2">
              <Button onClick={closeReview} variant="default">通过</Button>
              <Button onClick={closeReview} variant="destructive">拒绝</Button>
            </div>
          </div>
        </DialogContent>
      )}
    </div>
  );
};
