# 数据驱动注册系统

> **WIP** — 本文档描述正在开发中的功能，API 和行为可能发生变化。

## 概述

数据驱动注册系统允许模组开发者通过 JSON 文件注册方块（Block），无需编写 Java 代码。系统自动扫描模组资源目录下的 JSON 定义文件，解析后构建虚拟注册树，与现有的 Java 注册体系集成。

目标：让非开发者也能参与内容创作，同时保持现有 Java API 的向后兼容。

---

## 架构

```
JSON 文件 (data/<modid>/kasugalib/*.json)
        │
        ▼
JsonTreeBuilder          — 扫描目录、解析 JSON、构建 RawData
        │
        ▼
JsonPropertyParser       — 将 JSON 属性转换为 Modifier<BlockBehaviour.Properties>
        │
        ▼
FactoryRegistry          — 根据 type 字符串创建对应的 Reg 实例
        │
        ▼
JsonRegistryGroup        — 虚拟注册组，挂载到主注册树
        │
        ▼
CreativeTabCollector     — 将注册的方块填入指定创造模式标签页
```

### 集成时机

```
Mod 加载
  │
  ├── @Context 类 static 初始化
  │     └── FactoryRegistry.register()   ← 注册工厂类型
  │
  └── Registry dispatch（首次分派时）
        └── JsonTreeBuilder.buildForMod()  ← 惰性构建 JSON 树
              └── modRegistry.addChild()   ← 挂载到注册树
```

**时序保证**：JSON 树的构建被推迟到首次 Registry dispatch 时执行，确保所有 `@Context` 类的 static 初始化（包括 `FactoryRegistry` 注册）已完成。

---

## JSON 文件

### 位置

```
src/main/resources/data/<modid>/kasugalib/<任意文件名>.json
```

文件会被自动扫描，支持多文件分散定义，按文件名排序处理。

### 结构

```json
{
  "groups": [ ... ],
  "blocks": [ ... ]
}
```

### Groups（组）

组用于定义共用的属性集和层级结构，支持嵌套（通过 `parent` 字段）。

```json
{
  "id": "kuayue:c22_panels",
  "parent": null,
  "properties": { ... },
  "item_properties": { ... }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 唯一标识，建议带 namespace |
| `parent` | string | 否 | 父组 id，不指定则挂到根组 |
| `properties` | object | 否 | 方块属性，所有子 block 继承 |
| `item_properties` | object | 否 | 物品属性，所有子 block 继承 |

### Blocks（方块）

```json
{
  "id": "kuayue:22_floor",
  "type": "slab",
  "group": "kuayue:c22_panels",
  "properties": { ... },
  "item_properties": { ... },
  "model": { ... },
  "textures": { ... }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 注册名，格式 `namespace:path` |
| `type` | string | 是 | 工厂类型，需在 `FactoryRegistry` 中注册 |
| `group` | string | 否 | 挂载的组 id |
| `properties` | object | 否 | 方块属性 |
| `item_properties` | object | 否 | 物品属性（如创造标签页） |
| `model` | object | 否 | 模型引用 |
| `textures` | object | 否 | 纹理引用 |

**优先级**：block 级属性 > group 级继承属性。当 block 的 `item_properties` 未指定 `tab` 时，自动从所属 group 继承。

---

## 属性

### 方块属性 (properties)

JSON 中支持的方块属性通过 `JsonPropertyParser` 解析，映射到 `BlockBehaviour.Properties`。

```json
{
  "no_occlusion": true,
  "strength": [1.5, 3.0],
  "map_color": "blue",
  "no_collision": true,
  "requires_correct_tool_for_drops": true,
  "random_ticks": true,
  "sound": "stone",
  "light_level": 15,
  "friction": 0.8,
  "speed_factor": 0.5,
  "jump_factor": 1.5,
  "dynamic_shapes": true
}
```

| 属性 | JSON 类型 | 说明 |
|------|-----------|------|
| `no_occlusion` | boolean | 不遮挡相邻方块 |
| `strength` | number 或 [number, number] | 破坏时间/硬度，两元素数组为 [hardness, resistance] |
| `map_color` | string | 地图颜色（可使用 dye color 名称） |
| `no_collision` | boolean | 无碰撞箱 |
| `requires_correct_tool_for_drops` | boolean | 需要合适工具才能掉落 |
| `random_ticks` | boolean | 随机刻更新 |
| `sound` | string | 放置/破坏音效类型 |
| `light_level` | number | 亮度等级 (0-15) |
| `friction` | number | 摩擦力系数 |
| `speed_factor` | number | 行走速度倍率 |
| `jump_factor` | number | 跳跃高度倍率 |
| `dynamic_shapes` | boolean | 动态碰撞箱 |

### 物品属性 (item_properties)

```json
{
  "tab": "kuayue:train_panel_tab",
  "stacks_to": 64
}
```

| 属性 | JSON 类型 | 说明 |
|------|-----------|------|
| `tab` | string | 创造模式标签页的 ResourceLocation |
| `stacks_to` | number | 最大堆叠数 |

---

## 工厂类型

工厂类型需在 `FactoryRegistry` 中通过 Java 注册，然后才能被 JSON 引用。

### 内置工厂

当前已在 `C22JsonFactory` 中注册的工厂类型：

| type | 说明 | Java 类 |
|------|------|---------|
| `train_panel` | 普通面板 | `PanelReg<TrainPanelBlock>` |
| `slab` | 下半砖 | `SlabReg<SlabBlock>` |
| `slab_top` | 上半砖 | `SlabReg<SlabBlock>` (top=true) |
| `ladder` | 梯子 | `SlabReg<LadderBlock>` |
| `train_small_window` | 小窗户 | `PanelReg<TrainSmallWindowBlock>` |
| `train_openable_window_1` | 单格可开关窗 | `PanelReg<TrainOpenableWindowBlock>` (width=1) |
| `train_openable_window_2` | 双格可开关窗 | `PanelReg<TrainOpenableWindowBlock>` (width=2) |
| `train_hinge_panel` | 铰链面板 | `PanelReg<TrainHingePanelBlock>` |
| `air_vent` | 通风口 | `SlabReg<AirVentBlock>` |

### 注册新工厂类型

```java
@Context
public class MyFactory {
    static {
        FactoryRegistry.register("my_block_type", id -> new SomeReg<>(id, SomeBlock::new));
    }
}
```

必须使用 `@Context` 注解以保证 static 初始化在 JSON 树构建前完成。

---

## 实际示例：C22 车厢

### Java 代码保留部分

构造器或初始化逻辑复杂的部分保留在 Java 中：

- `DOOR_22` — CustomRenderedDoorBlock，多纹理、多位置参数
- `C22_END_FACE` — CustomRenderedEndFaceBlock，需要传递 DoorType 和渲染信息

### 从 Java 迁移到 JSON 的部分

14 个方块的注册定义被移入 JSON，包括 floor、panel、window、ladder、coupler、carport 等组件。Group 定义了共用属性（`no_occlusion`, `strength`, `map_color`）和创造标签页。

---

## 已知限制 / WIP

- [ ] **模型/纹理**：JSON 中未明确指定模型和纹理路径时，按 Minecraft 约定路径自动查找（`assets/<namespace>/models/block/<path>.json`），需手动补全
- [ ] **翻译键**：自动推导为 `block.<namespace>.<path>`，需在 lang 文件中手动添加
- [ ] **FactoryRegistry**：目前仅有少数工厂类型，尚未覆盖所有 KuaYue block 变体
- [ ] **SlabReg.getEntry()**：SlabReg 的 `getEntry()` 返回 `null`（已通过 `findBlock()` 递归子节点修复）
- [ ] **Group 属性继承**：子 block 的 tab 会从 group 继承，但其他 item_properties 字段的继承尚未完全覆盖
- [ ] **更多注册类型**：目前仅支持 block 注册，尚未扩展到 item、entity 等其他类型
- [ ] **循环依赖检测**：已支持 group 间的循环依赖检测，但更复杂的依赖场景未经充分测试
