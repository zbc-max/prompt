# 企业级低代码平台：元数据与 Schema 内核设计（不含业务 CRUD）

> 本文仅聚焦底层内核：元数据模型、Schema 模型、动态表单引擎、动态页面引擎。

---

## 1. 整体设计思想

### 1.1 架构原则

1. **元数据驱动**：所有页面/表单/组件/规则均以元数据存储，运行时解释执行。
2. **Schema 统一**：以 JSON Schema 为结构基线，以扩展 DSL 描述交互行为。
3. **版本冻结**：Schema 使用“草稿版 + 发布版 + 历史版”模型，运行时仅加载发布快照。
4. **权限前置**：字段权限、组件权限、事件权限在渲染前进行裁剪。
5. **引擎分层**：解析层、编译层、执行层、渲染层解耦。
6. **可扩展性**：组件、校验器、数据源适配器、事件执行器均可插件化扩展。

### 1.2 内核分层

- **Metadata Kernel**：元数据定义、版本、发布、回滚。
- **Schema Kernel**：Schema 校验、归一化、兼容升级。
- **Form Runtime**：字段生命周期、联动、校验、公式、权限。
- **Page Runtime**：页面布局、组件树、事件总线、页面生命周期。

### 1.3 统一标识规范

- `schema_code`：跨租户唯一（逻辑编码，含业务语义）。
- `schema_version`：语义版本（如 `1.0.0`），发布时不可变。
- `snapshot_id`：发布快照 ID，运行时直接引用。
- `component_key`：组件在页面内唯一键，用于事件与联动。
- `field_key`：字段唯一键，用于数据模型映射。

---

## 2. 元数据结构

> 元数据按“定义层 + 规则层 + 资源层 + 权限层”组织。

### 2.1 定义层

1. `lc_component_schema`：组件协议定义（组件能力、属性协议、事件协议）。
2. `lc_form_schema`：表单根 Schema（结构与行为入口）。
3. `lc_form_field_schema`：字段 Schema（字段级元数据）。
4. `lc_page_schema`：页面根 Schema（布局、组件树、页面配置）。

### 2.2 规则层

1. `lc_validation_rule`：字段/表单/页面可复用校验规则。
2. `lc_event_rule`：事件触发规则（监听、条件、动作链）。
3. `lc_permission_rule`：字段/组件/页面权限规则。

### 2.3 资源层

1. `lc_data_source`：API/SQL/字典/脚本数据源。
2. `lc_data_source_param`：数据源入参与鉴权参数。
3. `lc_data_source_mapping`：数据源返回值映射规则。

### 2.4 版本层

- `*_schema` 表保存当前草稿。
- `*_schema_version` 表保存历史版本。
- `*_schema_publish` 保存发布记录与快照定位。

---

## 3. Schema 结构

### 3.1 form_schema（JSON）

```json
{
  "schemaCode": "hr_leave_form",
  "schemaName": "请假申请",
  "version": "1.0.0",
  "model": {
    "object": "leave_application",
    "primaryKey": "id"
  },
  "layout": {
    "type": "grid",
    "columns": 24,
    "gutter": 12
  },
  "fields": [
    {
      "fieldKey": "leaveType",
      "componentType": "select",
      "label": "请假类型",
      "required": true,
      "dataSourceRef": "ds_leave_type",
      "defaultValue": "annual"
    }
  ],
  "rules": {
    "validationRuleRefs": ["vr_leave_days"],
    "eventRuleRefs": ["er_leave_linkage"],
    "permissionRuleRefs": ["pr_leave_field_edit"]
  }
}
```

### 3.2 page_schema（JSON）

```json
{
  "schemaCode": "hr_leave_page",
  "schemaName": "请假管理页",
  "version": "1.0.0",
  "route": "/hr/leave",
  "layout": {
    "type": "grid",
    "breakpoints": {"xs": 24, "sm": 12, "md": 8, "lg": 6}
  },
  "componentTree": [
    {
      "componentKey": "queryForm",
      "componentType": "form-container",
      "children": [
        {"componentKey": "queryLeaveType", "componentType": "select"}
      ]
    },
    {
      "componentKey": "listTable",
      "componentType": "table"
    }
  ],
  "lifecycle": {
    "onInit": ["er_page_init"],
    "onMounted": ["er_page_load"],
    "onBeforeLeave": ["er_page_guard"]
  }
}
```

### 3.3 component_schema（JSON）

```json
{
  "componentType": "select",
  "displayName": "下拉选择",
  "category": "basic",
  "propsSchema": {
    "allowClear": {"type": "boolean", "default": true},
    "multiple": {"type": "boolean", "default": false}
  },
  "eventSchema": {
    "change": {"payload": ["value", "option"]}
  },
  "permissionPoints": ["visible", "editable"],
  "supportedBindings": ["dataSource", "defaultValue", "formula"]
}
```

### 3.4 field_schema（JSON）

```json
{
  "fieldKey": "leaveDays",
  "fieldName": "请假天数",
  "dbType": "decimal(10,2)",
  "componentType": "number",
  "required": true,
  "defaultValue": 1,
  "formula": "dateDiff(endDate,startDate)+1",
  "visibleWhen": "leaveType != 'other'",
  "editableWhen": "status == 'draft'",
  "validationRuleRefs": ["vr_leave_days"]
}
```

---

## 4. 表关系设计

### 4.1 核心关系

- `lc_component_schema` 1:N `lc_component_schema_version`
- `lc_form_schema` 1:N `lc_form_schema_version`
- `lc_form_schema` 1:N `lc_form_field_schema`
- `lc_page_schema` 1:N `lc_page_schema_version`
- `lc_form_schema` N:M `lc_data_source`（通过 `lc_schema_data_source_ref`）
- `lc_form_schema` N:M `lc_validation_rule`（通过 `lc_schema_validation_ref`）
- `lc_form_schema` N:M `lc_event_rule`（通过 `lc_schema_event_ref`）
- `lc_form_schema` N:M `lc_permission_rule`（通过 `lc_schema_permission_ref`）
- `lc_page_schema` N:M `lc_event_rule`（通过 `lc_page_event_ref`）

### 4.2 强制审计字段

所有表统一包含：

- `id`
- `tenant_id`
- `create_by`
- `create_time`
- `update_by`
- `update_time`
- `deleted`

---

## 5. 完整 SQL（MySQL 8）

```sql
CREATE TABLE IF NOT EXISTS lc_component_schema (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  component_type VARCHAR(64) NOT NULL COMMENT '组件类型编码',
  display_name VARCHAR(128) NOT NULL COMMENT '组件显示名称',
  category VARCHAR(64) NOT NULL COMMENT '组件分类',
  props_schema_json JSON NOT NULL COMMENT '属性Schema(JSON)',
  event_schema_json JSON NULL COMMENT '事件Schema(JSON)',
  permission_points_json JSON NULL COMMENT '权限点定义(JSON)',
  supported_bindings_json JSON NULL COMMENT '支持绑定能力(JSON)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_component_type (tenant_id, component_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组件Schema定义表';

CREATE TABLE IF NOT EXISTS lc_component_schema_version (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  component_schema_id BIGINT NOT NULL COMMENT '组件Schema主表ID',
  version_no VARCHAR(32) NOT NULL COMMENT '版本号',
  snapshot_json JSON NOT NULL COMMENT '版本快照(JSON)',
  publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态:0草稿1已发布2归档',
  publish_time DATETIME NULL COMMENT '发布时间',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_component_ver (tenant_id, component_schema_id, version_no, deleted),
  KEY idx_component_schema_id (component_schema_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组件Schema版本表';

CREATE TABLE IF NOT EXISTS lc_form_schema (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  schema_code VARCHAR(128) NOT NULL COMMENT '表单Schema编码',
  schema_name VARCHAR(256) NOT NULL COMMENT '表单Schema名称',
  domain_code VARCHAR(64) NOT NULL COMMENT '所属业务域编码',
  current_version_no VARCHAR(32) NULL COMMENT '当前版本号',
  schema_json JSON NOT NULL COMMENT '表单Schema(JSON)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_schema_code (tenant_id, schema_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单Schema主表';

CREATE TABLE IF NOT EXISTS lc_form_schema_version (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  form_schema_id BIGINT NOT NULL COMMENT '表单Schema主表ID',
  version_no VARCHAR(32) NOT NULL COMMENT '版本号',
  snapshot_json JSON NOT NULL COMMENT 'Schema快照(JSON)',
  publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态:0草稿1已发布2归档',
  publish_time DATETIME NULL COMMENT '发布时间',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_form_ver (tenant_id, form_schema_id, version_no, deleted),
  KEY idx_form_schema_id (form_schema_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单Schema版本表';

CREATE TABLE IF NOT EXISTS lc_form_field_schema (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  form_schema_id BIGINT NOT NULL COMMENT '表单Schema主表ID',
  field_key VARCHAR(128) NOT NULL COMMENT '字段唯一键',
  field_name VARCHAR(256) NOT NULL COMMENT '字段名称',
  db_type VARCHAR(64) NOT NULL COMMENT '字段数据库类型',
  component_type VARCHAR(64) NOT NULL COMMENT '组件类型',
  field_order INT NOT NULL DEFAULT 0 COMMENT '字段顺序',
  required_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填:0否1是',
  default_value_json JSON NULL COMMENT '默认值(JSON)',
  formula_expr TEXT NULL COMMENT '公式表达式',
  visible_when_expr TEXT NULL COMMENT '显示条件表达式',
  editable_when_expr TEXT NULL COMMENT '编辑条件表达式',
  ext_json JSON NULL COMMENT '扩展配置(JSON)',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_form_field (tenant_id, form_schema_id, field_key, deleted),
  KEY idx_form_schema_id (form_schema_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单字段Schema表';

CREATE TABLE IF NOT EXISTS lc_page_schema (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  schema_code VARCHAR(128) NOT NULL COMMENT '页面Schema编码',
  schema_name VARCHAR(256) NOT NULL COMMENT '页面Schema名称',
  route_path VARCHAR(256) NOT NULL COMMENT '页面路由路径',
  current_version_no VARCHAR(32) NULL COMMENT '当前版本号',
  layout_schema_json JSON NOT NULL COMMENT '布局Schema(JSON)',
  page_schema_json JSON NOT NULL COMMENT '页面Schema(JSON)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_page_code (tenant_id, schema_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面Schema主表';

CREATE TABLE IF NOT EXISTS lc_page_schema_version (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  page_schema_id BIGINT NOT NULL COMMENT '页面Schema主表ID',
  version_no VARCHAR(32) NOT NULL COMMENT '版本号',
  snapshot_json JSON NOT NULL COMMENT 'Schema快照(JSON)',
  publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态:0草稿1已发布2归档',
  publish_time DATETIME NULL COMMENT '发布时间',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_page_ver (tenant_id, page_schema_id, version_no, deleted),
  KEY idx_page_schema_id (page_schema_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面Schema版本表';

CREATE TABLE IF NOT EXISTS lc_data_source (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  source_code VARCHAR(128) NOT NULL COMMENT '数据源编码',
  source_name VARCHAR(256) NOT NULL COMMENT '数据源名称',
  source_type VARCHAR(32) NOT NULL COMMENT '数据源类型:API/SQL/DICT/SCRIPT',
  http_method VARCHAR(16) NULL COMMENT 'HTTP方法',
  endpoint_url VARCHAR(512) NULL COMMENT '接口URL',
  sql_text TEXT NULL COMMENT 'SQL语句',
  auth_type VARCHAR(32) NULL COMMENT '鉴权类型',
  timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '超时时间(毫秒)',
  cache_ttl_sec INT NOT NULL DEFAULT 0 COMMENT '缓存秒数',
  response_mapping_json JSON NULL COMMENT '响应映射(JSON)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_source_code (tenant_id, source_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源定义表';

CREATE TABLE IF NOT EXISTS lc_validation_rule (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  rule_code VARCHAR(128) NOT NULL COMMENT '校验规则编码',
  rule_name VARCHAR(256) NOT NULL COMMENT '校验规则名称',
  target_scope VARCHAR(32) NOT NULL COMMENT '作用域:FIELD/FORM/PAGE',
  rule_type VARCHAR(32) NOT NULL COMMENT '规则类型:REGEX/FUNCTION/SCRIPT',
  rule_expr TEXT NOT NULL COMMENT '规则表达式',
  error_message VARCHAR(512) NOT NULL COMMENT '错误提示信息',
  priority INT NOT NULL DEFAULT 100 COMMENT '优先级(越小越先执行)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_validation_rule (tenant_id, rule_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校验规则表';

CREATE TABLE IF NOT EXISTS lc_event_rule (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  rule_code VARCHAR(128) NOT NULL COMMENT '事件规则编码',
  rule_name VARCHAR(256) NOT NULL COMMENT '事件规则名称',
  trigger_scope VARCHAR(32) NOT NULL COMMENT '触发范围:FIELD/COMPONENT/PAGE',
  trigger_event VARCHAR(64) NOT NULL COMMENT '触发事件:onChange/onClick/onLoad',
  condition_expr TEXT NULL COMMENT '触发条件表达式',
  action_chain_json JSON NOT NULL COMMENT '动作链(JSON)',
  async_flag TINYINT NOT NULL DEFAULT 0 COMMENT '异步执行:0否1是',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_event_rule (tenant_id, rule_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件规则表';

CREATE TABLE IF NOT EXISTS lc_permission_rule (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  rule_code VARCHAR(128) NOT NULL COMMENT '权限规则编码',
  rule_name VARCHAR(256) NOT NULL COMMENT '权限规则名称',
  target_scope VARCHAR(32) NOT NULL COMMENT '作用范围:PAGE/COMPONENT/FIELD',
  permission_type VARCHAR(32) NOT NULL COMMENT '权限类型:VISIBLE/EDITABLE/DISABLED/HIDDEN',
  subject_type VARCHAR(32) NOT NULL COMMENT '主体类型:ROLE/USER/DEPT/EXPR',
  subject_value VARCHAR(512) NOT NULL COMMENT '主体值(角色编码/表达式)',
  effect_type VARCHAR(16) NOT NULL COMMENT '生效类型:ALLOW/DENY',
  priority INT NOT NULL DEFAULT 100 COMMENT '优先级(越小越先执行)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_permission_rule (tenant_id, rule_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限规则表';

CREATE TABLE IF NOT EXISTS lc_schema_data_source_ref (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  schema_type VARCHAR(16) NOT NULL COMMENT 'Schema类型:FORM/PAGE',
  schema_id BIGINT NOT NULL COMMENT 'Schema主表ID',
  bind_target_key VARCHAR(128) NOT NULL COMMENT '绑定目标键(fieldKey/componentKey)',
  data_source_id BIGINT NOT NULL COMMENT '数据源ID',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schema_ds_ref (tenant_id, schema_type, schema_id, bind_target_key, data_source_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Schema与数据源绑定关系表';

CREATE TABLE IF NOT EXISTS lc_schema_validation_ref (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  schema_type VARCHAR(16) NOT NULL COMMENT 'Schema类型:FORM/PAGE',
  schema_id BIGINT NOT NULL COMMENT 'Schema主表ID',
  target_key VARCHAR(128) NOT NULL COMMENT '目标键(fieldKey/componentKey)',
  validation_rule_id BIGINT NOT NULL COMMENT '校验规则ID',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schema_validation_ref (tenant_id, schema_type, schema_id, target_key, validation_rule_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Schema与校验规则绑定关系表';

CREATE TABLE IF NOT EXISTS lc_schema_event_ref (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  schema_type VARCHAR(16) NOT NULL COMMENT 'Schema类型:FORM/PAGE',
  schema_id BIGINT NOT NULL COMMENT 'Schema主表ID',
  target_key VARCHAR(128) NOT NULL COMMENT '目标键(fieldKey/componentKey/pageKey)',
  event_rule_id BIGINT NOT NULL COMMENT '事件规则ID',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schema_event_ref (tenant_id, schema_type, schema_id, target_key, event_rule_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Schema与事件规则绑定关系表';

CREATE TABLE IF NOT EXISTS lc_schema_permission_ref (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  schema_type VARCHAR(16) NOT NULL COMMENT 'Schema类型:FORM/PAGE',
  schema_id BIGINT NOT NULL COMMENT 'Schema主表ID',
  target_key VARCHAR(128) NOT NULL COMMENT '目标键(fieldKey/componentKey/pageKey)',
  permission_rule_id BIGINT NOT NULL COMMENT '权限规则ID',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schema_permission_ref (tenant_id, schema_type, schema_id, target_key, permission_rule_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Schema与权限规则绑定关系表';
```

---

## 6. Java 实体（Java 17）

```java
package com.example.lowcode.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础审计实体
 */
@Data
public abstract class BaseTenantEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

```java
package com.example.lowcode.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 表单Schema主表实体
 */
@Data
@TableName("lc_form_schema")
public class LcFormSchemaEntity extends BaseTenantEntity {
    private String schemaCode;
    private String schemaName;
    private String domainCode;
    private String currentVersionNo;

    @TableField("schema_json")
    private String schemaJson;

    private Integer status;
}
```

```java
package com.example.lowcode.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 表单字段Schema实体
 */
@Data
@TableName("lc_form_field_schema")
public class LcFormFieldSchemaEntity extends BaseTenantEntity {
    private Long formSchemaId;
    private String fieldKey;
    private String fieldName;
    private String dbType;
    private String componentType;
    private Integer fieldOrder;
    private Integer requiredFlag;
    private String defaultValueJson;
    private String formulaExpr;
    private String visibleWhenExpr;
    private String editableWhenExpr;
    private String extJson;
}
```

```java
package com.example.lowcode.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 页面Schema主表实体
 */
@Data
@TableName("lc_page_schema")
public class LcPageSchemaEntity extends BaseTenantEntity {
    private String schemaCode;
    private String schemaName;
    private String routePath;
    private String currentVersionNo;

    @TableField("layout_schema_json")
    private String layoutSchemaJson;

    @TableField("page_schema_json")
    private String pageSchemaJson;

    private Integer status;
}
```

```java
package com.example.lowcode.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据源实体
 */
@Data
@TableName("lc_data_source")
public class LcDataSourceEntity extends BaseTenantEntity {
    private String sourceCode;
    private String sourceName;
    private String sourceType;
    private String httpMethod;
    private String endpointUrl;
    private String sqlText;
    private String authType;
    private Integer timeoutMs;
    private Integer cacheTtlSec;
    private String responseMappingJson;
    private Integer status;
}
```

---

## 7. 前端 Schema 类型定义（TypeScript）

```ts
export type SchemaVersion = string;

export interface BaseAuditMeta {
  id: string;
  tenantId: string;
  createBy: string;
  createTime: string;
  updateBy: string;
  updateTime: string;
  deleted: 0 | 1;
}

export interface ComponentSchema {
  componentType: string;
  displayName: string;
  category: string;
  propsSchema: Record<string, unknown>;
  eventSchema?: Record<string, unknown>;
  permissionPoints?: Array<'visible' | 'editable' | 'disabled'>;
  supportedBindings?: Array<'dataSource' | 'defaultValue' | 'formula'>;
}

export interface FieldSchema {
  fieldKey: string;
  fieldName: string;
  dbType: string;
  componentType: string;
  required: boolean;
  defaultValue?: unknown;
  formula?: string;
  visibleWhen?: string;
  editableWhen?: string;
  validationRuleRefs?: string[];
}

export interface FormSchema {
  schemaCode: string;
  schemaName: string;
  version: SchemaVersion;
  model: {
    object: string;
    primaryKey: string;
  };
  layout: {
    type: 'grid';
    columns: number;
    gutter: number;
  };
  fields: FieldSchema[];
  rules: {
    validationRuleRefs: string[];
    eventRuleRefs: string[];
    permissionRuleRefs: string[];
  };
}

export interface PageComponentNode {
  componentKey: string;
  componentType: string;
  props?: Record<string, unknown>;
  children?: PageComponentNode[];
}

export interface PageSchema {
  schemaCode: string;
  schemaName: string;
  version: SchemaVersion;
  route: string;
  layout: {
    type: 'grid';
    breakpoints: Record<string, number>;
  };
  componentTree: PageComponentNode[];
  lifecycle: {
    onInit?: string[];
    onMounted?: string[];
    onBeforeLeave?: string[];
  };
}
```

---

## 8. Schema 解析流程（Form Engine）

### 8.1 输入

- `schema_code`
- `version`（可空，空则取发布版本）
- `tenant_id`
- 当前用户上下文（角色、部门、岗位、数据范围）

### 8.2 解析阶段

1. **版本解析**：读取 `lc_form_schema_version` 发布快照。
2. **结构校验**：按 `lc_component_schema` 验证字段组件合法性。
3. **规则装配**：装配 `validation/event/permission` 规则集合。
4. **数据源注入**：按 `lc_schema_data_source_ref` 注入字段数据源。
5. **权限裁剪**：执行 `lc_permission_rule`，生成可见/可编辑矩阵。
6. **公式编译**：将 `formula_expr` 编译为安全执行单元。
7. **执行计划生成**：产出渲染树、联动图、校验图、事件图。

### 8.3 输出

- `RenderSchema`（已裁剪）
- `FieldPermissionMatrix`
- `EventExecutionPlan`
- `ValidationExecutionPlan`

---

## 9. 页面渲染流程（Page Engine）

### 9.1 页面初始化

1. 路由匹配 `route_path` -> 定位 `lc_page_schema`。
2. 获取发布版本 `lc_page_schema_version.snapshot_json`。
3. 执行权限预裁剪（页面级/组件级）。
4. 构建组件树 Virtual Node。

### 9.2 生命周期执行

1. 执行 `onInit`：初始化上下文、加载首屏数据。
2. 执行 `onMounted`：触发异步数据源请求与懒加载组件。
3. 用户交互触发事件总线：
   - 事件匹配 `lc_event_rule`
   - 条件判断
   - 动作链执行（更新字段、请求数据源、组件状态变更、消息通知）
4. 路由离开执行 `onBeforeLeave`：未保存拦截、清理订阅。

### 9.3 动态渲染关键点

- 栅格布局使用 `layout.breakpoints` 响应式渲染。
- 组件树按 `componentType` 映射到组件注册中心。
- 组件属性由“静态属性 + 表达式属性 + 事件结果”合并。
- 页面局部刷新采用依赖图精确更新，避免整页重渲染。

