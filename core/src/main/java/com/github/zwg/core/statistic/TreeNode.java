package com.github.zwg.core.statistic;

import java.util.List;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/8
 */
public class TreeNode {

    private TreeNode parent;

    private List<TreeNode> children;

    private Object data;

    private long startTime;

    private long endTime;

    public TreeNode(Object data) {
        this(null, data);
    }

    public TreeNode(TreeNode parent, Object data) {
        this.parent = parent;
        this.data = data;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public TreeNode markBegin() {
        this.startTime = System.currentTimeMillis();
        return this;
    }

    public TreeNode markEnd() {
        this.endTime = System.currentTimeMillis();
        return this;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
