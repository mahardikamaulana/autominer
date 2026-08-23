package com.xeraphire.autominer.settings;

import customsettingslib.components.CustomModSetting;
import customsettingslib.components.vanillaimproved.SwitchableFormLocalSlider;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.gfx.gameFont.FontOptions;

import java.util.concurrent.atomic.AtomicReference;

public class MultiplierSetting extends CustomModSetting<Integer> {
    public final int min;
    public final int max;
    public final AtomicReference<Integer> newValue = new AtomicReference<>();

    public MultiplierSetting(String id, int defaultValue, int min, int max) {
        super(id, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void addSaveData(SaveData saveData) {
        saveData.addInt(id, value);
    }

    @Override
    public void applyLoadData(LoadData loadData) {
        value = loadData.getInt(id, defaultValue);
    }

    @Override
    public void setupPacket(PacketWriter writer) {
        writer.putNextInt(value);
    }

    @Override
    public Integer applyPacket(PacketReader reader) {
        return reader.getNextInt();
    }

    @Override
    protected boolean isValidValue(Object value) {
        return super.isValidValue(value) && inBounds((Integer) value);
    }

    protected boolean inBounds(int value) {
        return min <= value && value <= max;
    }

    @Override
    public int addComponents(int y, int n) {
        newValue.set(value);
        int width = getWidth();
        boolean isEnabled = isEnabled();

        SwitchableFormLocalSlider slider = new SwitchableFormLocalSlider("settingsui", id, LEFT_MARGIN, y, getTrueValue(), min, max, width, new FontOptions(16)) {
            @Override
            public String getValueText() {
                return getValue() + "x";
            }
        };
        slider.drawValueInPercent = false;
        slider.drawValue = true;
        slider.onChanged((e) -> newValue.set(e.from.getValue()));
        slider.setActive(isEnabled);

        settingsForm.addComponent(slider);
        return slider.getTotalHeight();
    }

    @Override
    public void restoreToDefault() {
        super.restoreToDefault();
        newValue.set(defaultValue);
    }

    @Override
    public void onSave() {
        changeValue(newValue.get());
    }
}
