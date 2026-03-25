import { useState } from 'react';
import ReactFlow, { 
  Node, 
  Edge, 
  Controls, 
  Background,
  BackgroundVariant,
  addEdge,
  Connection,
} from 'reactflow';
import 'reactflow/dist/style.css';

import { IdentityNode, RiskNode, ProductNode, ConfirmationNode } from '@/components/admin/FlowNode';

const nodeTypes = {
  identity: IdentityNode,
  risk: RiskNode,
  product: ProductNode,
  confirmation: ConfirmationNode,
};

const initialNodes: Node[] = [
  { id: '1', type: 'identity', position: { x: 250, y: 0 }, data: { label: '身份核验' } },
  { id: '2', type: 'risk', position: { x: 250, y: 150 }, data: { label: '风险揭示' } },
  { id: '3', type: 'product', position: { x: 250, y: 300 }, data: { label: '产品介绍' } },
  { id: '4', type: 'confirmation', position: { x: 250, y: 450 }, data: { label: '确认签字' } },
];

const initialEdges: Edge[] = [
  { id: 'e1-2', source: '1', target: '2' },
  { id: 'e2-3', source: '2', target: '3' },
  { id: 'e3-4', source: '3', target: '4' },
];

export const FlowDesigner = () => {
  const [edges, setEdges] = useState<Edge[]>(initialEdges);
  
  const onConnect = (params: Connection) => setEdges(eds => addEdge(params, eds));
  
  return (
    <div className="h-[calc(100vh-200px)]">
      <div className="mb-4">
        <h1 className="text-2xl font-bold mb-2">流程设计器</h1>
        <p className="text-gray-600">拖拽节点，编排双录流程</p>
      </div>
      
      <div className="border rounded-lg overflow-hidden">
        <ReactFlow
          nodes={initialNodes}
          edges={edges}
          nodeTypes={nodeTypes}
          onConnect={onConnect}
          fitView
          snapToGrid
          snapGrid={[15, 15]}
        >
          <Controls />
          <Background variant={BackgroundVariant.Dots} gap={12} size={1} />
        </ReactFlow>
      </div>
    </div>
  );
};
