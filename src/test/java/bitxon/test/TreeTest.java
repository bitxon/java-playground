package bitxon.test;

import bitxon.model.structure.tree.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class TreeTest {
    /*
                        1
                     /     \
                    2       3
                   / \     / \
                  4   5   6   7
                 /       / \
                8       9   10
     */
    static final Node TREE_ROOT = Node.builder().data(1)
        .left(Node.builder().data(2)
            .left(Node.builder().data(4)
                .left(Node.builder().data(8).build()).build())
            .right(Node.builder().data(5).build()).build())
        .right(Node.builder().data(3)
            .left(Node.builder().data(6)
                .left(Node.builder().data(9).build())
                .right(Node.builder().data(10).build()).build())
            .right(Node.builder().data(7).build()).build())
        .build();

    ArrayList<Integer> result = new ArrayList<>();

    //-----------------------------------------------------------------------------------------------------------------

    void preorderTravers(Node node) {
        if (node == null) {
            return;
        }
        result.add(node.data());
        preorderTravers(node.left());
        preorderTravers(node.right());
    }

    @Test
    @DisplayName("Deep First Search: Pre-Order")
    void preorderTravers() {
        preorderTravers(TREE_ROOT);
        assertThat(result).containsExactly(1, 2, 4, 8, 5, 3, 6, 9, 10, 7);
    }

    //-----------------------------------------------------------------------------------------------------------------

    void inorderTravers(Node node) {
        if (node == null) {
            return;
        }
        inorderTravers(node.left());
        result.add(node.data());
        inorderTravers(node.right());
    }

    @Test
    @DisplayName("Deep First Search: In-Order")
    void inorderTravers() {
        inorderTravers(TREE_ROOT);
        assertThat(result).containsExactly(8, 4, 2, 5, 1, 9, 6, 10, 3, 7);

    }

    //-----------------------------------------------------------------------------------------------------------------

    void postorderTravers(Node node) {
        if (node == null) {
            return;
        }
        postorderTravers(node.left());
        postorderTravers(node.right());
        result.add(node.data());
    }

    @Test
    @DisplayName("Deep First Search: Post-Order")
    void postorderTravers() {
        postorderTravers(TREE_ROOT);
        assertThat(result).containsExactly(8, 4, 5, 2, 9, 10, 6, 7, 3, 1);
    }

    //-----------------------------------------------------------------------------------------------------------------

    void levelTravers(Node root) {
        if (root == null) {
            return;
        }
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.poll();

            result.add(node.data());

            if (node.left() != null) {
                queue.add(node.left());
            }
            if (node.right() != null) {
                queue.add(node.right());
            }
        }
    }

    @Test
    @DisplayName("Breadth-First Search: Level Order")
    void levelTravers() {
        levelTravers(TREE_ROOT);
        assertThat(result).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }
}
