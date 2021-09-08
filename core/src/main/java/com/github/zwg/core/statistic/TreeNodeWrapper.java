package com.github.zwg.core.statistic;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.repeat;

import java.io.StringReader;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/8
 */
public class TreeNodeWrapper {

    private static final String STEP_FIRST_CHAR = "`---";
    private static final String STEP_NORMAL_CHAR = "+---";
    private static final String STEP_HAS_BOARD = "|   ";
    private static final String STEP_EMPTY_BOARD = "    ";

    // 是否输出耗时
    private final boolean isPrintCost;

    // 根节点
    private final TreeNode root;

    // 当前节点
    private TreeNode current;

    public TreeNodeWrapper(boolean isPrintCost, String title) {
        this.root = new TreeNode(title).markBegin().markEnd();
        this.current = root;
        this.isPrintCost = isPrintCost;
    }

    public String render() {
        final StringBuilder treeSB = new StringBuilder();
        recursive(0, true, "", root, new Callback() {

            @Override
            public void callback(int deep, boolean isLast, String prefix, TreeNode node) {

                final boolean hasChild = !node.getChildren().isEmpty();
                final String stepString = isLast ? STEP_FIRST_CHAR : STEP_NORMAL_CHAR;
                final int stepStringLength = StringUtils.length(stepString);
                treeSB.append(prefix).append(stepString);

                int costPrefixLength = 0;
                if (hasChild) {
                    treeSB.append("+");
                }
                if (isPrintCost
                        && !node.isRoot()) {
                    final String costPrefix = String
                            .format("[%s,%sms]", (node.getStartTime() - root.getEndTime()),
                                    (node.getEndTime() - node.getStartTime()));
                    costPrefixLength = StringUtils.length(costPrefix);
                    treeSB.append(costPrefix);
                }

                final Scanner scanner = new Scanner(new StringReader(node.getData().toString()));
                try {
                    boolean isFirst = true;
                    while (scanner.hasNextLine()) {
                        if (isFirst) {
                            treeSB.append(scanner.nextLine()).append("\n");
                            isFirst = false;
                        } else {
                            treeSB
                                    .append(prefix)
                                    .append(repeat(' ', stepStringLength))
                                    .append(hasChild ? "|" : EMPTY)
                                    .append(repeat(' ', costPrefixLength))
                                    .append(scanner.nextLine())
                                    .append("\n");
                        }
                    }
                } finally {
                    scanner.close();
                }

            }

        });

        return treeSB.toString();
    }

    public boolean isTop() {
        return current.isRoot();
    }

    public TreeNodeWrapper begin(Object data) {
        current = new TreeNode(current, data);
        current.markBegin();
        return this;
    }

    public TreeNodeWrapper begin() {
        return begin(null);
    }

    public TreeNodeWrapper end() {
        if (current.isRoot()) {
            throw new IllegalStateException("current node is root.");
        }
        current.markEnd();
        current = current.getParent();
        return this;
    }

    public Object get() {
        if (current.isRoot()) {
            throw new IllegalStateException("current node is root.");
        }
        return current.getData();
    }

    public TreeNodeWrapper set(Object data) {
        if (current.isRoot()) {
            throw new IllegalStateException("current node is root.");
        }
        current.setData(data);
        return this;
    }

    private void recursive(int deep, boolean isLast, String prefix, TreeNode node,
            Callback callback) {
        callback.callback(deep, isLast, prefix, node);
        if (!node.isLeaf()) {
            final int size = node.getChildren().size();
            for (int index = 0; index < size; index++) {
                final boolean isLastFlag = index == size - 1;
                final String currentPrefix =
                        isLast ? prefix + STEP_EMPTY_BOARD : prefix + STEP_HAS_BOARD;
                recursive(
                        deep + 1,
                        isLastFlag,
                        currentPrefix,
                        node.getChildren().get(index),
                        callback
                );
            }
        }
    }

    private interface Callback {

        void callback(int deep, boolean isLast, String prefix, TreeNode node);

    }
}
