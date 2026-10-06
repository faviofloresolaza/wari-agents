package wari.agents;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Isolated Git fixtures; never uses the real WARI repository. */
public final class RunnerTest {
    private static Path jar;
    private static Path home;
    private static Path app;
    private static int passed;

    private record Result(int exit, String output) {}

    private static Result run(Path cwd, String... command) throws Exception {
        Process process = new ProcessBuilder(command).directory(cwd.toFile()).redirectErrorStream(true).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("timeout: " + List.of(command)); }
        return new Result(process.exitValue(), new String(process.getInputStream().readAllBytes()));
    }
    private static Result git(Path cwd, String... args) throws Exception {
        String[] command = new String[args.length + 1]; command[0] = "git";
        System.arraycopy(args, 0, command, 1, args.length);
        Result r = run(cwd, command);
        if (r.exit != 0) throw new AssertionError("fixture Git: " + r.output);
        return r;
    }
    private static Result runner(String... args) throws Exception {
        String[] command = new String[args.length + 4];
        command[0] = "java"; command[1] = "-Dwari.agents.home=" + home;
        command[2] = "-jar"; command[3] = jar.toString();
        System.arraycopy(args, 0, command, 4, args.length);
        return run(home, command);
    }
    private static void expect(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        passed++; System.out.println("PASS " + label);
    }
    private static void setup(Path root) throws Exception {
        app = root.resolve("APP-WARI"); home = app.resolve("wari-agents");
        Path wari = app.resolve("wari-fortalecimiento");
        Path worktrees = app.resolve("worktrees");
        Files.createDirectories(home.resolve("orchestrator"));
        Files.createDirectories(wari); Files.createDirectories(worktrees);
        Files.writeString(home.resolve("AGENTS.md"), "fixture\n");
        Files.writeString(home.resolve("orchestrator/AGENT-JARVIS.md"), "fixture\n");
        Files.createDirectories(home.resolve("agents/qa"));
        Files.writeString(home.resolve("agents/qa/AGENT-QA.md"), "fixture\n");
        Files.createDirectories(home.resolve("providers"));
        Files.writeString(home.resolve("providers/default-provider.txt"), "codex\n");
        git(wari, "init", "-b", "produccion");
        git(wari, "config", "user.name", "Runner Test");
        git(wari, "config", "user.email", "runner@example.invalid");
        Files.writeString(wari.resolve("README.fixture"), "fixture\n");
        git(wari, "add", "README.fixture"); git(wari, "commit", "-m", "fixture");
        Path remote = root.resolve("remote.git");
        Files.createDirectories(remote);
        git(remote, "init", "--bare");
        git(wari, "remote", "add", "origin", remote.toString());
        git(wari, "push", "origin", "produccion");
        git(wari, "worktree", "add", "-b", "MEWARI-9101", worktrees.resolve("MEWARI-9101").toString());
        git(wari, "worktree", "add", "-b", "MEWARI-9102", worktrees.resolve("MEWARI-9102").toString());
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Runner JAR path required");
        jar = Path.of(args[0]).toRealPath();
        Path fixtureParent = jar.getParent().resolve("build");
        Files.createDirectories(fixtureParent);
        Path root = Files.createTempDirectory(fixtureParent, "wari-runner-test-");
        try {
            setup(root);
            Result initialCheck = runner("check");
            expect(initialCheck.exit == 0, "sibling discovery without WARI scan: " + initialCheck.output);
            expect(runner("init", "OTHER-1").exit != 0, "only MEWARI and DWARI keys accepted");
            expect(runner("init", "MEWARI-9101").exit == 0, "init HU A");
            expect(runner("init", "MEWARI-9102").exit == 0, "init HU B");
            Path a = app.resolve("worktrees/MEWARI-9101/workspace/MEWARI-9101");
            Path b = app.resolve("worktrees/MEWARI-9102/workspace/MEWARI-9102");
            expect(Files.isRegularFile(a.resolve("state.json")) && Files.isRegularFile(b.resolve("state.json")), "separate states");
            Files.createDirectories(a.resolve("output/agents/analysis"));
            Files.writeString(a.resolve("output/agents/analysis/ANALYSIS-STATE.md"), "fixture\n");
            expect(runner("record", "MEWARI-9101", "--phase", "analysis", "--agent", "AN", "--next", "Review A").exit == 0, "record A");
            String contextA = runner("context", "MEWARI-9101").output;
            String contextB = runner("context", "MEWARI-9102").output;
            expect(contextA.contains("Review A") && !contextB.contains("Review A"), "no cross-HU state");
            expect(contextA.contains("output/agents/analysis/ANALYSIS-STATE.md") && !contextB.contains("ANALYSIS-STATE.md"), "directed artifact discovery");
            expect(contextA.contains("analysis") && runner("context", "MEWARI-9101").output.contains("Review A"), "resume from new process");
            expect(contextA.contains("QA"), "QA capability advertised");
            expect(runner("task-add", "MEWARI-9101", "--id", "AN-1", "--role", "ANALISTA", "--title", "Analizar HU",
                "--resources", "jira:MEWARI-9101").exit == 0, "create ready analysis task");
            expect(runner("task-add", "MEWARI-9101", "--id", "DEV-1", "--role", "DESARROLLADOR", "--title", "Implementar",
                "--depends", "AN-1", "--resources", "worktree:MEWARI-9101", "--files", "src/Foo.java").exit == 0 &&
                runner("tasks", "MEWARI-9101").output.contains("PENDING"), "dependent task remains pending");
            expect(runner("task-start", "MEWARI-9101", "--id", "DEV-1").exit != 0, "cannot start pending task");
            expect(runner("task-start", "MEWARI-9101", "--id", "AN-1").exit == 0 &&
                runner("task-complete", "MEWARI-9101", "--id", "AN-1", "--evidence", "output/analysis.md").exit == 0,
                "complete task with evidence");
            expect(runner("tasks", "MEWARI-9101").output.contains("\"taskId\":\"DEV-1\"") &&
                runner("tasks", "MEWARI-9101").output.contains("READY"), "dependency completion releases task");
            expect(runner("task-start", "MEWARI-9101", "--id", "DEV-1").exit == 0, "start ready development task");
            expect(runner("task-add", "MEWARI-9101", "--id", "INT-1", "--role", "INTEGRADOR", "--title", "Integrar",
                "--resources", "worktree:MEWARI-9101").exit == 0 &&
                runner("task-start", "MEWARI-9101", "--id", "INT-1").exit != 0, "shared resource blocks parallel task");
            expect(runner("task-complete", "MEWARI-9101", "--id", "DEV-1", "--evidence", "git:diff-reviewed").exit == 0,
                "development task completed");
            expect(runner("task-add", "MEWARI-9101", "--id", "FIX-1", "--role", "DESARROLLADOR", "--title", "Corregir uno",
                "--correction-of", "DEV-1").exit == 0 &&
                runner("task-start", "MEWARI-9101", "--id", "FIX-1").exit == 0 &&
                runner("task-complete", "MEWARI-9101", "--id", "FIX-1", "--evidence", "git:fix-1").exit == 0 &&
                runner("task-add", "MEWARI-9101", "--id", "FIX-2", "--role", "DESARROLLADOR", "--title", "Corregir dos",
                    "--correction-of", "FIX-1").exit == 0 &&
                runner("task-start", "MEWARI-9101", "--id", "FIX-2").exit == 0 &&
                runner("task-complete", "MEWARI-9101", "--id", "FIX-2", "--evidence", "git:fix-2").exit == 0 &&
                runner("task-add", "MEWARI-9101", "--id", "FIX-3", "--role", "DESARROLLADOR", "--title", "Corregir tres",
                    "--correction-of", "FIX-2").exit != 0, "third automatic correction is blocked");
            Path treeA = app.resolve("worktrees/MEWARI-9101");
            Files.writeString(treeA.resolve("manual-new.txt"), "fixture\n");
            expect(runner("context", "MEWARI-9101").output.contains("manual-new.txt"), "new untracked file visible in basic status");
            expect(runner("list").output.contains("MEWARI-9101") && runner("list").output.contains("MEWARI-9102"), "list active HUs");
            expect(runner("gate", "MEWARI-9101", "--branch", "MEWARI-9102").exit != 0, "agreed branch mismatch blocked");
            git(treeA, "checkout", "-b", "MISMATCH-1");
            expect(runner("record", "MEWARI-9101", "--phase", "development").exit != 0, "actual branch mismatch blocks state write");
            expect(Files.readString(a.resolve("state.json")).contains("Review A"), "blocked write preserved state");
            git(treeA, "checkout", "MEWARI-9101");
            expect(runner("gate", "MEWARI-9101", "--destination", "origin/produccion").exit != 0, "production write blocked");
            expect(runner("create", "MEWARI-9103").exit != 0, "branch creation requires confirmation");
            Path wari = app.resolve("wari-fortalecimiento");
            git(wari, "branch", "already-remote");
            git(wari, "push", "origin", "already-remote");
            expect(runner("create", "MEWARI-9104", "--branch", "already-remote", "--confirm").exit != 0 &&
                !Files.exists(app.resolve("worktrees/MEWARI-9104")), "remote branch collision blocked");
            Files.writeString(wari.resolve("README.fixture"), "updated base\n");
            git(wari, "add", "README.fixture"); git(wari, "commit", "-m", "updated base");
            git(wari, "push", "origin", "produccion");
            String currentBase = git(wari, "rev-parse", "HEAD").output.trim();
            expect(runner("create", "MEWARI-9103", "--branch", "manual-name", "--confirm").exit == 0,
                "explicit branch created from updated remote base");
            Path treeC = app.resolve("worktrees/MEWARI-9103");
            expect(git(treeC, "rev-parse", "HEAD").output.trim().equals(currentBase), "new branch uses updated origin/produccion");
            expect(runner("init", "MEWARI-9103", "--branch", "manual-name").exit == 0, "custom branch state bound to HU");
            try (FileChannel channel = FileChannel.open(a.resolve(".state.lock"), StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
                expect(runner("record", "MEWARI-9101", "--phase", "locked").exit != 0, "state lock blocks concurrent write");
            }
            expect(runner("launch", "MEWARI-9101", "--dry-run").output.contains("--add-dir"), "Codex adapter dry-run");
            expect(runner("launch", "--dry-run", "--prompt", "Continúa MEWARI-9101").output.contains("--add-dir"),
                "natural prompt selects one HU");
            Result consult = runner("launch", "--dry-run", "--prompt", "Consulta MEWARI-9199");
            expect(consult.exit == 0 && consult.output.contains("No existe worktree") &&
                !Files.exists(app.resolve("worktrees/MEWARI-9199")), "consultation does not create a worktree");
            expect(runner("launch", "--dry-run", "--prompt", "Compara MEWARI-9101 con MEWARI-9102").exit != 0,
                "multiple HUs do not select first silently");
            expect(runner("launch", "MEWARI-9101", "--dry-run", "--prompt", "Continúa MEWARI-9102").exit != 0,
                "prompt cannot switch selected HU");
            expect(runner("record", "MEWARI-9101", "--phase", "analysis", "--evidence", "token value").exit != 0, "secret-like state rejected");
            String validB = Files.readString(b.resolve("state.json"));
            Files.writeString(b.resolve("state.json"), validB.replace("\"schemaVersion\":1", "\"schemaVersion\":1.5"));
            expect(runner("context", "MEWARI-9102").exit != 0, "fractional schema version blocked");
            Files.writeString(b.resolve("state.json"), validB.replace("\"schemaVersion\":1", "\"schemaVersion\":1e999"));
            expect(runner("record", "MEWARI-9102", "--phase", "analysis").exit != 0, "non-finite JSON blocked without overwrite");
            Files.writeString(b.resolve("state.json"), validB);
            for (int i = 0; i < 25; i++) Files.writeString(treeA.resolve(String.format("fixture-%04d.txt", i)), "changed\n");
            Result large = runner("context", "MEWARI-9101");
            expect(large.exit == 0 && large.output.contains("\"gitStatusTruncated\":true"), "large Git status drains without blocking");
            expect(git(treeA, "check-ignore", "workspace/MEWARI-9101/state.json").exit == 0, "generic local HU ignore");
            System.out.println("RESULT PASS " + passed + " checks");
        } finally {
            // Test fixture is temporary and contains no WARI source or corporate data.
            try (var paths = Files.walk(root)) {
                paths.sorted((x, y) -> Integer.compare(y.getNameCount(), x.getNameCount())).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) { }
                });
            }
        }
    }
}
