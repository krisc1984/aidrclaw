<!-- GSD:project-start source:PROJECT.md -->
## Project

**智能双录系统**

**下一代智能双录系统** — 面向金融理财、基金、财富产品销售场景的可插拔、模块化、Agent 驱动的双录平台。

**核心定位**：纯私有化部署的企业级合规双录系统，通过 Agent 自动化和智能质检实现销售过程的合规留痕与高效审核。

---

**Core Value:** **单一核心价值**：通过 Agent 驱动的自动化双录流程，将金融销售合规双录的效率和体验提升到新水平，同时保证 100% 监管合规。

**核心价值验证指标**：
- 双录流程完成率 ≥ 95%
- 预质检自动化覆盖率 ≥ 80%
- 人工复核工作量减少 ≥ 60%
- 客户双录体验满意度 ≥ 4.5/5

---

### Constraints

### 技术约束

| 约束 | 影响 |
|------|------|
| **纯私有化部署** | 所有组件必须支持本地部署，不得依赖公有云服务 |
| **数据不出域** | 音视频及客户信息必须存储在私有环境，无明文外传 |
| **合规安全** | TLS 1.3 加密传输、文件哈希防篡改、RBAC 权限控制 |
| **插件化架构** | 所有核心能力必须以插件形式实现，支持热插拔 |

### 非功能需求

| 维度 | 目标 |
|------|------|
| **并发支持** | 单节点支持 100+ 并发录制会话，支持水平扩展 |
| **预质检延迟** | 录制完成后 5 分钟内输出质检结果 |
| **高可用** | 核心服务支持多副本部署，无单点故障 |
| **容错** | 客户端网络中断时支持断点续传或本地缓存 |

---
<!-- GSD:project-end -->

<!-- GSD:stack-start source:STACK.md -->
## Technology Stack

Technology stack not yet documented. Will populate after codebase mapping or first phase.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd:quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd:debug` for investigation and bug fixing
- `/gsd:execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd:profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
