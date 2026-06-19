package com.novafx.project.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watches directories for changes to {@code .nfx} files and fires a callback
 * when a file is modified.
 * <p>
 * Uses Java's {@link WatchService} with debouncing to avoid duplicate events
 * from editors that write atomically or in multiple passes.
 * <p>
 * Lifecycle: {@link #start()} to begin watching, {@link #stop()} to shut down.
 * Once stopped a watcher cannot be restarted.
 */
public final class ProjectFileWatcher {

    private static final Logger log = LoggerFactory.getLogger(ProjectFileWatcher.class);

    /** File extension to watch (case-insensitive). */
    private static final String NFX_EXT = ".nfx";

    /** Debounce interval in milliseconds — events within this window are collapsed. */
    private static final long DEBOUNCE_MS = 300;

    private final WatchService watcher;
    private final Consumer<Path> onChange;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    /** Tracks last modified time per file for debouncing. */
    private final ConcurrentHashMap<Path, Long> lastFired = new ConcurrentHashMap<>();

 /**
     * Creates a file watcher that fires {@code onChange} for changed
     * {@code .nfx} files in any registered directory.
     *
     * @param onChange called (on the watcher thread) when a .nfx file changes;
     *                 the argument is the absolute path to the file.
     *                 Must not be null.
     * @throws IOException when the WatchService cannot be created
     */
    public ProjectFileWatcher(Consumer<Path> onChange) throws IOException {
        this.onChange = Objects.requireNonNull(onChange, "onChange must not be null");
        this.watcher = FileSystems.getDefault().newWatchService();
    }

    // ── Lifecycle ──

    /**
     * Registers a directory for watching.  All existing {@code .nfx} files
     * in the directory are <em>not</em> fired on registration — only
     * subsequent modifications trigger the callback.
     * <p>
     * Directories are watched non-recursively.
     *
     * @param dir the directory to watch; must exist and be readable
     * @throws IOException when the directory cannot be registered
     */
    public void watchDirectory(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Not a directory: " + dir);
        }
        dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        log.debug("Watching directory: {}", dir);
    }

    /**
     * Registers a directory and all its subdirectories recursively.
     *
     * @param root the root directory to watch recursively
     * @throws IOException when walking the tree fails
     */
    public void watchDirectoryRecursive(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                watchDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Starts the watcher thread.  No-op if already running or stopped.
     */
    public void start() {
        if (stopped.get()) {
            log.warn("Cannot restart a stopped ProjectFileWatcher");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            return; // already running
        }

        Thread thread = Thread.ofVirtual()
                .name("nfx-watcher")
                .start(this::pollLoop);

        log.debug("File watcher started");
    }

    /**
     * Stops the watcher.  Once stopped the instance cannot be restarted.
     */
    public void stop() {
        stopped.set(true);
        running.set(false);
        try {
            watcher.close();
        } catch (IOException e) {
            log.warn("Error closing watcher: {}", e.getMessage());
        }
        lastFired.clear();
        log.debug("File watcher stopped");
    }

    /**
     * Returns {@code true} when the watcher thread is active.
     */
    public boolean isRunning() {
        return running.get();
    }

    // ── Internal ──

    private void pollLoop() {
        while (running.get() && !stopped.get()) {
            try {
                WatchKey key = watcher.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    handleEvent(key, event);
                }

                if (!key.reset()) {
                    log.warn("Watch key no longer valid — directory may have been deleted");
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleEvent(WatchKey key, WatchEvent<?> raw) {
        WatchEvent.Kind<?> kind = raw.kind();
        if (kind == OVERFLOW) return;

        var ev = (WatchEvent<Path>) raw;
        Path dir = (Path) key.watchable();
        Path child = dir.resolve(ev.context());

        // Only interested in .nfx files
        if (!child.toString().toLowerCase().endsWith(NFX_EXT)) return;
        if (!Files.isRegularFile(child)) return;

        // Debounce: don't fire if we just fired for this file
        long now = System.currentTimeMillis();
        Long last = lastFired.get(child);
        if (last != null && (now - last) < DEBOUNCE_MS) {
            return;
        }

        // Wait for the file to be fully written (up to 2 seconds)
        try {
            Path stable = waitForStableFile(child, 2_000);
            if (stable == null) {
                log.warn("File {} did not stabilize within timeout", child);
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        lastFired.put(child, now);
        log.debug("File changed: {}", child);

        try {
            onChange.accept(child.toRealPath());
        } catch (Exception e) {
            log.error("File change handler threw for {}: {}", child, e.getMessage());
        }
    }

    /**
     * Waits for a file to stop changing size, confirming the write is complete.
     * Some editors write in multiple passes — this avoids firing before the
     * content is fully flushed.
     */
    private static Path waitForStableFile(Path path, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        long lastSize = -1;

        while (System.currentTimeMillis() < deadline) {
            if (!Files.exists(path)) return null;
            try {
                long size = Files.size(path);
                if (size == lastSize && size > 0) {
                    return path; // stable
                }
                lastSize = size;
            } catch (IOException e) {
                return null;
            }
            Thread.sleep(50);
        }
        return null;
    }
}
