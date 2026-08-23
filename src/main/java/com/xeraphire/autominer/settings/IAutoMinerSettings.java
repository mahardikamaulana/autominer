package com.xeraphire.autominer.settings;

public interface IAutoMinerSettings {
    boolean isRequireFuel();
    int getMiningSpeed();
    int getFuelDuration();
    boolean isEnableEndgameMiners();

    void setRequireFuel(boolean requireFuel);
    void setMiningSpeed(int speed);
    void setFuelDuration(int duration);
    void setEnableEndgameMiners(boolean enable);
}
