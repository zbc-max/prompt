package com.lowcode.generator.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Service
public class TemplateEngineService {

    private final Configuration freemarkerCfg;

    public TemplateEngineService() throws IOException {
        this.freemarkerCfg = new Configuration(Configuration.VERSION_2_3_32);
        this.freemarkerCfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
        this.freemarkerCfg.setDefaultEncoding("UTF-8");
    }

    public String render(String templatePath, Map<String, Object> model) {
        try (StringWriter writer = new StringWriter()) {
            Template template = freemarkerCfg.getTemplate(templatePath);
            template.process(model, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("模板渲染失败: " + templatePath, e);
        }
    }
}
