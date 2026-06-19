package com.novafx.core;

import com.novafx.core.domain.Preset;
import com.novafx.math.FunctionDefinition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PresetTest {

    @Test
    void shouldCreateValidPreset() {
        FunctionDefinition def = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.1);
        Preset p = new Preset("Test", def);
        assertThat(p.name()).isEqualTo("Test");
        assertThat(p.definition()).isSameAs(def);
    }

    @Test
    void shouldRejectBlankName() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "t", 0, 1, 1);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Preset("", def));
    }

    @Test
    void shouldRejectNullName() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "t", 0, 1, 1);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Preset(null, def));
    }

    @Test
    void shouldRejectNullDefinition() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Preset("Test", null));
    }

    @Test
    void shouldHaveCorrectEquality() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "t", 0, 1, 1);
        Preset a = new Preset("Same", def);
        Preset b = new Preset("Same", def);
        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    void shouldNotBeEqualToDifferentName() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "t", 0, 1, 1);
        Preset a = new Preset("A", def);
        Preset b = new Preset("B", def);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringShouldContainName() {
        Preset p = new Preset("Awesome", new FunctionDefinition("t", "t", "t", 0, 1, 1));
        assertThat(p.toString()).contains("Awesome");
    }
}
