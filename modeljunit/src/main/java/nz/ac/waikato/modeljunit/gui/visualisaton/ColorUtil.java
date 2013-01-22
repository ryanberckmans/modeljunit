package nz.ac.waikato.modeljunit.gui.visualisaton;

import java.awt.Color;

/**
 * Utility class for defining colors
 * 
 * @author Celia Lai
 */
public class ColorUtil {
    /* unexplored vertex */
    public static Color UNEXPLORED = Color.GRAY;

    /* explored vertex */
    public static Color EXPLORED = Color.BLACK;

    /* explored vertex for picked test sequences */
    public static Color PICKED = Color.GREEN;

    /* explored vertex for current test sequences */
    public static Color EXPLORED_CURRENT_SEQ = new Color(51, 102, 0);

    /* failed edge */
    public static Color FAILED_EDGE = Color.RED;

    /* unexplored trans */
    public static Color DOTTED_LINE = new Color(140, 140, 200);

    /* initial state */
    public static Color FIRST_STATE = new Color(204, 153, 0);

    /* graph*/
    public static Color GRAPH = Color.magenta;
}
