//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.tildejustin.accesswidenerloader;


import net.fabricmc.loader.impl.lib.accesswidener.*;
import net.fabricmc.mappingio.tree.*;

public final class AccessWidenerTinyRemapper implements AccessWidenerVisitor {
    private final AccessWidenerVisitor delegate;
    private final int fromNamespaceId;
    private final int toNamespaceId;
    private final MappingTree tree;

    public AccessWidenerTinyRemapper(AccessWidenerVisitor delegate, MappingTree tree, String fromNamespace, String toNamespace) {
        this.delegate = delegate;
        this.fromNamespaceId = tree.getNamespaceId(fromNamespace);
        this.toNamespaceId = tree.getNamespaceId(toNamespace);
        this.tree = tree;
        if (fromNamespaceId == MappingTreeView.NULL_NAMESPACE_ID || toNamespaceId == MappingTreeView.NULL_NAMESPACE_ID) {
            throw new IllegalStateException("mappings do not contain necessary namespace, cannot remap accesswidener (you likely need to use merged mappings)");
        }
    }

    public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitClass(tree.mapClassName(name, fromNamespaceId, toNamespaceId), access, transitive);
    }

    public void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        MappingTree.MethodMapping method = tree.getMethod(owner, name, descriptor, fromNamespaceId);
        if (method != null)
            delegate.visitMethod(tree.mapClassName(owner, fromNamespaceId, toNamespaceId), method.getName(toNamespaceId), method.getDesc(toNamespaceId), access, transitive);
    }

    public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        MappingTree.FieldMapping field = tree.getField(owner, name, descriptor, fromNamespaceId);
        if (field != null)
            delegate.visitField(tree.mapClassName(owner, fromNamespaceId, toNamespaceId), field.getName(toNamespaceId), field.getDesc(toNamespaceId), access, transitive);
    }

    public void visitHeader(String namespace) {
        if (fromNamespaceId != tree.getNamespaceId(namespace)) {
            throw new IllegalArgumentException("Cannot remap access widener from namespace '" + namespace + "'. Expected: '" + fromNamespaceId + "'");
        } else {
            delegate.visitHeader(tree.getNamespaceName(toNamespaceId));
        }
    }
}
