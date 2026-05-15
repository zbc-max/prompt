package com.lowcode.workflow.engine;

import com.lowcode.workflow.model.WorkflowModels.NodeDefinition;
import com.lowcode.workflow.model.WorkflowModels.WorkflowTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NodeEngine {

    public List<WorkflowTask> buildApprovalTasks(String instanceId, NodeDefinition node, List<String> approvers) {
        List<WorkflowTask> tasks = new ArrayList<>();
        for (String approver : approvers) {
            WorkflowTask task = new WorkflowTask();
            task.setTaskId("TASK_" + System.nanoTime());
            task.setInstanceId(instanceId);
            task.setNodeId(node.getNodeId());
            task.setApproverId(approver);
            task.setStatus(com.lowcode.workflow.model.WorkflowModels.TaskStatus.PENDING);
            task.setCreateTime(LocalDateTime.now());
            tasks.add(task);
        }
        return tasks;
    }

    public boolean canNodePass(String signType, List<WorkflowTask> tasks) {
        long approved = tasks.stream().filter(t -> t.getStatus().name().equals("APPROVED")).count();
        long rejected = tasks.stream().filter(t -> t.getStatus().name().equals("REJECTED")).count();
        if ("AND".equals(signType)) return approved == tasks.size();
        if ("OR".equals(signType)) return approved > 0;
        return approved == 1 && rejected == 0;
    }
}
