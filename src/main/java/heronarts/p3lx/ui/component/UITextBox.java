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

package heronarts.p3lx.ui.component;

import heronarts.lx.clipboard.LXClipboardItem;
import heronarts.lx.clipboard.LXTextValue;
import heronarts.lx.command.LXCommand;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UICopy;
import heronarts.p3lx.ui.UIPaste;

public class UITextBox extends UIInputBox implements UICopy, UIPaste {

  private final static String NO_VALUE = "-";

  private String value = NO_VALUE;
  private StringParameter parameter = null;

  private final LXParameterListener parameterListener = (p) -> {
    setValue(this.parameter.getString(), false);
  };

  public UITextBox() {
    this(0, 0, 0, 0);
  }

  public UITextBox(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  @Override
  public LXParameter getParameter() {
    return this.parameter;
  }

  public UITextBox setParameter(StringParameter parameter) {
    if (this.parameter != null) {
      this.parameter.removeListener(this.parameterListener);
    }
    this.parameter = parameter;
    if (parameter != null) {
      this.parameter.addListener(this.parameterListener);
      setValue(parameter.getString(), false);
    } else {
      setValue(NO_VALUE);
    }
    return this;
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  public String getValue() {
    return this.value;
  }

  @Override
  protected String getValueString() {
    return this.value;
  }

  @Override
  protected String getEditBufferValue() {
    return this.value;
  }

  public UITextBox setValue(String value) {
    return setValue(value, true);
  }

  public UITextBox setValue(String value, boolean pushToParameter) {
    if (!this.value.equals(value)) {
      this.value = value;
      if (pushToParameter && (this.parameter != null)) {
        if (this.useCommandEngine) {
          getUI().lx.command.perform(new LXCommand.Parameter.SetString(this.parameter, value));
        } else {
          this.parameter.setValue(value);
        }
      }
      this.onValueChange(this.value);
      redraw();
    }
    return this;
  }

  /**
   * Subclasses may override to handle value changes
   *
   * @param value New value being set
   */
  protected /* abstract */ void onValueChange(String value) {}


  @Override
  protected void saveEditBuffer() {
    String value = this.editBuffer.trim();
    if (value.length() > 0) {
      setValue(value);
    }
  }

  private static final String VALID_CHARACTERS =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ,.<>?;':\"[]{}-=_+`~!@#$%^&*()|1234567890/\\";

  public static boolean isValidTextCharacter(char keyChar) {
    return VALID_CHARACTERS.indexOf(keyChar) >= 0;
  }

  @Override
  protected boolean isValidCharacter(char keyChar) {
    return isValidTextCharacter(keyChar);
  }

  @Override
  public LXClipboardItem onCopy() {
    if (this.parameter != null) {
      return new LXTextValue(this.parameter);
    }
    return null;

  }

  @Override
  public void onPaste(LXClipboardItem item) {
    if (item instanceof LXTextValue) {
      if (isEnabled() && isEditable()) {
        setValue(((LXTextValue) item).getValue());
      }
    }
  }

}
