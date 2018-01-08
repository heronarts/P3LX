/**
 * Copyright 2015- Mark C. Slee, Heron Arts LLC
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
 * @author L8on <lwallace@gmail.com>
 */

package heronarts.p3lx.ui.control;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;

import java.util.ArrayList;
import java.util.List;

/**
 * UIWindow implementation to control a list of effects.
 * It ultimately operates on a static List of LXEffects,
 * but has convenient constructors that accept an
 * LX or LXChannel object as the source of effects.
 */
public class UIEffectsControl extends UIWindow {
  private final static String DEFAULT_TITLE = "EFFECT";
  private final static int DEFAULT_NUM_KNOBS = 4;
  private final static int KNOBS_PER_ROW = 4;
  private final static int KNOB_ROW_HEIGHT = 48;
  private final static int BASE_HEIGHT = 174;
  public final static int WIDTH = 140;

  private final UIKnob[] knobs;
  private LXEffect selectedEffect;

  public UIEffectsControl(UI ui, LX lx, float x, float y) {
    this(ui, lx, DEFAULT_TITLE, x, y);
  }

  public UIEffectsControl(UI ui, LX lx, int numKnobs, float x, float y) {
    this(ui, lx, DEFAULT_TITLE, numKnobs,  x, y);
  }

  public UIEffectsControl(UI ui, LX lx, String label, float x, float y) {
    this(ui, lx, label, DEFAULT_NUM_KNOBS, x, y);
  }

  public UIEffectsControl(UI ui, LX lx, String label, int numKnobs, float x, float y) {
    this(ui, lx.engine.masterChannel.getEffects(), label, numKnobs, x, y);
  }

  public UIEffectsControl(UI ui, LXChannel channel, float x, float y) {
    this(ui, channel, DEFAULT_TITLE, x, y);
  }

  public UIEffectsControl(UI ui, LXChannel channel, String label, float x, float y) {
    this(ui, channel.getEffects(), label, DEFAULT_NUM_KNOBS, x, y);
  }

  public UIEffectsControl(UI ui, LXChannel channel, int numKnobs, float x, float y) {
    this(ui, channel.getEffects(), DEFAULT_TITLE, numKnobs, x, y);
  }

  public UIEffectsControl(UI ui, List<LXEffect> effects, String label, int numKnobs, float x, float y) {
    super(ui, label, x, y, WIDTH, BASE_HEIGHT + KNOB_ROW_HEIGHT * (numKnobs / KNOBS_PER_ROW));

    int yp = TITLE_LABEL_HEIGHT;

    List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
    for (LXEffect eff : effects) {
      items.add(new EffectScrollItem(eff));
    }
    final UIItemList.ScrollList effectList = (UIItemList.ScrollList)
      new UIItemList.ScrollList(ui, 1, yp, this.width - 2, 140)
      .setMomentary(true)
      .setItems(items);

    effectList
      .setBackgroundColor(ui.theme.getDeviceBackgroundColor())
      .addToContainer(this);
    yp += effectList.getHeight() + 10;

    this.knobs = new UIKnob[numKnobs];
    for (int ki = 0; ki < knobs.length; ++ki) {
      knobs[ki] = new UIKnob(5 + 34 * (ki % KNOBS_PER_ROW), yp
          + (ki / KNOBS_PER_ROW) * KNOB_ROW_HEIGHT);
      knobs[ki].addToContainer(this);
    }

    if (effects.size() > 0) {
      selectEffect(effects.get(0));
    }
  }

  private void selectEffect(LXEffect effect) {
    this.selectedEffect = effect;

    int pi = 0;
    for (LXParameter parameter : effect.getParameters()) {
      if (pi >= this.knobs.length) {
        break;
      }
      if (parameter instanceof LXListenableNormalizedParameter) {
        this.knobs[pi++].setParameter((LXListenableNormalizedParameter) parameter);
      }
    }

    while (pi < this.knobs.length) {
      this.knobs[pi++].setParameter(null);
    }
  }

  private class EffectScrollItem extends UIItemList.AbstractItem {

    private final LXEffect effect;

    EffectScrollItem(LXEffect effect) {
      this.effect = effect;
    }

    public String getLabel() {
      return this.effect.getLabel();
    }

    @Override
    public boolean isActive() {
      return
        (selectedEffect == this.effect) ||
        this.effect.isEnabled();
    }

    @Override
    public int getActiveColor(UI ui) {
      return (selectedEffect == this.effect) ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
    }

    @Override
    public void onActivate() {
      if (this.effect != selectedEffect) {
        selectEffect(this.effect);
      }

      this.effect.toggle();
    }

    @Override
    public void onDeactivate() {
      this.effect.disable();
    }
  }
}
