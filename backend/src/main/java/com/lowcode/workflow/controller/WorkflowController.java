package com.lowcode.workflow.controller;

import com.lowcode.platform.api.ApiResponse;
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
    public ApiResponse<String> deploy(@RequestBody WorkflowDefinition def) {
        runtimeService.deploy(def);
        return ApiResponse.ok("OK");
    }

    @PostMapping("/start")
    public ApiResponse<WorkflowInstance> start(@RequestParam String processCode,
                                  @RequestParam Integer version,
                                  @RequestParam String businessKey,
                                  @RequestParam String userId,
                                  @RequestBody Map<String, Object> vars) {
        return ApiResponse.ok(runtimeService.start(processCode, version, businessKey, userId, vars));
    }

    @PostMapping("/approve")
    public ApiResponse<String> approve(@RequestBody ApprovalAction action) { runtimeService.approve(action); return ApiResponse.ok("OK"); }

    @PostMapping("/reject")
    public ApiResponse<String> reject(@RequestBody ApprovalAction action) { runtimeService.reject(action); return ApiResponse.ok("OK"); }

    @PostMapping("/recall/{instanceId}")
    public ApiResponse<String> recall(@PathVariable String instanceId, @RequestParam String userId) { runtimeService.recall(instanceId, userId); return ApiResponse.ok("OK"); }

    @GetMapping("/logs")
    public ApiResponse<List<String>> logs() { return ApiResponse.ok(runtimeService.logs()); }
}
