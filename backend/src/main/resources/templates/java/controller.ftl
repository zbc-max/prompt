package ${ctx.project.basePackage}.${ctx.project.moduleName}.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${ctx.table.businessName}")
public class ${ctx.table.className}Controller {

    @GetMapping("/list")
    public String list() { return "TODO list"; }

    @PostMapping
    public String create() { return "TODO create"; }

    @PutMapping
    public String update() { return "TODO update"; }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) { return "TODO delete"; }
}
