import { Handle, Position, NodeProps } from 'reactflow';

export const IdentityNode = ({ data }: NodeProps) => {
  return (
    <div className="bg-white border-2 border-blue-500 rounded-lg p-4 min-w-[200px] shadow-sm">
      <Handle type="target" position={Position.Top} className="w-3 h-3" />
      <div className="font-bold text-blue-600 flex items-center gap-2">
        <span>👤</span> 身份核验
      </div>
      <div className="text-sm text-gray-600 mt-2">{data.label}</div>
      <Handle type="source" position={Position.Bottom} className="w-3 h-3" />
    </div>
  );
};

export const RiskNode = ({ data }: NodeProps) => {
  return (
    <div className="bg-white border-2 border-orange-500 rounded-lg p-4 min-w-[200px] shadow-sm">
      <Handle type="target" position={Position.Top} className="w-3 h-3" />
      <div className="font-bold text-orange-600 flex items-center gap-2">
        <span>⚠️</span> 风险揭示
      </div>
      <div className="text-sm text-gray-600 mt-2">{data.label}</div>
      <Handle type="source" position={Position.Bottom} className="w-3 h-3" />
    </div>
  );
};

export const ProductNode = ({ data }: NodeProps) => {
  return (
    <div className="bg-white border-2 border-green-500 rounded-lg p-4 min-w-[200px] shadow-sm">
      <Handle type="target" position={Position.Top} className="w-3 h-3" />
      <div className="font-bold text-green-600 flex items-center gap-2">
        <span>📊</span> 产品介绍
      </div>
      <div className="text-sm text-gray-600 mt-2">{data.label}</div>
      <Handle type="source" position={Position.Bottom} className="w-3 h-3" />
    </div>
  );
};

export const ConfirmationNode = ({ data }: NodeProps) => {
  return (
    <div className="bg-white border-2 border-purple-500 rounded-lg p-4 min-w-[200px] shadow-sm">
      <Handle type="target" position={Position.Top} className="w-3 h-3" />
      <div className="font-bold text-purple-600 flex items-center gap-2">
        <span>✅</span> 确认签字
      </div>
      <div className="text-sm text-gray-600 mt-2">{data.label}</div>
      <Handle type="source" position={Position.Bottom} className="w-3 h-3" />
    </div>
  );
};
