package com.novafx.core;

import com.novafx.core.domain.Project;
import com.novafx.math.FunctionDefinition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProjectTest {

    private static final FunctionDefinition DEF = new FunctionDefinition("t", "t", "t", 0, 1, 1);

    @Test
    void shouldCreateValidProject() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Project p = new Project(id, "Test", "desc", DEF, now, now);
        assertThat(p.id()).isEqualTo(id);
        assertThat(p.name()).isEqualTo("Test");
        assertThat(p.description()).isEqualTo("desc");
        assertThat(p.createdAt()).isEqualTo(now);
        assertThat(p.functionDefinition()).isSameAs(DEF);
    }

    @Test
    void shouldDefaultDescriptionToEmpty() {
        Project p = new Project(UUID.randomUUID(), "Test", null, DEF, Instant.now(), Instant.now());
        assertThat(p.description()).isEmpty();
    }

    @Test
    void shouldRejectNullId() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Project(null, "n", "d", DEF, Instant.now(), Instant.now()));
    }

    @Test
    void shouldRejectBlankName() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Project(UUID.randomUUID(), "  ", "d", DEF, Instant.now(), Instant.now()));
    }

    @Test
    void shouldRejectNullName() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Project(UUID.randomUUID(), null, "d", DEF, Instant.now(), Instant.now()));
    }

    @Test
    void shouldRejectNullFunctionDefinition() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Project(UUID.randomUUID(), "n", "", null, Instant.now(), Instant.now()));
    }

    @Test
    void shouldUseIdForEquality() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Project a = new Project(id, "A", "", DEF, now, now);
        Project b = new Project(id, "B", "diff", DEF, now, now);
        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    void shouldNotBeEqualToDifferentId() {
        Instant now = Instant.now();
        Project a = new Project(UUID.randomUUID(), "A", "", DEF, now, now);
        Project b = new Project(UUID.randomUUID(), "A", "", DEF, now, now);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringShouldContainName() {
        Project p = new Project(UUID.randomUUID(), "MyProject", "", DEF, Instant.now(), Instant.now());
        assertThat(p.toString()).contains("MyProject");
    }
}
