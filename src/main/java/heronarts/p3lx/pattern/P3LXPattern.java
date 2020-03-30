/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;
import heronarts.p3lx.P3LX;
import processing.core.PApplet;

public abstract class P3LXPattern extends LXPattern {

  protected final P3LX lx;

  protected final PApplet applet;

  protected P3LXPattern(LX lx) {
    super(lx);
    if (!(lx instanceof P3LX)) {
      throw new IllegalArgumentException("P3LXPattern must be given a P3LX instance");
    }
    this.lx = (P3LX) lx;
    this.applet = this.lx.applet;
  }
}
