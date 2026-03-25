import { useState } from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Switch } from '@/components/ui/switch';
import { Button } from '@/components/ui/button';

interface Plugin {
  id: string;
  name: string;
  version: string;
  enabled: boolean;
  description: string;
}

const MOCK_PLUGINS: Plugin[] = [
  { id: 'asr', name: 'ASR 语音识别', version: '1.0.0', enabled: true, description: '语音转文本' },
  { id: 'tts', name: 'TTS 语音合成', version: '1.0.0', enabled: true, description: '文本转语音' },
  { id: 'face', name: '人脸比对', version: '1.0.0', enabled: true, description: '身份证照片比对' },
  { id: 'liveness', name: '活体检测', version: '1.0.0', enabled: false, description: '眨眼、张嘴检测' },
];

export const PluginMarket = () => {
  const [plugins, setPlugins] = useState<Plugin[]>(MOCK_PLUGINS);
  
  const togglePlugin = (id: string, enabled: boolean) => {
    setPlugins(plugins.map(p => p.id === id ? { ...p, enabled } : p));
  };
  
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">插件市场</h1>
      <p className="text-gray-600 mb-6">管理和配置已安装的插件</p>
      
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>插件名称</TableHead>
            <TableHead>版本</TableHead>
            <TableHead>描述</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {plugins.map(plugin => (
            <TableRow key={plugin.id}>
              <TableCell className="font-medium">{plugin.name}</TableCell>
              <TableCell>v{plugin.version}</TableCell>
              <TableCell>{plugin.description}</TableCell>
              <TableCell>
                <Switch 
                  checked={plugin.enabled} 
                  onCheckedChange={(enabled: boolean) => togglePlugin(plugin.id, enabled)} 
                />
              </TableCell>
              <TableCell>
                <Button variant="outline" size="sm">配置</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};
