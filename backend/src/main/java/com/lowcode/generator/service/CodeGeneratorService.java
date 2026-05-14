package com.lowcode.generator.service;

import com.lowcode.generator.core.TemplateEngineService;
import com.lowcode.generator.model.GenModels.GenContext;
import com.lowcode.generator.util.ZipUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodeGeneratorService {

    private final TemplateEngineService templateEngineService;

    public CodeGeneratorService(TemplateEngineService templateEngineService) {
        this.templateEngineService = templateEngineService;
    }

    public Map<String, String> generatePreview(GenContext context) {
        Map<String, Object> model = new HashMap<>();
        model.put("ctx", context);

        Map<String, String> files = new HashMap<>();
        files.put("backend/" + context.getTable().getClassName() + "Controller.java", templateEngineService.render("java/controller.ftl", model));
        files.put("backend/" + context.getTable().getClassName() + "Service.java", templateEngineService.render("java/service.ftl", model));
        files.put("backend/" + context.getTable().getClassName() + "Mapper.java", templateEngineService.render("java/mapper.ftl", model));
        files.put("frontend/" + context.getTable().getBusinessName() + "/index.vue", templateEngineService.render("vue/index.ftl", model));
        files.put("sql/" + context.getTable().getTableName() + ".sql", templateEngineService.render("sql/schema.ftl", model));

        if ("MASTER_DETAIL".equals(context.getTable().getSceneType())) {
            files.put("frontend/" + context.getTable().getBusinessName() + "/detail.vue", templateEngineService.render("vue/detail.ftl", model));
        }
        if ("TREE".equals(context.getTable().getSceneType())) {
            files.put("frontend/" + context.getTable().getBusinessName() + "/tree.vue", templateEngineService.render("vue/tree.ftl", model));
        }
        return files;
    }

    public byte[] generateZip(GenContext context) {
        return ZipUtil.zip(generatePreview(context));
    }
}
