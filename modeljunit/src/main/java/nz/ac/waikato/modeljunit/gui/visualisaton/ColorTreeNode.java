package nz.ac.waikato.modeljunit.gui.visualisaton;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *  ColorTreeNode extends DefaultMutableTreeNode.
 *  Tree nodes are rendered into different colors depends on whether
 *  the nodes are in the current sequence or not.
 *  
 *  @author Celia Lai
 */
public class ColorTreeNode extends DefaultMutableTreeNode {

  /** True if the selected node is in the current sequence */
  private boolean isInCurrentSeq;
  
  public ColorTreeNode(Object obj) {
    super(obj);
  }
  
  public ColorTreeNode(Object obj, boolean allowsChildren) {
    super(obj, allowsChildren);
  }

  public boolean isInCurrentSeq() {
    return isInCurrentSeq;
  }

  public void setInCurrentSeq(boolean isInCurrentSeq) {
    this.isInCurrentSeq = isInCurrentSeq;
  }
}
