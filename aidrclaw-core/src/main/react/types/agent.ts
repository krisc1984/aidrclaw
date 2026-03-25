// 对话消息
export interface ChatMessage {
  id: string;
  role: 'agent' | 'customer';
  text: string;
  audioUrl?: string;
  timestamp: Date;
}

// 对话节点类型
export enum NodeType {
  MESSAGE = 'MESSAGE',
  QUESTION = 'QUESTION',
  ACTION = 'ACTION',
  MERGE = 'MERGE',
  TERMINAL = 'TERMINAL',
}

// 对话节点
export interface DialogNode {
  id: string;
  type: NodeType;
  content: string;
  question?: QuestionConfig;
  action?: ActionConfig;
  children?: Record<string, string>;
  parent?: string;
}

// 提问配置
export interface QuestionConfig {
  expectedIntents?: string[];
  intentMappings?: Record<string, string>;
  timeout?: number;
  maxRetries?: number;
  timeoutIntent?: string;
  defaultIntent?: string;
}

// 动作配置
export interface ActionConfig {
  pluginName?: string;
  methodName?: string;
  parameters?: Record<string, any>;
}

// 对话树
export interface DialogTree {
  id: string;
  name: string;
  rootNodeId: string;
  nodes: Record<string, DialogNode>;
  metadata?: DialogTreeMetadata;
}

// 对话树元数据
export interface DialogTreeMetadata {
  applicableProducts?: string[];
  riskLevel?: string;
  version?: string;
  description?: string;
  createdAt?: number;
  updatedAt?: number;
}

// Agent 状态
export interface AgentState {
  sessionId: string | null;
  currentNode: DialogNode | null;
  isRecording: boolean;
  isProcessing: boolean;
  exception: ExceptionAlert | null;
  history: ChatMessage[];
}

// 异常提示
export interface ExceptionAlert {
  type: string;
  message: string;
  severity: 'warning' | 'error';
  timestamp: Date;
}

// Agent API 请求
export interface AgentTurnRequest {
  sessionId: string;
  input: {
    type: 'TEXT' | 'AUDIO';
    text?: string;
    audioData?: string;
  };
}

// Agent API 响应
export interface AgentTurnResponse {
  text: string;
  audioUrl?: string;
  nextNodeId: string;
  metadata: {
    intent: string;
    confidence: number;
  };
}
