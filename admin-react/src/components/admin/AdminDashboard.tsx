import { Outlet, Link, useLocation } from 'react-router-dom';

export const AdminDashboard = () => {
  const location = useLocation();
  
  const navItems = [
    { path: '/admin/plugins', label: '插件市场' },
    { path: '/admin/flows', label: '流程设计器' },
    { path: '/admin/records', label: '录制记录' },
    { path: '/admin/quality', label: '质检复核' },
  ];
  
  return (
    <div className="flex h-screen bg-gray-100">
      {/* 侧边栏 */}
      <aside className="w-64 bg-white border-r">
        <div className="p-6 border-b">
          <h1 className="text-xl font-bold">智能双录系统</h1>
          <p className="text-sm text-gray-500 mt-1">管理后台</p>
        </div>
        <nav className="p-4 space-y-2">
          {navItems.map(item => (
            <Link
              key={item.path}
              to={item.path}
              className={`block p-3 rounded-lg transition-colors ${
                location.pathname === item.path
                  ? 'bg-blue-50 text-blue-600'
                  : 'hover:bg-gray-100'
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>
      
      {/* 主内容区 */}
      <div className="flex-1 flex flex-col">
        <header className="h-16 bg-white border-b flex items-center px-6">
          <h1 className="text-xl font-bold">智能双录系统 - 管理后台</h1>
        </header>
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
