package heronarts.p3lx.ui.control;

import heronarts.lx.LX;
import heronarts.lx.Tempo;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIIntegerBox;

public class UITempoControl extends UIWindow implements LXParameterListener {

  public final static int WIDTH = 140;
  public final static int HEIGHT = 46;

  private final Tempo tempo;


  private final DiscreteParameter tempoMajor;
  private final DiscreteParameter tempoMinor;

  public UITempoControl(UI ui, LX lx, float x, float y) {
    this(ui, lx.tempo, x, y);
  }

  public UITempoControl(UI ui, final Tempo tempo, float x, float y) {
    super(ui, "TEMPO", x, y, WIDTH, HEIGHT);
    this.tempo = tempo;
    this.tempoMajor = new DiscreteParameter("MAJOR", (int) tempo.bpm(), (int) Tempo.MIN_BPM, (int) Tempo.MAX_BPM+1);
    this.tempoMinor = new DiscreteParameter("MINOR", (int) Math.round(100 * (tempo.bpm() % 1)), 0, 100);
    this.tempoMajor.addListener(this);
    this.tempoMinor.addListener(this);
    tempo.bpm.addListener(this);

    float yPos = 20;
    new UIIntegerBox(4, yPos, 42, 20).setParameter(this.tempoMajor).addToContainer(this);
    new UIIntegerBox(48, yPos, 28, 20).setParameter(this.tempoMinor).addToContainer(this);
    new UIButton(80, yPos, this.width-84, 20) {
      @Override
      protected void onToggle(boolean active) {
        if (active) {
          tempo.tap();
        }
      }
    }.setLabel("TAP").setMomentary(true).addToContainer(this);

  }

  public void onParameterChanged(LXParameter parameter) {
    if (parameter == this.tempo.bpm) {
      double bpm = this.tempo.bpm();
      this.tempoMajor.setValue((int) bpm);
      this.tempoMinor.setValue(Math.round(100 * (bpm % 1)));
    } else if ((parameter == this.tempoMajor) || (parameter == this.tempoMinor)) {
      this.tempo.setBpm(this.tempoMajor.getValuei() + this.tempoMinor.getValuei() / 100.);
    }
  }
}
