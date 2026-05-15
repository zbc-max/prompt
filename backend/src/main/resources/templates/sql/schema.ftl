-- ${ctx.table.comment}
CREATE TABLE ${ctx.table.tableName} (
<#list ctx.table.columns as c>
  ${c.columnName} ${c.dbType} COMMENT '${c.comment}'<#if c_has_next>,</#if>
</#list>
);
