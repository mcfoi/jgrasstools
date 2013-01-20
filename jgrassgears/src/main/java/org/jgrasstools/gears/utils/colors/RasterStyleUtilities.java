/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.gears.utils.colors;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.utils.SldUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gears.utils.math.NumericsUtilities;
import org.opengis.filter.expression.Expression;

/**
 * A class to help with raster styling. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RasterStyleUtilities {

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    public static void dumpRasterStyle( String path, double min, double max, double[] values, Color[] colors, double opacity )
            throws Exception {
        String styleStr = createRasterStyleString(min, max, values, colors, opacity);
        FileUtilities.writeFile(styleStr, new File(path));
    }

    private static String createRasterStyleString( double min, double max, double[] values, Color[] colors, double opacity )
            throws Exception {
        Style newStyle = createRasterStyle(min, max, values, colors, opacity);
        String styleStr = SldUtilities.styleToString(newStyle);
        return styleStr;
    }

    private static Style createRasterStyle( double min, double max, double[] values, Color[] colors, double opacity ) {
        StyleBuilder sB = new StyleBuilder(sf);
        RasterSymbolizer rasterSym = sf.createRasterSymbolizer();

        int colorsNum = colors.length;
        boolean hasAllValues = false;
        if (values != null) {
            // we take first and last and interpolate in the middle
            hasAllValues = true;
        }
        double interval = (max - min) / (colorsNum - 1);
        double runningValue = min;

        ColorMap colorMap = sf.createColorMap();

        for( int i = 0; i < colors.length - 1; i++ ) {
            Color fromColor = colors[i];
            Color toColor = colors[i + 1];

            double start;
            double end;
            if (hasAllValues) {
                start = values[i];
                end = values[i + 1];
            } else {
                start = runningValue;
                runningValue = runningValue + interval;
                end = runningValue;
            }

            Expression opacityExpr = sB.literalExpression(opacity);

            if (i == 0) {
                Expression fromColorExpr = sB.colorExpression(fromColor);
                Expression fromExpr = sB.literalExpression(start);
                ColorMapEntry entry = sf.createColorMapEntry();
                entry.setQuantity(fromExpr);
                entry.setColor(fromColorExpr);
                entry.setOpacity(opacityExpr);
                colorMap.addColorMapEntry(entry);
            }

            if (!NumericsUtilities.dEq(start, end)) {
                Expression toColorExpr = sB.colorExpression(toColor);
                Expression toExpr = sB.literalExpression(end);
                ColorMapEntry entry = sf.createColorMapEntry();
                entry.setQuantity(toExpr);
                entry.setOpacity(opacityExpr);
                entry.setColor(toColorExpr);
                colorMap.addColorMapEntry(entry);
            }
            // i++;
        }

        rasterSym.setColorMap(colorMap);

        /*
         * set global transparency for the map
         */
        rasterSym.setOpacity(sB.literalExpression(opacity));

        Style newStyle = SLD.wrapSymbolizers(rasterSym);
        return newStyle;
    }

    public static String createStyleForColortable( String colorTableName, double min, double max, double[] values, double opacity )
            throws Exception {

        List<Color> colorList = new ArrayList<Color>();
        String tableString = new DefaultTables().getTableString(colorTableName);
        String[] split = tableString.split("\n");
        List<Double> newValues = null; // if necessary
        for( String line : split ) {
            if (line.startsWith("#")) { //$NON-NLS-1$
                continue;
            }
            String[] lineSplit = line.trim().split("\\s+"); //$NON-NLS-1$

            if (lineSplit.length == 3) {
                int r = Integer.parseInt(lineSplit[0]);
                int g = Integer.parseInt(lineSplit[1]);
                int b = Integer.parseInt(lineSplit[2]);

                colorList.add(new Color(r, g, b));
            } else if (lineSplit.length == 8) {
                if (newValues == null) {
                    newValues = new ArrayList<Double>();
                }

                // also value are provided, rewrite input values
                double v1 = Double.parseDouble(lineSplit[0]);
                int r1 = Integer.parseInt(lineSplit[1]);
                int g1 = Integer.parseInt(lineSplit[2]);
                int b1 = Integer.parseInt(lineSplit[3]);

                colorList.add(new Color(r1, g1, b1));
                newValues.add(v1);

                double v2 = Double.parseDouble(lineSplit[4]);
                int r2 = Integer.parseInt(lineSplit[5]);
                int g2 = Integer.parseInt(lineSplit[6]);
                int b2 = Integer.parseInt(lineSplit[7]);

                colorList.add(new Color(r2, g2, b2));
                newValues.add(v2);
            } else if (lineSplit.length == 4) {
                if (newValues == null) {
                    newValues = new ArrayList<Double>();
                }

                // also value are provided, rewrite input values
                double v1 = Double.parseDouble(lineSplit[0]);
                int r1 = Integer.parseInt(lineSplit[1]);
                int g1 = Integer.parseInt(lineSplit[2]);
                int b1 = Integer.parseInt(lineSplit[3]);

                colorList.add(new Color(r1, g1, b1));
                newValues.add(v1);

            }
        }

        Color[] colorsArray = colorList.toArray(new Color[0]);
        if (newValues != null) {
            // redefine values
            values = new double[newValues.size()];
            for( int i = 0; i < newValues.size(); i++ ) {
                values[i] = newValues.get(i);
            }
        }

        return createRasterStyleString(min, max, values, colorsArray, opacity);
    }

    public static void main( String[] args ) throws Exception {
        double[] values = {0, 360};
        // String createStyleForColortable = createStyleForColortable("aspect", 0.0, 360.0, null,
        // 0.5);
        // System.out.println(createStyleForColortable);
         String createStyleForColortable = createStyleForColortable(ColorTables.extrainbow.name(),
         73.835, 144.889, null, 0.8);
         System.out.println(createStyleForColortable);
        // String createStyleForColortable = createStyleForColortable(DefaultTables.SLOPE, 0.0,
        // 0.9656, null, 1.0);
        // System.out.println(createStyleForColortable);
//        String createStyleForColortable = createStyleForColortable("flow", 0, 0, null, 1);
//        System.out.println(createStyleForColortable);
    }
}
