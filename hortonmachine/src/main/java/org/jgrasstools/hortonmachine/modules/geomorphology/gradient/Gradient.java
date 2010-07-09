/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jgrasstools.hortonmachine.modules.geomorphology.gradient;

import static org.jgrasstools.gears.libs.modules.JGTConstants.doubleNovalue;
import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.License;
import oms3.annotations.Out;
import oms3.annotations.Status;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.libs.monitor.DummyProgressMonitor;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.hortonmachine.i18n.HortonMessageHandler;
/**
 * <p>
 * The openmi compliant representation of the gradient model. Calculates the
 * gradient in each point of the map,
 * </p>
 * <p>
 * It estimate the gradient with several formula:
 * 
 * <pre>
*  <li>finite difference defaultMode=0;
*  <li> Horn defaultMode=1;
*  <li> Evans defaultMode=2;
 * </pre>
 * 
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the matrix of elevations (-pit);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>matrix of the gradients (-gradient);</LI>
 * </OL>
 * <P></DD> Usage: h.gradient --igrass-pit pit --ograss-gradient gradient
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
@Description("Calculates the gradient in each point of the map.")
@Author(name = "Daniele Andreis, Antonello Andrea, Erica Ghesla, Cozzini Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo", contact = "http://www.neng.usu.edu/cee/faculty/dtarb/tardem.html#programs, www.hydrologis.com")
@Keywords("Geomorphology")
@Status(Status.TESTED)
@License("http://www.gnu.org/licenses/gpl-3.0.html")
public class Gradient extends JGTModel {
    /*
     * EXTERNAL VARIABLES
     */
    // input
    @Description("The digital elevation model (DEM).")
    @In
    public GridCoverage2D inDem = null;

    @Description("The progress monitor.")
    @In
    public IJGTProgressMonitor pm = new DummyProgressMonitor();

    @Description("The map of gradient.")
    @Out
    public GridCoverage2D outSlope = null;

    @Description("Select the formula.")
    @Out
    public int defaultMode = 0;

    /*
     * INTERNAL VARIABLES
     */
    private HortonMessageHandler msg = HortonMessageHandler.getInstance();

    private int nCols;

    private Double xRes;

    private int nRows;

    private Double yRes;

    @Execute
    public void process() {
        if (!concatOr(outSlope == null, doReset)) {
            return;
        }

        HashMap<String, Double> regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inDem);
        nCols = regionMap.get(CoverageUtilities.COLS).intValue();
        nRows = regionMap.get(CoverageUtilities.ROWS).intValue();
        xRes = regionMap.get(CoverageUtilities.XRES);
        yRes = regionMap.get(CoverageUtilities.YRES);

        RenderedImage elevationRI = inDem.getRenderedImage();
        RandomIter elevationIter = RandomIterFactory.create(elevationRI, null);
        WritableRaster gradientWR = null;
        if (defaultMode == 1) {
            gradientWR = gradientHorn(elevationIter);
        } else if (defaultMode == 2) {
            gradientWR = gradientEvans(elevationIter);
        } else {

            gradientWR = gradientDiff(elevationIter);

        }
        outSlope = CoverageUtilities.buildCoverage("gradient", gradientWR, regionMap, inDem.getCoordinateReferenceSystem());
    }

    /**
    * Computes the gradient algorithm. p=f_{x}^{2}+f_{y}^{2}
    *  
    * The derivatives can be calculate with the  the horn formula:
    * 
    * f<sub>x</sub>=(2*f(x+1,y)+f(x+1,y-1)+f(x+1,y+1)-2*f(x-1,y)-f(x-1,y+1)-f(x-1,y-1))/(8 &#916 x) 
    * f<sub>y</sub>=(2*f(x,y+1)+f(x+1,y+1)+f(x-1,y+1)-2*f(x,y-1)-f(x+1,y-1)+f(x-1,y-1))/(8 &#916 y)
    * <p>
    * The kernel is compound of 9 cell (8 around the central pixel) and the numeration is:
    * 
    * 1   2   3
    * 4   5   6
    * 7   8   9
    * 
    * This numeration is used to extract the appropriate elevation value (es elev1 an so on)
    * 
    * </p>
    *
    *
    *
    */

    private WritableRaster gradientHorn( RandomIter elevationIter ) {

        WritableRaster gradientWR = CoverageUtilities.createDoubleWritableRaster(nCols, nRows, null, null, doubleNovalue);

        pm.beginTask(msg.message("gradient.working"), nRows);
        for( int y = 1; y < nRows - 1; y++ ) {
            if (isCanceled(pm)) {
                return null;
            }
            for( int x = 1; x < nCols - 1; x++ ) {
                // extract the value to use for the algoritm. It is the finite difference approach.
                double elev5 = elevationIter.getSampleDouble(x, y, 0);
                double elev4 = elevationIter.getSampleDouble(x - 1, y, 0);
                double elev6 = elevationIter.getSampleDouble(x + 1, y, 0);
                double elev2 = elevationIter.getSampleDouble(x, y - 1, 0);
                double elev8 = elevationIter.getSampleDouble(x, y + 1, 0);
                double elev9 = elevationIter.getSampleDouble(x + 1, y + 1, 0);
                double elev1 = elevationIter.getSampleDouble(x - 1, y - 1, 0);
                double elev3 = elevationIter.getSampleDouble(x + 1, y - 1, 0);
                double elev7 = elevationIter.getSampleDouble(x - 1, y + 1, 0);

                if (isNovalue(elev5) || isNovalue(elev1) || isNovalue(elev2) || isNovalue(elev3) || isNovalue(elev4)
                        || isNovalue(elev6) || isNovalue(elev7) || isNovalue(elev8) || isNovalue(elev9)) {
                    gradientWR.setSample(x, y, 0, doubleNovalue);
                } else {
                    double fu = 2 * elev6 + elev9 + elev3;
                    double fd = 2 * elev4 + elev7 + elev1;
                    double xGrad = (fu - fd) / (8 * xRes);
                    fu = 2 * elev8 + elev7 + elev9;
                    fd = 2 * elev2 + elev1 + elev3;
                    double yGrad = (fu - fd) / (8 * yRes);
                    double grad = Math.sqrt(Math.pow(xGrad, 2) + Math.pow(yGrad, 2));
                    gradientWR.setSample(x, y, 0, grad);
                }
            }
            pm.worked(1);
        }
        pm.done();

        return gradientWR;
    }

    /**
     * Estimate the gradient (p=f_{x}^{2}+f_{y}^{2}) with a finite difference formula:
     * 
     * <pre>
     *  p=&radic{f<sub>x</sub>&sup2;+f<sub>y</sub>&sup2;}
     * f<sub>x</sub>=(f(x+1,y)-f(x-1,y))/(2 &#916 x) 
     * f<sub>y</sub>=(f(x,y+1)-f(x,y-1))/(2 &#916 y)
     * </pre>
     * 
    */
    private WritableRaster gradientDiff( RandomIter elevationIter ) {

        WritableRaster gradientWR = CoverageUtilities.createDoubleWritableRaster(nCols, nRows, null, null, doubleNovalue);

        pm.beginTask(msg.message("gradient.working"), nRows);
        for( int y = 1; y < nRows - 1; y++ ) {

            for( int x = 1; x < nCols - 1; x++ ) {
                // extract the value to use for the algoritm. It is the finite difference approach.
                double elevIJ = elevationIter.getSampleDouble(x, y, 0);
                double elevIJipre = elevationIter.getSampleDouble(x - 1, y, 0);
                double elevIJipost = elevationIter.getSampleDouble(x + 1, y, 0);
                double elevIJjpre = elevationIter.getSampleDouble(x, y - 1, 0);
                double elevIJjpost = elevationIter.getSampleDouble(x, y + 1, 0);
                if (isNovalue(elevIJ) || isNovalue(elevIJipre) || isNovalue(elevIJipost) || isNovalue(elevIJjpre)
                        || isNovalue(elevIJjpost)) {
                    gradientWR.setSample(x, y, 0, doubleNovalue);
                } else if (!isNovalue(elevIJ) && !isNovalue(elevIJipre) && !isNovalue(elevIJipost) && !isNovalue(elevIJjpre)
                        && !isNovalue(elevIJjpost)) {
                    double xGrad = 0.5 * (elevIJipost - elevIJipre) / xRes;
                    double yGrad = 0.5 * (elevIJjpre - elevIJjpost) / yRes;
                    double grad = Math.sqrt(Math.pow(xGrad, 2) + Math.pow(yGrad, 2));
                    gradientWR.setSample(x, y, 0, grad);
                } else {
                    throw new IllegalArgumentException("Error in gradient");
                }
            }
            pm.worked(1);
        }
        pm.done();

        return gradientWR;
    }

    /** estimate the gradient using the Horn formula.
     * <p>
     * Where the gradient is:
     * </p>
     * <pre>
     *  p=&radic{f<sub>x</sub>&sup2;+f<sub>y</sub>&sup2;}
     *  
     *  and the derivatives can be calculate with the  the Evans formula:
      
      * f<sub>x</sub>=(f(x+1,y)+f(x+1,y-1)+f(x+1,y+1)-f(x-1,y)-f(x-1,y+1)-f(x-1,y-1))/(6 &#916 x) 
      * f<sub>y</sub>=(f(x,y+1)+f(x+1,y+1)+f(x-1,y+1)-f(x,y-1)-f(x+1,y-1)+f(x-1,y-1))/(6 &#916 y)
     <p>
      * The kernel is compound of 9 cell (8 around the central pixel) and the numeration is:
      * 
      * 1   2   3
      * 4   5   6
      * 7   8   9
      * 
      * This enumeration is used to extract the appropriate elevation value (es elev1 an so on)
      * 
      * </p>
     *
     *
     *
     */
    private WritableRaster gradientEvans( RandomIter elevationIter ) {

        WritableRaster gradientWR = CoverageUtilities.createDoubleWritableRaster(nCols, nRows, null, null, doubleNovalue);

        pm.beginTask(msg.message("gradient.working"), nRows);
        for( int y = 1; y < nRows - 1; y++ ) {

            for( int x = 1; x < nCols - 1; x++ ) {

                // extract the value to use for the algoritm. It is the finite difference approach.
                double elev5 = elevationIter.getSampleDouble(x, y, 0);
                double elev4 = elevationIter.getSampleDouble(x - 1, y, 0);
                double elev6 = elevationIter.getSampleDouble(x + 1, y, 0);
                double elev2 = elevationIter.getSampleDouble(x, y - 1, 0);
                double elev8 = elevationIter.getSampleDouble(x, y + 1, 0);
                double elev9 = elevationIter.getSampleDouble(x + 1, y + 1, 0);
                double elev1 = elevationIter.getSampleDouble(x - 1, y - 1, 0);
                double elev3 = elevationIter.getSampleDouble(x + 1, y - 1, 0);
                double elev7 = elevationIter.getSampleDouble(x - 1, y + 1, 0);

                if (isNovalue(elev5) || isNovalue(elev1) || isNovalue(elev2) || isNovalue(elev3) || isNovalue(elev4)
                        || isNovalue(elev6) || isNovalue(elev7) || isNovalue(elev8) || isNovalue(elev9)) {
                    gradientWR.setSample(x, y, 0, doubleNovalue);
                } else {
                    double fu = elev6 + elev9 + elev3;
                    double fd = elev4 + elev7 + elev1;
                    double xGrad = (fu - fd) / (6 * xRes);
                    fu = elev8 + elev7 + elev9;
                    fd = elev2 + elev1 + elev3;

                    double yGrad = (fu - fd) / (6 * yRes);
                    double grad = Math.sqrt(Math.pow(xGrad, 2) + Math.pow(yGrad, 2));
                    gradientWR.setSample(x, y, 0, grad);
                }
            }

            pm.worked(1);
        }
        pm.done();

        return gradientWR;
    }

}

