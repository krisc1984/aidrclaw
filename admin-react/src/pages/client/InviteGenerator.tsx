import QRCode from 'qrcode.react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

interface InviteGeneratorProps {
  sessionId?: string;
  token?: string;
  managerName?: string;
  productName?: string;
}

export const InviteGenerator = ({ 
  sessionId = 'session-001', 
  token = 'mock-token-123',
  managerName = '王理财',
  productName = 'XX 成长基金'
}: InviteGeneratorProps) => {
  const inviteUrl = `${window.location.origin}/record/${sessionId}?token=${token}`;
  
  const copyLink = () => {
    navigator.clipboard.writeText(inviteUrl);
    alert('链接已复制到剪贴板');
  };
  
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <Card className="max-w-md w-full">
        <CardHeader>
          <CardTitle>双录邀请</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="text-sm text-gray-600 space-y-1">
            <div><span className="font-medium">理财经理：</span>{managerName}</div>
            <div><span className="font-medium">产品：</span>{productName}</div>
          </div>
          
          <div className="flex justify-center">
            <QRCode value={inviteUrl} size={256} level="H" />
          </div>
          
          <div className="space-y-2">
            <div className="text-sm font-medium">邀请链接：</div>
            <div className="flex gap-2">
              <Input value={inviteUrl} readOnly className="flex-1 text-xs" />
              <Button onClick={copyLink}>复制</Button>
            </div>
          </div>
          
          <div className="text-xs text-gray-500">
            提示：客户可扫描二维码或点击链接进入双录界面
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
