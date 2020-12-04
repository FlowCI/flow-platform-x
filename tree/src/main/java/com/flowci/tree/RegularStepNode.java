package com.flowci.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public final class RegularStepNode extends ParentNode implements StepNode {

    public static final boolean ALLOW_FAILURE_DEFAULT = false;

    /**
     * bash script
     */
    private String bash;

    /**
     * powershell script
     */
    private String pwsh;

    /**
     * Plugin name
     */
    private String plugin;

    /**
     * Step timeout in seconds
     */
    private Integer timeout;

    /**
     * Num of retry times
     */
    private Integer retry; // num of retry

    /**
     * Env vars export to job context
     */
    private Set<String> exports = new HashSet<>(0);

    /**
     * Is allow failure
     */
    private boolean allowFailure = ALLOW_FAILURE_DEFAULT;

    /**
     * Cache setting
     */
    private Cache cache;

    public RegularStepNode(String name, Node parent) {
        super(name, parent);
    }

    @JsonIgnore
    public boolean hasPlugin() {
        return !Strings.isNullOrEmpty(plugin);
    }

    @JsonIgnore
    public boolean hasCondition() {
        return !Strings.isNullOrEmpty(condition);
    }

    @JsonIgnore
    public boolean hasScript() {
        return !Strings.isNullOrEmpty(bash) || !Strings.isNullOrEmpty(pwsh);
    }

    @JsonIgnore
    public boolean isRootStep() {
        return parent instanceof FlowNode;
    }

    @JsonIgnore
    public boolean hasTimeout() {
        return timeout != null;
    }

    @JsonIgnore
    public boolean hasRetry() {
        return retry != null;
    }

    @JsonIgnore
    public boolean hasCache() {
        return cache != null;
    }
}
