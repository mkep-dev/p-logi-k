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

package org.github.mkep_dev.p_logi_k.examples.garageDoor

import org.github.mkep_dev.p_logi_k.program.fsm.FsmPLCProgram

class GarageDoorProgram : FsmPLCProgram("GarageDoorFSM", GarageDoorEfsm.efsm, aliases) {

    companion object {
        val aliases = mapOf(
            GarageDoorEfsm.btn_up.name to "bIO0/in/1",
            GarageDoorEfsm.btn_down.name to "bIO0/in/2",
            GarageDoorEfsm.hit_bar.name to "bIO0/in/3",
            GarageDoorEfsm.end_up.name to "bIO0/in/4",
            GarageDoorEfsm.end_down.name to "bIO0/in/5",
            GarageDoorEfsm.motor_up.identifier to "bIO0/out/1",
            GarageDoorEfsm.motor_down.identifier to "bIO0/out/2",
        )
    }
}