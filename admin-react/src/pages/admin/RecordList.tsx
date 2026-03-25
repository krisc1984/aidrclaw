import { useState } from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

interface Recording {
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

const MOCK_RECORDINGS: Recording[] = [
  { 
    id: '1', 
    sessionId: 'session-001', 
    customerName: '张三', 
    product: '基金',
    startTime: '2026-03-25 10:30:00',
    duration: 1200,
    videoUrl: '/videos/session-001.mp4',
    qualityScore: 85,
    qualityPassed: true,
  },
  { 
    id: '2', 
    sessionId: 'session-002', 
    customerName: '李四', 
    product: '保险',
    startTime: '2026-03-25 11:00:00',
    duration: 900,
    videoUrl: '/videos/session-002.mp4',
    qualityScore: 65,
    qualityPassed: false,
  },
];

export const RecordList = () => {
  const [records] = useState<Recording[]>(MOCK_RECORDINGS);
  const [searchTerm, setSearchTerm] = useState('');
  const [productFilter, setProductFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [playingUrl, setPlayingUrl] = useState<string | null>(null);
  
  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };
  
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">录制记录</h1>
      
      <div className="flex gap-4 mb-4">
        <Input 
          placeholder="搜索客户名称或会话 ID" 
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className="max-w-sm"
        />
        <Select value={productFilter} onValueChange={setProductFilter}>
          <SelectTrigger className="w-[180px]">
            {productFilter === 'all' ? '全部产品' : productFilter}
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部产品</SelectItem>
            <SelectItem value="fund">基金</SelectItem>
            <SelectItem value="insurance">保险</SelectItem>
            <SelectItem value="wealth">理财</SelectItem>
          </SelectContent>
        </Select>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            {statusFilter === 'all' ? '全部状态' : statusFilter === 'passed' ? '通过' : '不通过'}
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="passed">通过</SelectItem>
            <SelectItem value="failed">不通过</SelectItem>
          </SelectContent>
        </Select>
      </div>
      
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>会话 ID</TableHead>
            <TableHead>客户名称</TableHead>
            <TableHead>产品</TableHead>
            <TableHead>开始时间</TableHead>
            <TableHead>时长</TableHead>
            <TableHead>质检分数</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {records.map(record => (
            <TableRow key={record.id}>
              <TableCell className="font-mono text-sm">{record.sessionId}</TableCell>
              <TableCell>{record.customerName}</TableCell>
              <TableCell>{record.product}</TableCell>
              <TableCell>{record.startTime}</TableCell>
              <TableCell>{formatDuration(record.duration)}</TableCell>
              <TableCell>
                <Badge variant={record.qualityPassed ? 'default' : 'destructive'}>
                  {record.qualityScore}分
                </Badge>
              </TableCell>
              <TableCell>
                <Button 
                  variant="outline" 
                  size="sm"
                  onClick={() => setPlayingUrl(playingUrl === record.videoUrl ? null : record.videoUrl)}
                >
                  {playingUrl === record.videoUrl ? '关闭' : '回放'}
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      
      {playingUrl && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50">
          <div className="bg-white p-4 rounded-lg max-w-4xl w-full">
            <video controls className="w-full h-auto max-h-[70vh]" autoPlay>
              <source src={playingUrl} type="video/mp4" />
              您的浏览器不支持视频播放
            </video>
            <div className="mt-4 flex justify-end">
              <Button onClick={() => setPlayingUrl(null)}>关闭</Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
