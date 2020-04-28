/**
 * Copyright 2019- Mark C. Slee, Heron Arts LLC
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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.ui;

import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;

public class UIDialogBox extends UI2dContainer implements UIMouseFocus {

  private static final int PADDING = 4;
  private static final int WIDTH = 280;
  private static final int HEIGHT = 80;

  public static final int OPTION_WIDTH = 60;
  private static final int OPTION_PADDING = 4;

  private static final int BUTTON_ROW = 22;

  private static int[] optionWidthArray(int length) {
    int[] arr = new int[length];
    for (int i = 0; i < arr.length; ++i) {
      arr[i] = OPTION_WIDTH;
    }
    return arr;
  }

  public UIDialogBox(UI ui, String message) {
    this(ui, message, new String[] { "Okay" }, null);
  }

  public UIDialogBox(UI ui, String message, Runnable callback) {
    this(ui, message, new String[] { "Okay" }, new Runnable[] { callback });
  }

  public UIDialogBox(UI ui, String message, String[] options, Runnable[] callbacks) {
    this(ui, message, options, optionWidthArray(options.length), callbacks);
  }

  public UIDialogBox(UI ui, String message, String[] options, int[] optionWidth, Runnable[] callbacks) {
    super((ui.getWidth() - WIDTH) / 2, (ui.getHeight() - 2*HEIGHT) / 2, WIDTH, HEIGHT);
    setBackgroundColor(ui.theme.getDeviceFocusedBackgroundColor());
    setBorderColor(UI.BLACK);
    setBorderRounding(4);

    new UILabel(PADDING, PADDING, this.width - 2*PADDING, this.height - 2*PADDING - BUTTON_ROW)
    .setLabel(message)
    .setPadding(4)
    .setTextAlignment(PConstants.LEFT, PConstants.TOP)
    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
    .setBorderRounding(4)
    .addToContainer(this);

    int optionTotalWidth = 0;
    for (int width : optionWidth) {
      optionTotalWidth += width;
    }
    optionTotalWidth += (options.length - 1) * OPTION_PADDING;

    float yp = this.height - BUTTON_ROW;
    float xp = this.width / 2 - optionTotalWidth / 2;
    for (int i = 0; i < options.length; ++i) {
      final int ii = i;
      new UIButton.Action(xp, yp, optionWidth[i], 16) {
        @Override
        protected void onClick() {
          getUI().hideContextOverlay();
          if ((callbacks != null) && (callbacks[ii] != null)) {
            callbacks[ii].run();
          }
        }
      }
      .setLabel(options[i])
      .addToContainer(this);
      xp += optionWidth[i] + OPTION_PADDING;
    }
  }
}
