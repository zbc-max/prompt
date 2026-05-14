package com.lowcode.workflow.controller;

import com.lowcode.workflow.model.WorkflowModels.*;
import com.lowcode.workflow.service.WorkflowRuntimeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {
    private final WorkflowRuntimeService runtimeService;

    public WorkflowController(WorkflowRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostMapping("/deploy")
    public String deploy(@RequestBody WorkflowDefinition def) {
        runtimeService.deploy(def);
        return "OK";
    }

    @PostMapping("/start")
    public WorkflowInstance start(@RequestParam String processCode,
                                  @RequestParam Integer version,
                                  @RequestParam String businessKey,
                                  @RequestParam String userId,
                                  @RequestBody Map<String, Object> vars) {
        return runtimeService.start(processCode, version, businessKey, userId, vars);
    }

    @PostMapping("/approve")
    public String approve(@RequestBody ApprovalAction action) { runtimeService.approve(action); return "OK"; }

    @PostMapping("/reject")
    public String reject(@RequestBody ApprovalAction action) { runtimeService.reject(action); return "OK"; }

    @PostMapping("/recall/{instanceId}")
    public String recall(@PathVariable String instanceId, @RequestParam String userId) { runtimeService.recall(instanceId, userId); return "OK"; }

    @GetMapping("/logs")
    public List<String> logs() { return runtimeService.logs(); }
}
