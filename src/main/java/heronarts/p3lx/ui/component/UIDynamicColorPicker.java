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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.ui.component;

import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXDynamicColor;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITimerTask;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDynamicColorPicker extends UIColorPicker implements UIFocus {

  private final LXDynamicColor dynamicColor;

  public UIDynamicColorPicker(LXDynamicColor dynamicColor) {
    this(0, 0, 16, 16, dynamicColor);
  }

  public UIDynamicColorPicker(float x, float y, float w, float h, LXDynamicColor dynamicColor) {
    super(x, y, w, h, dynamicColor.primary, true);
    setBorderRounding(4);
    this.dynamicColor = dynamicColor;
    addLoopTask(new UITimerTask(30, UITimerTask.Mode.FPS) {
      @Override
      protected void run() {
        setBackgroundColor(dynamicColor.getColor());
      }
    });
    setFocusCorners(false);
  }

  @Override
  protected void drawFocus(UI ui, PGraphics pg) {
    pg.stroke(UI.WHITE);
    pg.strokeWeight(1);
    pg.noFill();
    pg.rect(0, 0, this.width-1, this.height-1, getBorderRounding());
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE || keyCode == java.awt.event.KeyEvent.VK_DELETE) {
      if (this.dynamicColor.getIndex() > 0) {
        consumeKeyEvent();
        hideOverlay();
        this.dynamicColor.getSwatch().removeColor(this.dynamicColor.getIndex());
      }
    }
    super.onKeyPressed(keyEvent, keyChar, keyCode);
  }

  @Override
  protected UIColorOverlay buildColorOverlay(UI ui) {
    return new UIDynamicColorOverlay(ui);
  }

  class UIDynamicColorOverlay extends UIColorPicker.UIColorOverlay {

    private final UI2dComponent
      blendMode,
      primaryColorSelector,
      secondaryColorSelector,
      arrowLabel,
      period;

    UIDynamicColorOverlay(UI ui) {
      super(38);

      // Horizontal break
      new UI2dComponent(12, 140, 220, 1) {}
      .setBorderColor(ui.theme.getDarkBackgroundColor())
      .addToContainer(this);

      UI2dContainer controls = UI2dContainer.newHorizontalContainer(16, 4);
      controls.setPosition(12, 148);

      new UIButton(64, 16, dynamicColor.mode)
      .addToContainer(controls);

      this.blendMode =
        new UIButton(28, dynamicColor.blendMode)
        .addToContainer(controls);

      this.primaryColorSelector = new UIColorSelector(dynamicColor.primary)
      .addToContainer(controls);

      this.arrowLabel =
        new UILabel(16, 16, "↔")
        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
        .setMargin(0, -4)
        .addToContainer(controls);

      this.secondaryColorSelector = new UIColorSelector(dynamicColor.secondary)
      .addToContainer(controls);

      this.period = new UIDoubleBox(48, dynamicColor.period)
      .setNormalizedMouseEditing(false)
      .setShiftMultiplier(60)
      .setProgressIndicator(new UIDoubleBox.ProgressIndicator() {
        @Override
        public boolean hasProgress() {
          return true;
        }
        @Override
        public double getProgress() {
          return dynamicColor.getBasis();
        }
      })
      .addToContainer(controls);

      new UIDynamicColorIndicator()
      .addToContainer(controls);

      focusColor(dynamicColor.primary);
      controls.addToContainer(this);

      dynamicColor.mode.addListener((p) -> { setMode(); } );
      setMode();
    }

    private void setMode() {
      boolean isFixed = dynamicColor.mode.getEnum() == LXDynamicColor.Mode.FIXED;
      boolean isOscillate = dynamicColor.mode.getEnum() == LXDynamicColor.Mode.OSCILLATE;
      this.period.setVisible(!isFixed);
      this.blendMode.setVisible(isOscillate);
      this.primaryColorSelector.setVisible(isOscillate);
      this.arrowLabel.setVisible(isOscillate);
      this.secondaryColorSelector.setVisible(isOscillate);
      if (!isOscillate) {
        focusColor(dynamicColor.primary);
      }
    }

    void focusColor(ColorParameter color) {
      setColor(color);
      if (color == dynamicColor.primary) {
        this.primaryColorSelector.setBorderWeight(2);
        this.primaryColorSelector.setBorderColor(UI.WHITE);
        this.secondaryColorSelector.setBorder(false);
      } else {
        this.secondaryColorSelector.setBorderWeight(2);
        this.secondaryColorSelector.setBorderColor(UI.WHITE);
        this.primaryColorSelector.setBorder(false);
      }
    }

    private class UIColorSelector extends UI2dComponent {

      private final ColorParameter color;

      public UIColorSelector(ColorParameter color) {
        super(0, 0, 16, 16);
        this.color = color;
        setBackgroundColor(color.getColor());
        color.addListener((p) -> { setBackgroundColor(color.getColor()); });
      }

      @Override
      public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        consumeMousePress();
        focusColor(this.color);
        super.onMousePressed(mouseEvent, mx, my);
      }
    }

    private class UIDynamicColorIndicator extends UI2dComponent {
      public UIDynamicColorIndicator() {
        super(0, 0, 16, 16);
        setBackgroundColor(dynamicColor.getColor());

        addLoopTask(new UITimerTask(30, UITimerTask.Mode.FPS) {
          @Override
          protected void run() {
            setBackgroundColor(dynamicColor.getColor());
          }
        });
      }
    }
  }


}
