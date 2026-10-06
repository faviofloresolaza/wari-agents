package wari.agents.provider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CodexAdapter implements ProviderAdapter {
    @Override public List<String> command(Path platform, Path worktree, String prompt) {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "codex.cmd" : "codex";
        List<String> command = new ArrayList<>(List.of(executable, "-C", platform.toString()));
        if (worktree != null) command.addAll(List.of("--sandbox", "workspace-write", "--add-dir", worktree.toString()));
        command.add(prompt);
        return command;
    }
}
