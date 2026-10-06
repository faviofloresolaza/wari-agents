package wari.agents.provider;

import java.nio.file.Path;
import java.util.List;

/** Provider-specific process launch; the Runner owns HU identity and state. */
public interface ProviderAdapter {
    List<String> command(Path platform, Path worktree, String prompt);
}
