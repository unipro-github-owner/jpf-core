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

import java.io.Serializable;
import java.util.Iterator;

import gov.nasa.jpf.vm.ChoiceGenerator.CgStorage;

/**
 * concrete type to store execution paths. TrailInfo corresponds to Transition,
 * i.e. all instructions executed in the context of a vm.forward() leading
 * into a new state
 */
public class Transition implements Iterable<Step>, Cloneable {

  ChoiceGenerator<?> cg;
  ThreadInfo ti;

  private Step   first, last;
  int nSteps;

  private Object annotation;
  String         output;

  private int stateId = StateSet.UNKNOWN_ID;

  public Transition (ChoiceGenerator<?> cg, ThreadInfo ti) {
    this.cg = cg;
    this.ti = ti;
  }

  @Override
  public Object clone() {
    try {
      Transition t = (Transition)super.clone();

      // the deep copy references
      t.cg = cg.clone();
      t.ti = (ThreadInfo)ti.clone();

      return t;

    } catch (CloneNotSupportedException cnsx){
      return null; // cannot happen
    }
  }

  public String getLabel () {
    if (last != null) {
      return last.getLineString();
    } else {
      return "?";
    }
  }

  public int getStateId() {
    return(stateId);
  }

  public void setStateId(int id) {
    stateId = id;
  }

  public void setOutput (String s) {
    output = s;
  }

  public void setAnnotation (Object o) {
    annotation = o;
  }

  public Object getAnnotation () {
    return annotation;
  }

  public String getOutput () {
    return output;
  }

  // don't use this for step iteration - this is very inefficient
  public Step getStep (int index) {
    Step s = first;
    for (int i=0; s != null && i < index; i++) s = s.next;
    return s;
  }

  public Step getLastStep () {
    return last;
  }

  public int getStepCount () {
    return nSteps;
  }

  public ThreadInfo getThreadInfo() {
    return ti;
  }

  public int getThreadIndex () {
    return ti.getId();
  }

  public ChoiceGenerator<?> getChoiceGenerator() {
    return cg;
  }

  public ChoiceGenerator<?>[] getChoiceGeneratorCascade(){
    return cg.getCascade();
  }

  public void incStepCount() {
    nSteps++;
  }

  void addStep (Step step) {
    if (first == null) {
      first = step;
      last = step;
    } else {
      last.next = step;
      last = step;
    }
    nSteps++;
  }

  public class StepIterator implements Iterator<Step> {
    Step cur;

    @Override
	public boolean hasNext () {
      return (cur != last);
    }

    @Override
	public Step next () {
      if (cur == null) {
        cur = first;
      } else {
        if (cur != last) {
          cur = cur.next;
        } else {
          return null;
        }
      }
      return cur;
    }

    @Override
	public void remove () {
      if (cur == null) {
        first = first.next;
      } else {
        Step s;
        for (s = first; s.next != cur; s = s.next);
        s.next = cur.next;
        cur = cur.next;
      }
    }
  }

  @Override
  public Iterator<Step> iterator () {
    return new StepIterator();
  }

  static class TransitionStorage implements Serializable {
    private static final long serialVersionUID = 1L;
    CgStorage<?> cg;
    int ti;
    long nonSerialFirstId; //FIXME TODO investigate private Step   first, last;
    long nonSerialLastId; //FIXME TODO investigate
    int nSteps;
    long nonSerialAnnotationId; //FIXME TODO investigate Object annotation;
    String         output;
    int stateId;

    public Transition restore() {
      Transition t = new Transition(cg.restore(), VM.getVM().getThreadList().getThreadInfoForId(ti));
      t.first = (Step)VM.NON_SERIAL_STORAGE.remove(nonSerialFirstId);
      t.last = (Step)VM.NON_SERIAL_STORAGE.remove(nonSerialLastId);
      t.nSteps = nSteps;
      t.annotation = VM.NON_SERIAL_STORAGE.remove(nonSerialAnnotationId);
      t.output = output;
      t.stateId = stateId;
      return t;
    }
  }

  public TransitionStorage store() {
    TransitionStorage s = new TransitionStorage();
    s.cg = cg.store();
    s.ti = ti.getId();
    s.nonSerialFirstId = VM.NON_SERIAL_ID.getAndIncrement();
    VM.NON_SERIAL_STORAGE.put(s.nonSerialFirstId, first);
    s.nonSerialLastId = VM.NON_SERIAL_ID.getAndIncrement();
    VM.NON_SERIAL_STORAGE.put(s.nonSerialLastId, last);
    s.nSteps = nSteps;
    s.nonSerialAnnotationId = VM.NON_SERIAL_ID.getAndIncrement();
    VM.NON_SERIAL_STORAGE.put(s.nonSerialAnnotationId, annotation);
    s.output = output;
    s.stateId = stateId;
    return s;
  };
}
