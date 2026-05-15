package com.lowcode.generator.model;

import lombok.Data;

import java.util.List;

@Data
public class GenModels {
    @Data
    public static class GenProject {
        private String basePackage;
        private String moduleName;
        private String author;
    }

    @Data
    public static class GenColumn {
        private String columnName;
        private String javaField;
        private String dbType;
        private String javaType;
        private String comment;
        private Boolean pk;
        private Boolean required;
    }

    @Data
    public static class GenTable {
        private String tableName;
        private String className;
        private String businessName;
        private String comment;
        private String sceneType; // SINGLE/MASTER_DETAIL/TREE
        private List<GenColumn> columns;
        private GenTable subTable;
    }

    @Data
    public static class GenContext {
        private GenProject project;
        private GenTable table;
    }
}
