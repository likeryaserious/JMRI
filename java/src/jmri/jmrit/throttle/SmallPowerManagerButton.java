package jmri.jmrit.throttle;

import jmri.jmrit.catalog.NamedIcon;

public class SmallPowerManagerButton extends PowerManagerButton {

    /**
     *
     */
    private static final long serialVersionUID = -8046373027649683567L;

    public SmallPowerManagerButton() {
        super();
        setBorderPainted(false);
    }

    void loadIcons() {
        powerOnIcon = new NamedIcon("resources/icons/throttles/GreenPowerLED.gif", "resources/icons/throttles/GreenPowerLED.gif");
        powerOffIcon = new NamedIcon("resources/icons/throttles/RedPowerLED.gif", "resources/icons/throttles/RedPowerLED.gif");
        powerXIcon = new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif");
    }

}
