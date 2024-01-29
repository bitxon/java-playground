package bitxon.model.structure.tree;

import lombok.Builder;

@Builder
public record Node(
    Node left,
    Node right,
    int data
) {}
