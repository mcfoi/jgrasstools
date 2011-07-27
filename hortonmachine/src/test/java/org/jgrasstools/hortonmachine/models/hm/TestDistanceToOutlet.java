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
package org.jgrasstools.hortonmachine.models.hm;

import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.hortonmachine.modules.network.distancetooutlet.DistanceToOutlet;
import org.jgrasstools.hortonmachine.utils.HMTestCase;
import org.jgrasstools.hortonmachine.utils.HMTestMaps;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class TestDistanceToOutlet extends HMTestCase {

    /**
     * test {@link DistanceToOutlet} in the topological mode.
     * 
     */
    public void testDistanceToOutletTopological() {

        HashMap<String, Double> envelopeParams = HMTestMaps.envelopeParams;
        CoordinateReferenceSystem crs = HMTestMaps.crs;
        double[][] flowData = HMTestMaps.mflowDataBorder;
        GridCoverage2D flowCoverage = CoverageUtilities.buildCoverage("flow", flowData, envelopeParams, crs, true);

        DistanceToOutlet distanceToOutlet = new DistanceToOutlet();
        distanceToOutlet.inFlow = flowCoverage;
        distanceToOutlet.pMode = 1;
        distanceToOutlet.process();
        GridCoverage2D distanceCoverage = distanceToOutlet.outDistance;
        checkMatrixEqual(distanceCoverage.getRenderedImage(), HMTestMaps.d2oPixelData);
    }

    /**
     * test {@link DistanceToOutlet} in the simple mode.
     * 
     */
    public void testDistanceToOutletMetere() {

        HashMap<String, Double> envelopeParams = HMTestMaps.envelopeParams;
        CoordinateReferenceSystem crs = HMTestMaps.crs;
        double[][] flowData = HMTestMaps.mflowDataBorder;
        GridCoverage2D flowCoverage = CoverageUtilities.buildCoverage("flow", flowData, envelopeParams, crs, true);

        DistanceToOutlet distanceToOutlet = new DistanceToOutlet();
        distanceToOutlet.inFlow = flowCoverage;
        distanceToOutlet.pMode = 0;
        distanceToOutlet.process();
        GridCoverage2D distanceCoverage = distanceToOutlet.outDistance;
        checkMatrixEqual(distanceCoverage.getRenderedImage(), HMTestMaps.d2oMeterData, 0.01);
    }
    
    /**
     * test {@link DistanceToOutlet} in 3d.
     * 
     */
    public void testDistanceToOutlet3D() throws Exception {
        HashMap<String, Double> envelopeParams = HMTestMaps.envelopeParams;
        CoordinateReferenceSystem crs = HMTestMaps.crs;
        //get the flow direction map.
        double[][] flowData = HMTestMaps.mflowDataBorder;
        GridCoverage2D flowCoverage = CoverageUtilities.buildCoverage("flow", flowData, envelopeParams, crs, true);
        //get the pit map.
        double[][] pitData = HMTestMaps.pitData;
        GridCoverage2D pitCoverage = CoverageUtilities.buildCoverage("pit", pitData, envelopeParams, crs, true);
        DistanceToOutlet distanceToOutlet = new DistanceToOutlet();
        //set the needed input. 
        distanceToOutlet.pMode = 0;
        distanceToOutlet.inFlow = flowCoverage;
        distanceToOutlet.inPit = pitCoverage;
        distanceToOutlet.process();
        GridCoverage2D distanceCoverage = distanceToOutlet.outDistance;
        checkMatrixEqual(distanceCoverage.getRenderedImage(), HMTestMaps.d2o3dData);
    }
    
}
