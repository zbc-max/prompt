package com.lowcode.workflow.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class WorkflowModels {
    @Data
    public static class WorkflowDefinition {
        private String processCode;
        private String processName;
        private Integer version;
        private List<NodeDefinition> nodes;
        private List<TransitionDefinition> transitions;
    }

    @Data
    public static class NodeDefinition {
        private String nodeId;
        private String nodeName;
        private NodeType nodeType;
        private String approverType; // ROLE/USER/EXPR
        private String approverValue;
        private String signType; // SINGLE/AND/OR
        private String rejectToNodeId;
    }

    @Data
    public static class TransitionDefinition {
        private String fromNodeId;
        private String toNodeId;
        private String conditionExpr;
        private Integer priority;
    }

    public enum NodeType { START, APPROVAL, END }

    public enum InstanceStatus { DRAFT, RUNNING, APPROVED, REJECTED, CANCELED }

    public enum TaskStatus { PENDING, APPROVED, REJECTED, CANCELED }

    @Data
    public static class WorkflowInstance {
        private String instanceId;
        private String processCode;
        private Integer processVersion;
        private String businessKey;
        private Map<String, Object> variables;
        private InstanceStatus status;
        private String currentNodeId;
        private String startUserId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Data
    public static class WorkflowTask {
        private String taskId;
        private String instanceId;
        private String nodeId;
        private String approverId;
        private TaskStatus status;
        private LocalDateTime createTime;
        private LocalDateTime actionTime;
        private String comment;
    }

    @Data
    public static class ApprovalAction {
        private String instanceId;
        private String nodeId;
        private String approverId;
        private String action; // APPROVE/REJECT/RECALL
        private String comment;
    }
}
