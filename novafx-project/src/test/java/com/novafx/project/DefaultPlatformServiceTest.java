package com.novafx.project;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class DefaultPlatformServiceTest {

    private final DefaultPlatformService service = new DefaultPlatformService();

    @Test
    void workspaceShouldBeAbsolute() {
        Path ws = service.workspaceDirectory();
        assertThat(ws).isAbsolute();
        assertThat(ws.toString()).endsWith("workspace/projects");
    }

    @Test
    void configShouldBeAbsolute() {
        Path cfg = service.configDirectory();
        assertThat(cfg).isAbsolute();
        assertThat(cfg.toString()).endsWith("novafx");
    }

    @Test
    void dataShouldBeAbsolute() {
        Path data = service.dataDirectory();
        assertThat(data).isAbsolute();
        assertThat(data.toString()).endsWith("novafx");
    }

    @Test
    void workspaceShouldBeUnderData() {
        Path ws = service.workspaceDirectory();
        Path data = service.dataDirectory();
        assertThat(ws.startsWith(data)).isTrue();
    }
}
