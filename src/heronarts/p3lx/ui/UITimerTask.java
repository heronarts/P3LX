/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui;

import heronarts.lx.LXLoopTask;

public abstract class UITimerTask implements LXLoopTask {

  private double accum = 0;

  private final double period;

  public enum Mode {
    MILLISECONDS,
    FPS
  }

  protected UITimerTask(double period) {
    this(period, Mode.MILLISECONDS);
  }

  protected UITimerTask(double period, Mode mode) {
    this.period = (mode == Mode.FPS) ? (1000. / period) : period;
  }

  @Override
  public final void loop(double deltaMs) {
    this.accum += deltaMs;
    if (this.accum >= this.period) {
      this.accum = this.accum % this.period;
      run();
    }
  }

  /**
   * Subclasses implement this method to perform the operation
   */
  protected abstract void run();

}
