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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.ObjectList;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Random;

/**
 * abstract root class for configurable choice generators
 */
public abstract class ChoiceGeneratorBase<T> extends ChoiceGeneratorLight<T> implements ChoiceGenerator<T> {

  /**
   * choice randomization policies, which can be set from JPF configuration
   */
  // want the id to be visible to subclasses outside package
  protected String id;
  
  // for subsequent access, there is no need to translate a JPF String object reference
  // into a host VM String anymore (we just need it for creation to look up
  // the class if this is a named CG)
  protected int idRef;
  
  // the state id of the state in which the CG was created
  protected int stateId;
  
  // and the thread that executed this insn
  protected ThreadInfo ti;

  /**
   *  don't use this since it is not safe for cascaded ChoiceGenerators
   * (we need the 'id' to be as context specific as possible)
   */
  @Deprecated
  protected ChoiceGeneratorBase() {
    id = "?";
  }

  protected ChoiceGeneratorBase(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public int getIdRef() {
    return idRef;
  }

  @Override
  public void setIdRef(int idRef) {
    this.idRef = idRef;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public boolean isSchedulingPoint() {
    return false;
  }

  //--- the getters and setters for the CG creation info
  @Override
  public void setThreadInfo(ThreadInfo ti) {
    this.ti = ti;
  }

  @Override
  public ThreadInfo getThreadInfo() {
    return ti;
  }

  @Override
  public void setContext(ThreadInfo tiCreator) {
    ti = tiCreator;
    insn = tiCreator.getPC();
  }
  
  @Override
  public void setStateId(int stateId){
    this.stateId = stateId;

    if (isCascaded){
      getCascadedParent().setStateId(stateId);
    }
  }

  @Override
  public int getStateId(){
    return stateId;
  }

  // -- end attrs --
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder(getClass().getName());
    b.append(" {id:\"");
    b.append(id);
    b.append("\" ,");
    b.append(getProcessedNumberOfChoices());
    b.append('/');
    b.append(getTotalNumberOfChoices());
    b.append(",isCascaded:");
    b.append(isCascaded);

    if (attr != null) {
      b.append(",attrs:[");
      int i = 0;
      for (Object a : ObjectList.iterator(attr)) {
        if (i++ > 1) {
          b.append(',');
        }
        b.append(a);
      }
      b.append(']');
    }

    b.append('}');

    return b.toString();
  }
}
