/*
 * This file is part of the "CI-slam module": an addition to JGrassTools
 * It has been contributed by Marco Foi (www.mcfoi.it) and Cristiano Lanni
 * 
 * "CI-slam module" is free software: you can redistribute it and/or modify
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
package org.jgrasstools.modules;

import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_AUTHORCONTACTS;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_AUTHORNAMES;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_LICENSE;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_OMSPSIINITATBEDROCK_DESCRIPTION;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_OMSPSIINITATBEDROCK_KEYWORDS;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_OMSPSIINITATBEDROCK_NAME;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_OMSPSIINITATBEDROCK_STATUS;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_OMSPSIINITATBEDROCK_outPsiInitAtBedrock_DESCRIPTION;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_OMSPSIINITATBEDROCK_pPsiInitAtBedrockConstant_DESCRIPTION;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_SUBMODULES_LABEL;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_inPit_DESCRIPTION;
import static org.jgrasstools.hortonmachine.i18n.HortonMessages.OMSCISLAM_inSoilThickness_DESCRIPTION;
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.UI;
import oms3.annotations.Unit;

import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.hortonmachine.modules.hydrogeomorphology.cislam.utility_models.OmsPsiInitAtBedrock;

@Description(OMSCISLAM_OMSPSIINITATBEDROCK_DESCRIPTION)
@Author(name = OMSCISLAM_AUTHORNAMES, contact = OMSCISLAM_AUTHORCONTACTS)
@Keywords(OMSCISLAM_OMSPSIINITATBEDROCK_KEYWORDS)
@Label(OMSCISLAM_SUBMODULES_LABEL)
@Name(OMSCISLAM_OMSPSIINITATBEDROCK_NAME)
@Status(OMSCISLAM_OMSPSIINITATBEDROCK_STATUS)
@License(OMSCISLAM_LICENSE)
public class CislamPsiInitAtBedrock extends JGTModel {

    public final double DEFAULT_PSI_CONSTANT = 0.05;

    @Description(OMSCISLAM_inPit_DESCRIPTION)
    @Unit("m")
	@UI(JGTConstants.FILEIN_UI_HINT)
    @In
    public String inPit = null;

    @Description(OMSCISLAM_inSoilThickness_DESCRIPTION)
    @Unit("m")
	@UI(JGTConstants.FILEIN_UI_HINT)
    @In
    public String inSoilThickness = null;

    @Description(OMSCISLAM_OMSPSIINITATBEDROCK_pPsiInitAtBedrockConstant_DESCRIPTION)
    @Unit("m")
    @In
    public double pPsiInitAtBedrockConstant = 0.0;

    @Description(OMSCISLAM_OMSPSIINITATBEDROCK_outPsiInitAtBedrock_DESCRIPTION)
    @Unit("m")
    @Out
    public String outPsiInitAtBedrock = null;

    @Execute
    public void process() throws Exception {
    	OmsPsiInitAtBedrock omsPsiInitAtBedrock = new OmsPsiInitAtBedrock();
    	omsPsiInitAtBedrock.inPit = getRaster(inPit);
    	omsPsiInitAtBedrock.inSoilThickness = getRaster(inSoilThickness);
    	omsPsiInitAtBedrock.pPsiInitAtBedrockConstant = pPsiInitAtBedrockConstant;
    	omsPsiInitAtBedrock.inSoilThickness = getRaster(inSoilThickness);
    	omsPsiInitAtBedrock.process();
    	dumpRaster(omsPsiInitAtBedrock.outPsiInitAtBedrock, outPsiInitAtBedrock);        
    }
}
