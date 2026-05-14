package com.lowcode.workflow.service;

import com.lowcode.workflow.engine.ConditionEvaluator;
import com.lowcode.workflow.engine.NodeEngine;
import com.lowcode.workflow.engine.ProcessStateMachine;
import com.lowcode.workflow.model.WorkflowModels.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WorkflowRuntimeService {
    private final Map<String, WorkflowDefinition> defs = new ConcurrentHashMap<>();
    private final Map<String, WorkflowInstance> instances = new ConcurrentHashMap<>();
    private final Map<String, List<WorkflowTask>> taskStore = new ConcurrentHashMap<>();
    private final List<String> auditLogs = Collections.synchronizedList(new ArrayList<>());

    private final ConditionEvaluator evaluator = new ConditionEvaluator();
    private final NodeEngine nodeEngine = new NodeEngine();

    public void deploy(WorkflowDefinition definition) {
        defs.put(definition.getProcessCode() + ":" + definition.getVersion(), definition);
    }

    public WorkflowInstance start(String processCode, Integer version, String businessKey, String startUserId, Map<String, Object> vars) {
        WorkflowDefinition def = getDef(processCode, version);
        WorkflowInstance instance = new WorkflowInstance();
        instance.setInstanceId("INS_" + System.nanoTime());
        instance.setProcessCode(processCode);
        instance.setProcessVersion(version);
        instance.setBusinessKey(businessKey);
        instance.setVariables(new HashMap<>(vars));
        instance.setStatus(InstanceStatus.RUNNING);
        instance.setStartUserId(startUserId);
        instance.setStartTime(LocalDateTime.now());
        String startNodeId = def.getNodes().stream().filter(n -> n.getNodeType() == NodeType.START).findFirst().orElseThrow().getNodeId();
        instance.setCurrentNodeId(startNodeId);
        instances.put(instance.getInstanceId(), instance);

        moveNext(instance);
        return instance;
    }

    public void approve(ApprovalAction action) {
        WorkflowInstance instance = getInstance(action.getInstanceId());
        List<WorkflowTask> tasks = taskStore.getOrDefault(instance.getCurrentNodeId() + "@" + instance.getInstanceId(), new ArrayList<>());
        WorkflowTask task = tasks.stream().filter(t -> t.getApproverId().equals(action.getApproverId())).findFirst().orElseThrow();
        task.setStatus(TaskStatus.APPROVED);
        task.setActionTime(LocalDateTime.now());
        task.setComment(action.getComment());
        auditLogs.add("APPROVE:" + action.getInstanceId() + ":" + action.getApproverId());

        WorkflowDefinition def = getDef(instance.getProcessCode(), instance.getProcessVersion());
        NodeDefinition node = findNode(def, instance.getCurrentNodeId());
        if (nodeEngine.canNodePass(node.getSignType(), tasks)) moveNext(instance);
    }

    public void reject(ApprovalAction action) {
        WorkflowInstance instance = getInstance(action.getInstanceId());
        WorkflowDefinition def = getDef(instance.getProcessCode(), instance.getProcessVersion());
        NodeDefinition node = findNode(def, instance.getCurrentNodeId());
        instance.setCurrentNodeId(node.getRejectToNodeId());
        ProcessStateMachine.assertTransition(instance.getStatus(), InstanceStatus.REJECTED);
        instance.setStatus(InstanceStatus.REJECTED);
        auditLogs.add("REJECT:" + action.getInstanceId() + ":" + action.getApproverId());
    }

    public void recall(String instanceId, String userId) {
        WorkflowInstance instance = getInstance(instanceId);
        if (!userId.equals(instance.getStartUserId())) throw new IllegalStateException("仅发起人可撤回");
        ProcessStateMachine.assertTransition(instance.getStatus(), InstanceStatus.CANCELED);
        instance.setStatus(InstanceStatus.CANCELED);
        instance.setEndTime(LocalDateTime.now());
        auditLogs.add("RECALL:" + instanceId + ":" + userId);
    }

    public List<String> logs() { return auditLogs; }

    private void moveNext(WorkflowInstance instance) {
        WorkflowDefinition def = getDef(instance.getProcessCode(), instance.getProcessVersion());
        List<TransitionDefinition> candidates = def.getTransitions().stream()
                .filter(t -> t.getFromNodeId().equals(instance.getCurrentNodeId()))
                .sorted(Comparator.comparing(TransitionDefinition::getPriority))
                .collect(Collectors.toList());
        TransitionDefinition next = candidates.stream()
                .filter(t -> evaluator.evaluate(t.getConditionExpr(), instance.getVariables()))
                .findFirst().orElseThrow(() -> new IllegalStateException("未匹配到下一节点"));

        NodeDefinition node = findNode(def, next.getToNodeId());
        instance.setCurrentNodeId(node.getNodeId());
        if (node.getNodeType() == NodeType.END) {
            ProcessStateMachine.assertTransition(instance.getStatus(), InstanceStatus.APPROVED);
            instance.setStatus(InstanceStatus.APPROVED);
            instance.setEndTime(LocalDateTime.now());
            return;
        }
        List<String> approvers = Arrays.stream(node.getApproverValue().split(",")).map(String::trim).toList();
        List<WorkflowTask> tasks = nodeEngine.buildApprovalTasks(instance.getInstanceId(), node, approvers);
        taskStore.put(node.getNodeId() + "@" + instance.getInstanceId(), tasks);
    }

    private WorkflowDefinition getDef(String processCode, Integer version) {
        WorkflowDefinition def = defs.get(processCode + ":" + version);
        if (def == null) throw new IllegalStateException("流程定义不存在");
        return def;
    }

    private WorkflowInstance getInstance(String instanceId) {
        WorkflowInstance ins = instances.get(instanceId);
        if (ins == null) throw new IllegalStateException("流程实例不存在");
        return ins;
    }

    private NodeDefinition findNode(WorkflowDefinition def, String nodeId) {
        return def.getNodes().stream().filter(n -> n.getNodeId().equals(nodeId)).findFirst().orElseThrow();
    }
}
