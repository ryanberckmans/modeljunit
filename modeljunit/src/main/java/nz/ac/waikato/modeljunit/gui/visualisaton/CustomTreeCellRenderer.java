package nz.ac.waikato.modeljunit.gui.visualisaton;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import nz.ac.waikato.modeljunit.Transition;

/**
 * CustomTreeCellRenderer returns the tree cell renderer component that renders the tree node color.
 * 
 * @author Celia Lai
 */
public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {

        Component defaultRenderer = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                        hasFocus);

        if ((value != null) && (value instanceof ColorTreeNode)) {
            ColorTreeNode currentNode = (ColorTreeNode) value;
            Object userObject = currentNode.getUserObject();
            if (userObject instanceof Transition) {
                if (selected && currentNode.isInCurrentSeq()) {
                    // the selected node is in the current sequence
                    setForeground(ColorUtil.PICKED);
                    setBackgroundSelectionColor(null);
                    currentNode.setInCurrentSeq(false);
                } else {
                    if (currentNode.isInCurrentSeq()) {
                        // the node is not selected, but in the current sequence
                        setForeground(ColorUtil.EXPLORED_CURRENT_SEQ);
                        currentNode.setInCurrentSeq(false);
                    } else {
                        // the node is in other sequence
                        setForeground(ColorUtil.EXPLORED);
                    }
                }
                updateUI();
            }
        }

        return defaultRenderer;
    }
}
