/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.vm;

import gov.nasa.jpf.vm.Path.PathStorage;
import gov.nasa.jpf.vm.Transition.TransitionStorage;

/**
 * NOTE - making VMStates fully restorable is currently very
 * expensive and should only be done on a selective basis
 */
public class RestorableVMState {
  
  /** the set of last executed insns */
  private TransitionStorage lastTransition;

  /* these are the icky parts - the history is kept as stacks inside the
   * VM (for restoration reasons), hence we have to copy it if we want
   * to restore a state. Since this is really expensive, it has to be done
   * on demand, with varying degrees of information
   */
  private PathStorage path;

  Backtracker.RestorableState bkstate;

  RestorableVMState (VM vm) {
    Path p = vm.getPath();
    path = p == null ? null : p.store();
    bkstate = vm.getBacktracker().getRestorableState();
    lastTransition = vm.lastTrailInfo == null ? null : vm.lastTrailInfo.store();
  }
  
  public Backtracker.RestorableState getBkState() {
    return bkstate;
  }
  
  public Transition getLastTransition () {
    return lastTransition == null ? null : lastTransition.restore();
  }

  public boolean hasNoPath() {
    return path == null;
  }

  public Path getPath () {
    return path.restore();
  }
  
  public int getThread () {
    return lastTransition.ti;
  }

}
