import { useParams } from 'react-router-dom';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle } from 'lucide-react';

export const ClientRecord = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b p-4">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-xl font-bold">双录系统</h1>
          <p className="text-sm text-gray-600">会话：{sessionId}</p>
        </div>
      </header>
      
      <main className="max-w-4xl mx-auto p-6">
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>欢迎参加双录</AlertTitle>
          <AlertDescription>
            请按照虚拟坐席的指引完成双录流程
          </AlertDescription>
        </Alert>
        
        <div className="mt-6 bg-white rounded-lg shadow p-6">
          <div className="text-center text-gray-500 py-12">
            <p>虚拟坐席界面</p>
            <p className="text-sm mt-2">(集成 Phase 6 VirtualAgentInterface)</p>
          </div>
        </div>
      </main>
    </div>
  );
};
