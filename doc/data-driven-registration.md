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
RegisterEvent 分发       — 复用现有事件链，tab 通过
                            ItemRegModifiers.TAB_TO_BY_KEY_BY_SUPPLIER
                            以 Modifier 形式内联注入
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
  "groups": [ ... ],       // (可选) Group 定义数组
  "blocks": [ ... ],       // (可选) Block 定义数组
  "items": [ ... ],        // (可选, 计划中) Item 定义数组（无对应 Block）
  "block_entities": [ ... ] // (可选, 计划中) BlockEntity 定义数组
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
  "model": "kuayue:models/block/22_floor",
  "textures": {
    "0": "kuayue:block/22_floor"
  },
  "state_machine": "kuayue:panel_state_machine",
  "block_entity": {
    "type": "kuayue:simple_tile",
    "data": { }
  }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 注册名，格式 `namespace:path` |
| `type` | string | 是 | 工厂类型，需在 `FactoryRegistry` 中注册 |
| `group` | string | 否 | 挂载的组 id |
| `properties` | object | 否 | 方块属性 |
| `item_properties` | object | 否 | 物品属性（如创造标签页） |
| `model` | string | 否 | 模型路径 |
| `textures` | object | 否 | 纹理引用 |
| `state_machine` | string | 否 | 状态机 id |
| `block_entity` | object | 否 | BlockEntity 绑定 |

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
  "no_collission": true,
  "requires_correct_tool": true,
  "random_ticks": true,
  "sound_type": "metal",
  "light_emission": 15,
  "friction": 0.8,
  "speed_factor": 0.5,
  "jump_factor": 1.5,
  "dynamic_shape": true,
  "destroy_time": 1.5,
  "explosion_resistance": 3.0
}
```

| 属性 | JSON 类型 | 说明 |
|------|-----------|------|
| `no_occlusion` | boolean | 不遮挡相邻方块 |
| `strength` | number 或 [number, number] | 破坏时间/硬度，两元素数组为 [hardness, resistance] |
| `map_color` | string | 地图颜色（可使用 dye color 名称） |
| `no_collission` | boolean | 无碰撞箱 |
| `requires_correct_tool` | boolean | 需要合适工具才能掉落 |
| `random_ticks` | boolean | 随机刻更新 |
| `sound_type` | string | 放置/破坏音效类型 |
| `light_emission` | number | 亮度等级 (0-15) |
| `friction` | number | 摩擦力系数 |
| `speed_factor` | number | 行走速度倍率 |
| `jump_factor` | number | 跳跃高度倍率 |
| `dynamic_shape` | boolean | 动态碰撞箱 |
| `replaceable` | boolean | 是否可替换 |
| `destroy_time` | number | 破坏时间（单个值，与 `strength` 互斥） |
| `explosion_resistance` | number | 爆炸抗性 |
| `no_loot_table` | boolean | 无战利品表 |
| `ignited_by_lava` | boolean | 可被熔岩点燃 |
| `liquid` | boolean | 流体 |
| `force_solid_on` | boolean | 强制固实 |
| `air` | boolean | 空气 |
| `no_terrain_particles` | boolean | 无地形粒子 |

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
- [x] **SlabReg.getEntry()** 返回 `null` — 已通过 `ChildrenUtils.traverseRI()` 递归子节点修复
- [ ] **Group 属性继承**：子 block 的 tab 会从 group 继承，但其他 `item_properties` 字段的继承尚未完全覆盖
- [ ] **更多注册类型**：目前仅支持 block 注册，尚未扩展到 `items`、`block_entities` 等其他 JSON 顶层类型
- [x] **循环依赖检测** — 已使用 `GraphCycleDetector.topologicalSort()` 实现 Group parent 链检测
- [ ] **模型状态绑定**：`state_machine` 字段已解析但尚未在运行时消费
