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

package heronarts.p3lx.font;

import heronarts.p3lx.P3LX;
import heronarts.p3lx.P3LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Example pattern to render a text string using PixelFont.
 */
public class PixelFontPattern extends P3LXPattern {

  final private SawLFO hMod = new SawLFO(0, 360, 10000);
  final private SawLFO pMod = new SawLFO(0, 0, 10000);
  final private PImage image;

  public PixelFontPattern(P3LX lx) {
    this(lx, "The quick brown fox jumped over the lazy dog.");
  }

  public PixelFontPattern(P3LX lx, String s) {
    super(lx);
    this.image = (new PixelFont(lx)).drawString(s);
    this.addModulator(this.hMod).trigger();
    this.addModulator(
        this.pMod.setRange(-lx.width, this.image.width, this.image.width * 250))
        .trigger();
  }

  @Override
  public void run(double deltaMs) {
    for (int i = 0; i < this.colors.length; ++i) {
      double col = this.lx.column(i) + this.pMod.getValue();
      int floor = (int) Math.floor(col);
      int ceil = (int) Math.ceil(col);
      float b1 = this.applet
          .brightness(this.image.get(floor, this.lx.row(i)));
      float b2 = this.applet
          .brightness(this.image.get(ceil, this.lx.row(i)));

      this.colors[i] = LXColor.hsb(this.hMod.getValue(), 100.,
          PApplet.lerp(b1, b2, (float) (col - floor)));
    }
  }
}
