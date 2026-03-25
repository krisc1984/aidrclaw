import { Routes, Route, Navigate } from 'react-router-dom';
import { AdminDashboard } from './components/admin/AdminDashboard';
import { PluginMarket } from './pages/admin/PluginMarket';
import { FlowDesigner } from './pages/admin/FlowDesigner';
import { RecordList } from './pages/admin/RecordList';
import { QualityReview } from './pages/admin/QualityReview';
import { ClientRecord } from './pages/client/ClientRecord';
import { InviteGenerator } from './pages/client/InviteGenerator';

function App() {
  return (
    <Routes>
      {/* 管理后台路由 */}
      <Route path="/admin" element={<AdminDashboard />}>
        <Route index element={<Navigate to="/admin/plugins" replace />} />
        <Route path="plugins" element={<PluginMarket />} />
        <Route path="flows" element={<FlowDesigner />} />
        <Route path="records" element={<RecordList />} />
        <Route path="quality" element={<QualityReview />} />
      </Route>
      
      {/* 客户端路由 */}
      <Route path="/record/:sessionId" element={<ClientRecord />} />
      <Route path="/invite/:sessionId" element={<InviteGenerator />} />
      
      {/* 默认重定向 */}
      <Route path="/" element={<Navigate to="/admin" replace />} />
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  );
}

export default App;
