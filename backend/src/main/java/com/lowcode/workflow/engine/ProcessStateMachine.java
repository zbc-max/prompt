package com.lowcode.workflow.engine;

import com.lowcode.workflow.model.WorkflowModels.InstanceStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class ProcessStateMachine {
    private static final Map<InstanceStatus, Set<InstanceStatus>> TRANSITIONS = new EnumMap<>(InstanceStatus.class);

    static {
        TRANSITIONS.put(InstanceStatus.DRAFT, Set.of(InstanceStatus.RUNNING, InstanceStatus.CANCELED));
        TRANSITIONS.put(InstanceStatus.RUNNING, Set.of(InstanceStatus.APPROVED, InstanceStatus.REJECTED, InstanceStatus.CANCELED));
        TRANSITIONS.put(InstanceStatus.APPROVED, Set.of());
        TRANSITIONS.put(InstanceStatus.REJECTED, Set.of(InstanceStatus.RUNNING, InstanceStatus.CANCELED));
        TRANSITIONS.put(InstanceStatus.CANCELED, Set.of());
    }

    public static void assertTransition(InstanceStatus from, InstanceStatus to) {
        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalStateException("非法状态迁移: " + from + " -> " + to);
        }
    }
}
