# 企业级 RBAC + 数据权限系统设计（多租户）

> 技术基线：Spring Boot + Sa-Token + MyBatis-Plus + Vue3 + Pinia + VueRouter。

---

## 1. 权限模型设计

### 1.1 模型总览

采用 **RBAC（角色访问控制）+ 数据权限（Data Scope）+ 字段权限（Field Scope）+ 行级权限（Row Scope）** 的混合模型：

- **RBAC**：用户 -> 角色 -> 权限（菜单、按钮、接口）
- **Data Scope**：角色或用户对数据范围的约束
- **Field Scope**：字段可见/可编辑权限
- **Row Scope**：按行级条件过滤业务数据
- **Tenant Scope**：所有权限判定都在 `tenant_id` 隔离域内执行

### 1.2 权限点定义

1. **菜单权限**：页面入口可见与否。
2. **按钮权限**：操作按钮是否可见/可点。
3. **接口权限**：后端 API 调用授权。
4. **数据权限**：查询范围裁剪。
5. **字段权限**：结果字段可见/脱敏/只读。
6. **行级权限**：对某些记录按表达式限制访问。

### 1.3 数据范围策略枚举

- `SELF`：仅本人
- `DEPT`：本部门
- `DEPT_AND_CHILD`：本部门及子部门
- `CUSTOM_SQL`：自定义 SQL 片段
- `ALL`：全部数据

### 1.4 权限判定顺序（高性能）

1. 租户隔离（必须）
2. 登录态校验（Sa-Token）
3. 接口权限校验
4. 数据范围注入
5. 行级条件追加
6. 字段裁剪输出

> 说明：先拦截高层权限再执行 SQL，可显著降低无效查询。

---

## 2. ER 关系

### 2.1 核心实体

- `sys_user`（用户）
- `sys_role`（角色）
- `sys_menu`（菜单）
- `sys_permission`（权限点：MENU/BUTTON/API）
- `sys_data_scope_rule`（数据范围规则）
- `sys_field_permission`（字段权限）
- `sys_row_permission`（行级权限）

### 2.2 关系映射

- 用户与角色：`sys_user` N:M `sys_role`（`sys_user_role`）
- 角色与菜单：`sys_role` N:M `sys_menu`（`sys_role_menu`）
- 角色与权限点：`sys_role` N:M `sys_permission`（`sys_role_permission`）
- 角色与数据规则：`sys_role` 1:N `sys_data_scope_rule`
- 角色与字段权限：`sys_role` 1:N `sys_field_permission`
- 角色与行级权限：`sys_role` 1:N `sys_row_permission`

---

## 3. SQL（MySQL 8）

> 所有表统一字段：`id, tenant_id, create_by, create_time, update_by, update_time, deleted`。

```sql
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  username VARCHAR(64) NOT NULL COMMENT '登录账号',
  password VARCHAR(255) NOT NULL COMMENT '密码密文',
  real_name VARCHAR(128) NOT NULL COMMENT '真实姓名',
  dept_id BIGINT NULL COMMENT '部门ID',
  mobile VARCHAR(32) NULL COMMENT '手机号',
  email VARCHAR(128) NULL COMMENT '邮箱',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_username (tenant_id, username, deleted),
  KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
  data_scope_type VARCHAR(32) NOT NULL DEFAULT 'SELF' COMMENT '数据范围类型',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_role_code (tenant_id, role_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级菜单ID',
  menu_type VARCHAR(16) NOT NULL COMMENT '类型:DIR/MENU/BUTTON',
  menu_name VARCHAR(128) NOT NULL COMMENT '菜单名称',
  route_path VARCHAR(256) NULL COMMENT '路由路径',
  component_path VARCHAR(256) NULL COMMENT '前端组件路径',
  permission_code VARCHAR(128) NULL COMMENT '权限编码',
  icon VARCHAR(64) NULL COMMENT '图标',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  visible_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见:1是0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  KEY idx_parent_id (parent_id),
  KEY idx_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单与按钮表';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  perm_code VARCHAR(128) NOT NULL COMMENT '权限编码',
  perm_name VARCHAR(128) NOT NULL COMMENT '权限名称',
  perm_type VARCHAR(16) NOT NULL COMMENT '权限类型:MENU/BUTTON/API',
  api_path VARCHAR(256) NULL COMMENT '接口路径',
  api_method VARCHAR(16) NULL COMMENT '接口方法',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_perm_code (tenant_id, perm_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_user_role (tenant_id, user_id, role_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_role_menu (tenant_id, role_id, menu_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_role_perm (tenant_id, role_id, permission_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS sys_data_scope_rule (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  biz_code VARCHAR(64) NOT NULL COMMENT '业务编码(如order/list)',
  scope_type VARCHAR(32) NOT NULL COMMENT '范围类型:SELF/DEPT/DEPT_AND_CHILD/CUSTOM_SQL/ALL',
  custom_sql TEXT NULL COMMENT '自定义SQL片段(仅白名单字段)',
  priority INT NOT NULL DEFAULT 100 COMMENT '优先级(越小越高)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  KEY idx_role_biz (role_id, biz_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据范围规则表';

CREATE TABLE IF NOT EXISTS sys_field_permission (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  biz_code VARCHAR(64) NOT NULL COMMENT '业务编码',
  field_key VARCHAR(128) NOT NULL COMMENT '字段键',
  visible_flag TINYINT NOT NULL DEFAULT 1 COMMENT '可见:1是0否',
  editable_flag TINYINT NOT NULL DEFAULT 1 COMMENT '可编辑:1是0否',
  mask_type VARCHAR(32) NULL COMMENT '脱敏类型:NONE/PHONE/EMAIL/NAME/CUSTOM',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  KEY idx_role_biz_field (role_id, biz_code, field_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段权限表';

CREATE TABLE IF NOT EXISTS sys_row_permission (
  id BIGINT NOT NULL COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  biz_code VARCHAR(64) NOT NULL COMMENT '业务编码',
  row_expr VARCHAR(1024) NOT NULL COMMENT '行级表达式,如owner_id = ${userId}',
  priority INT NOT NULL DEFAULT 100 COMMENT '优先级(越小越高)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  create_by BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by BIGINT NOT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  KEY idx_role_biz (role_id, biz_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行级权限表';
```

---

## 4. Sa-Token 集成

### 4.1 登录写入权限快照

- 用户登录成功后，加载：
  - `roleCodes`
  - `permissionCodes`
  - `dataScopeRules`
  - `fieldPermissions`
- 将精简快照写入 Redis：`auth:snapshot:{tenantId}:{userId}`。

### 4.2 StpInterface 实现

```java
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return AuthSnapshotHolder.get().getPermissionCodes();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return AuthSnapshotHolder.get().getRoleCodes();
    }
}
```

### 4.3 接口权限校验

- Controller 使用 `@SaCheckPermission("order:list")`。
- 对高风险接口叠加 `@SaCheckRole("admin")`。

---

## 5. 权限注解

### 5.1 统一业务注解

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    String bizCode();
}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldScope {
    String bizCode();
}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RowScope {
    String bizCode();
}
```

### 5.2 注解组合建议

- 查询接口：`@SaCheckPermission + @DataScope + @RowScope + @FieldScope`
- 写入接口：`@SaCheckPermission + @RowScope`

---

## 6. Vue 权限指令

```ts
// src/directives/permission.ts
import type { Directive } from 'vue';
import { useAuthStore } from '@/stores/auth';

export const vPermission: Directive = {
  mounted(el, binding) {
    const authStore = useAuthStore();
    const code = binding.value as string;
    if (!authStore.permissionCodes.includes(code)) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};
```

- 用法：`<el-button v-permission="'order:create'">新建</el-button>`
- 建议扩展：支持 `anyOf/allOf` 组合表达式。

---

## 7. 动态菜单实现

### 7.1 后端

1. 根据用户角色查询 `sys_role_menu`。
2. 关联 `sys_menu` 过滤 `status=1 && visible_flag=1`。
3. 按 `parent_id + sort_no` 构建树。
4. 返回前端菜单 DTO（含 `routePath/componentPath/permissionCode/meta`）。

### 7.2 前端

- 登录后调用 `/api/auth/menu-tree`。
- 存入 Pinia `menuTree`。
- 左侧导航按树渲染。

---

## 8. 动态路由实现

### 8.1 路由注册流程

1. 登录后拉取菜单树。
2. 将菜单 `componentPath` 映射为 `import.meta.glob` 组件。
3. 通过 `router.addRoute('Layout', routeRecord)` 动态注入。
4. 注入后 `router.replace(current.fullPath)` 触发重解析。

### 8.2 关键代码（Vue3）

```ts
const modules = import.meta.glob('@/views/**/*.vue');

function resolveView(path: string) {
  return modules[`/src/views/${path}.vue`];
}

export function mountDynamicRoutes(menuTree: MenuNode[]) {
  menuTree.forEach(menu => {
    if (menu.menuType === 'MENU' && menu.routePath && menu.componentPath) {
      router.addRoute('Layout', {
        path: menu.routePath,
        name: menu.routeName,
        component: resolveView(menu.componentPath),
        meta: { perm: menu.permissionCode, title: menu.menuName }
      });
    }
  });
}
```

---

## 9. 数据权限拦截器（MyBatis-Plus）

### 9.1 设计

在 SQL 执行前拼接 WHERE 条件：

- 必带：`tenant_id = ?`
- 数据范围：`SELF/DEPT/DEPT_AND_CHILD/CUSTOM_SQL/ALL`
- 行级规则：`row_expr` 转换为 SQL 条件

### 9.2 拦截器核心逻辑

```java
public class DataPermissionInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        String originalSql = boundSql.getSql();
        DataScopeContext ctx = DataScopeContextHolder.get();
        String permissionSql = DataScopeSqlBuilder.build(ctx);
        String mergedSql = "SELECT * FROM (" + originalSql + ") t WHERE " + permissionSql;
        PluginUtils.mpBoundSql(boundSql).sql(mergedSql);
    }
}
```

### 9.3 DataScopeSqlBuilder 输出示例

- `SELF` -> `t.create_by = {userId}`
- `DEPT` -> `t.dept_id = {deptId}`
- `DEPT_AND_CHILD` -> `t.dept_id IN (子部门ID集合)`
- `CUSTOM_SQL` -> 白名单校验后拼接
- `ALL` -> `1=1`

---

## 10. 字段权限实现

### 10.1 后端输出裁剪

1. 查询结果映射为 `Map<String,Object>` 或 DTO。
2. 根据 `sys_field_permission` 对每个字段执行：
   - 不可见：删除字段
   - 只读：在元数据中标记 `editable=false`
   - 脱敏：按 `mask_type` 处理

### 10.2 字段脱敏策略

- PHONE：`138****1234`
- EMAIL：`j***@company.com`
- NAME：`张*`
- CUSTOM：执行自定义脱敏函数

### 10.3 前后端联动

- 后端返回 `fieldPermissionMap`。
- 前端 Form/Table 渲染前应用：
  - `visible=false` -> 不渲染列/字段
  - `editable=false` -> 组件 `disabled`

---

## 性能与扩展建议

1. **权限快照缓存**：登录后缓存权限，减少每次请求联表。
2. **规则预编译**：行级表达式与自定义 SQL 预编译并白名单校验。
3. **分层兜底**：接口权限失败立即拒绝，避免数据库负担。
4. **按业务编码隔离规则**：`biz_code` 维度管理，便于多系统复用。
5. **审计追踪**：记录权限命中链路（命中角色、规则、SQL片段）。

