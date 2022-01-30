/*
 * P-logi-K. A Kotlin(TM)-based PLC simulation.
 * Copyright (C)  2022.  Markus Keppeler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.github.mkep_dev.p_logi_k.examples.garageDoor;

import org.github.mkep_dev.p_logi_k.model.io.BasicIOCard;
import org.github.mkep_dev.p_logi_k.program.fsm.FsmPLCProgram;
import org.github.mkep_dev.p_logi_k.simulation.SimulationEnvironment;
import org.github.mkep_dev.p_logi_k.starter.PLCMaster;

public class StartJava {

    public static void main(String[] args) {
        PLCMaster master = new PLCMaster(50, new BasicIOCard(0));
        FsmPLCProgram program = new GarageDoorProgram();
        master.getPlcProgramMemory().addProgram(program);
        SimulationEnvironment sim = new GarageDoorSimulation(1.0,
                master.getIoService(),
                master.getSimulationClockService(),
                master.getPlcExecutionService());
        sim.start(program.getName());
    }
}
