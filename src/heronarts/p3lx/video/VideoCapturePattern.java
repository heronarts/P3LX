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

package heronarts.p3lx.video;

import heronarts.p3lx.P3LX;
import heronarts.p3lx.P3LXPattern;
import processing.video.Capture;

public class VideoCapturePattern extends P3LXPattern {

  private Capture capture;

  public VideoCapturePattern(P3LX lx) {
    super(lx);
    this.capture = null;
  }

  @Override
  public void onActive() {
    this.capture = new Capture(this.applet, this.lx.width, this.lx.height);
  }

  @Override
  public void onInactive() {
    this.capture.dispose();
    this.capture = null;
  }

  @Override
  public void run(double deltaMs) {
    if (this.capture.available()) {
      this.capture.read();
    }
    this.capture.loadPixels();
    for (int i = 0; i < this.colors.length; ++i) {
      this.colors[i] = this.capture.pixels[i];
    }
  }

}
