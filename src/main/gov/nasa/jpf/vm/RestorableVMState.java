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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import gov.nasa.jpf.vm.Path.PathStorage;
import gov.nasa.jpf.vm.Transition.TransitionStorage;

/**
 * NOTE - making VMStates fully restorable is currently very
 * expensive and should only be done on a selective basis
 */
public class RestorableVMState {
  private static AtomicLong fileName = new AtomicLong();
  private static java.nio.file.Path storage;
  static {
    //FIXME TODO this need to be made configurable and moved to Configuration
    try {
      storage = Files.createDirectories(Paths.get("tmp", "jpf", "storage"));
    } catch (IOException e) {
      // FIXME TODO will be properly handled after moving to correct place
      e.printStackTrace();
    }
  }

  /** the set of last executed insns */
  //private TransitionStorage lastTransition;

  /* these are the icky parts - the history is kept as stacks inside the
   * VM (for restoration reasons), hence we have to copy it if we want
   * to restore a state. Since this is really expensive, it has to be done
   * on demand, with varying degrees of information
   */
  //private PathStorage path;
  private Object data;
  private final long file;

  Backtracker.RestorableState bkstate;

  RestorableVMState (VM vm) {
    Path p = vm.getPath();
    PathStorage path = p == null ? null : p.store();
    bkstate = vm.getBacktracker().getRestorableState();
    TransitionStorage lastTransition = vm.lastTrailInfo == null ? null : vm.lastTrailInfo.store();

    //FIXME TODO this need to be made configurable
    file = fileName.getAndIncrement();
    try (OutputStream os = new FileOutputStream(storage.resolve("" + file).toFile());
        ObjectOutputStream oos = new ObjectOutputStream(os);)
    {
      oos.writeObject(lastTransition);
      oos.writeObject(path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private VmStateData getData() {
    if (data == null) {
      File p = storage.resolve("" + file).toFile();
      try (InputStream is = new FileInputStream(p);
          ObjectInputStream ois = new ObjectInputStream(is);)
      {
        TransitionStorage lastTransition = (TransitionStorage)ois.readObject();
        PathStorage path = (PathStorage)ois.readObject();
        data = new VmStateData(lastTransition, path);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        p.delete();
      }
    }
    return (VmStateData)data;
  }

  public Backtracker.RestorableState getBkState() {
    return bkstate;
  }

  public Transition getLastTransition () {
    return getData().lastTransition;
  }

  public boolean hasNoPath() {
    return getData().path == null;
  }

  public Path getPath () {
    return getData().path;
  }

  public int getThread () {
    return getData().lastTransition.ti.id;
  }

  private final static class VmStateData {
    private Transition lastTransition;
    private Path path;

    VmStateData(TransitionStorage lastTransition, PathStorage path) {
      this.lastTransition = lastTransition == null ? null : lastTransition.restore();
      this.path = path == null ? null : path.restore();
    }
  }
}
