package tfc.flame.asm;

import org.objectweb.asm.tree.ClassNode;

public abstract class ClassTransformer {
    /**
     * @param node the class node to process
     * @return null if the transformer doesn't accept the class, or the transformed node if it does
     */
    public abstract ClassNode accept(ClassNode node);
}
