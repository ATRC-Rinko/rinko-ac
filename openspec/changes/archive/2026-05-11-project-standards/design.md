## Context

当前项目已有 `docs/spec.md`（v1.2）定义架构和 `docs/constitution.md` 定义 4 条不可变原则。`rinko-infra` 和 `rinko-auth` 已完整实现，其余 5 个模块为骨架状态。现有代码中隐含着大量编码模式和约定（包结构、类命名、异常处理、Repository 模式等），但这些模式未被显式文档化，导致后续模块开发缺乏统一参照。

本设计的目标是从现有代码中提取模式，形成 6 份可执行的规范文档，确保未来新增的所有模块保持一致的架构风格和代码质量。

### 约束
- 规范必须与已有代码**向后兼容**——不能要求 rinko-infra、rinko-auth 进行重构
- 规范必须符合 `docs/constitution.md` 的 4 条不可变原则
- 规范必须与 `docs/spec.md` 中定义的技术栈保持一致
- 规范文件存放于 `openspec/specs/` 目录下，与 `docs/` 形成互补（docs 宏观架构，openspec/specs 微观实操）

## Goals / Non-Goals

**Goals:**
- 从现有代码中提取 6 类规范，形成可执行的编码标准
- 每份规范包含具体代码示例、反例、检查清单
- 优先覆盖已有代码中出现过的模式（不假设未来需求）
- 规范可直接指导 rinko-gateway/oss/log/notify/scheduler 的后续实现

**Non-Goals:**
- 不修改已有代码以"符合规范"——已实现代码就是规范来源
- 不引入新的依赖库或工具链
- 不覆盖 UI/前端规范
- 不覆盖 CI/CD 部署规范（已在 docs/plan.md 阶段七定义）

## Decisions

### 1. 规范来源策略：以已有代码为唯一真源

**决策**: 每项规范要求必须能在 rinko-infra 或 rinko-auth 中找到至少一个实际用例。

**理由**: 避免规范与代码脱节。在 rinko-infra（Java Servlet）和 rinko-auth（Kotlin WebFlux）两种不同技术栈中验证过的模式，已证明在项目中可行。

**替代方案**: 参考 Spring 官方最佳实践编写规范 → 可能引入与项目实际代码不一致的要求，导致开发者困惑。

### 2. 规范粒度：SPEC-level（需求层），非实现指南

**决策**: 规范以 "SHALL/MUST" 级别的要求定义，每项要求附 WHEN/THEN 场景。不写 "如何实现" 的教程式内容。

**理由**: 符合 OpenSpec 的 spec-driven 模式——spec 定义 WHAT，实现者自行决定 HOW。避免规范文档沦为大段教程。

### 3. Java vs Kotlin 规范分离与统一

**决策**: Java 和 Kotlin 共享大部分通用规范（包结构、API 设计、数据库迁移），但在代码风格相关的具体语法层分别规定（如 Kotlin data class vs Java record、命名约定差异）。

**理由**: 项目是混合语言项目（Java infra/oss/log/notify/scheduler + Kotlin gateway/auth）。规范需要覆盖两种语言，但不能强制 Kotlin 使用 Java 约定或反之。

### 4. 规范文件存放位置

**决策**: 6 份规范文件存放于 `openspec/specs/{capability-name}/spec.md`，与 `docs/` 目录共存但职责不同。

**理由**: `docs/` 是项目架构文档（给人读的宏观设计），`openspec/specs/` 是 OpenSpec 工具链管理的需求规格（机器可解析的结构化 spec）。两者互补：docs/spec.md 回答 "项目长什么样"，openspec/specs 回答 "编码要遵循什么规则"。

## Risks / Trade-offs

- **[风险] 规范可能遗漏已有代码中未出现的模式** → 规范仅覆盖 rinko-infra 和 rinko-auth 中已验证的模式，后续新模块引入新模式时可追加规范
- **[风险] Kotlin 和 Java 规范不一致** → 两种语言的 spec 从同一份代码模式中提取，差异仅出现在语法层（如 Kotlin data class vs Java @Data），语义层保持完全一致
- **[取舍] 规范偏严格可能降低初期开发速度** → 接受此 trade-off。严格规范确保 7 个模块长期保持一致的代码风格，前期学习成本 < 后期维护收益
