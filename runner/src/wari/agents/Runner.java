package wari.agents;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import wari.agents.provider.ProviderAdapters;

/** Deterministic local infrastructure. Functional reasoning remains in JARVIS. */
public final class Runner {
    private static final Pattern HU = Pattern.compile("(?:MEWARI|DWARI)-[0-9]+", Pattern.CASE_INSENSITIVE);
    private static final Set<String> AGENTS = Set.of("JARVIS", "AN", "DEV", "QA", "DOC", "INT");
    private static final Set<String> TASK_ROLES = Set.of("ANALISTA", "DESARROLLADOR", "QA", "INTEGRADOR", "DOCUMENTADOR");
    private static final Set<String> TASK_STATES = Set.of("PENDING", "READY", "IN_PROGRESS", "REVIEW", "CORRECTION_REQUIRED", "WAITING_APPROVAL", "BLOCKED", "DONE", "CANCELLED");
    private static final Pattern TASK_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");
    private static final int SCHEMA = 1;
    private static final int MAX_GIT_OUTPUT = 1024 * 1024;
    private final Path platform;
    private final Path app;
    private final Path wari;
    private final Path worktrees;

    private Runner(Path platform) throws IOException {
        this.platform = platform.toRealPath();
        this.app = this.platform.getParent();
        this.wari = app.resolve("wari-fortalecimiento");
        this.worktrees = app.resolve("worktrees");
        if (!this.platform.getFileName().toString().equals("wari-agents")) fail("La plataforma debe llamarse wari-agents");
    }

    public static void main(String[] args) {
        try {
            Path home;
            String override = System.getProperty("wari.agents.home");
            if (override != null) home = Path.of(override);
            else {
                CodeSource source = Runner.class.getProtectionDomain().getCodeSource();
                Path location = Path.of(source.getLocation().toURI()).toAbsolutePath();
                home = Files.isDirectory(location) ? location.getParent().getParent().getParent() : location.getParent().getParent();
            }
            new Runner(home).run(args);
        } catch (Exception e) {
            System.err.println("BLOCKED: " + e.getMessage());
            System.exit(2);
        }
    }

    private void run(String[] args) throws Exception {
        if (args.length == 0) { launch(new String[]{"launch"}); return; }
        switch (args[0]) {
            case "help" -> usage();
            case "check" -> check();
            case "list" -> list();
            case "context" -> { require(args, 2); context(args[1]); }
            case "init" -> { require(args, 2); init(args[1], option(args, "--branch", args[1])); }
            case "gate" -> { require(args, 2); gateCommand(args[1], option(args, "--branch", null), option(args, "--destination", null)); }
            case "record" -> { require(args, 2); record(args[1], args); }
            case "tasks" -> { require(args, 2); tasksCommand(args[1]); }
            case "task-add" -> { require(args, 2); taskAdd(args[1], args); }
            case "task-start" -> { require(args, 2); taskStart(args[1], args); }
            case "task-review" -> { require(args, 2); taskReview(args[1], args); }
            case "task-complete" -> { require(args, 2); taskComplete(args[1], args); }
            case "task-block" -> { require(args, 2); taskBlock(args[1], args); }
            case "create" -> { require(args, 2); create(args[1], option(args, "--branch", args[1]), has(args, "--confirm")); }
            case "launch" -> { launch(args); }
            default -> usage();
        }
    }

    private static void usage() {
        System.out.println("WARI Agents Runner v0.2\n" +
            "check | list | context <HU> | init <HU> [--branch <BRANCH>] | " +
            "gate <HU> [--branch <BRANCH>] [--destination <DEST>] | " +
            "record <HU> --phase <TEXT> [--agent <AGENT>] [--step <TEXT>] [--next <TEXT>] [--block <TEXT>] [--evidence <REF>] | " +
            "tasks <HU> | task-add <HU> --id <ID> --role <ROL> --title <TEXT> [--depends <ID,ID>] [--scope <TEXT>] [--resources <R,R>] [--files <P,P>] [--acceptance <TEXT>] [--evidence-required <TEXT>] [--correction-of <ID>] | " +
            "task-start <HU> --id <ID> | task-review <HU> --id <ID> --evidence <REF> | task-complete <HU> --id <ID> --evidence <REF> | task-block <HU> --id <ID> --reason <TEXT> [--fingerprint <TEXT>] | " +
            "create <HU> [--branch <BRANCH>] --confirm | launch [<HU>] [--dry-run] [--prompt <TEXT>]");
    }

    private static void require(String[] args, int n) {
        if (args.length < n) fail("Falta argumento");
    }
    private static boolean has(String[] args, String name) { return Arrays.asList(args).contains(name); }
    private static String option(String[] args, String name, String fallback) {
        for (int i = 1; i < args.length; i++) if (args[i].equals(name)) {
            if (i + 1 >= args.length || args[i + 1].startsWith("--")) fail("Falta valor para " + name);
            return args[i + 1];
        }
        return fallback;
    }
    private static void fail(String reason) { throw new IllegalStateException(reason); }
    private static String bounded(String value) {
        if (value == null) return "";
        if (value.length() > 512 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) fail("Valor demasiado largo o multilineal");
        if (Pattern.compile("(?i)(password|token|secret|api[_ -]?key|cookie|bearer)").matcher(value).find())
            fail("No persistir posibles credenciales en estado");
        return value;
    }
    private static String hu(String raw) {
        if (!HU.matcher(raw).matches()) fail("HU inválida; se espera MEWARI-<n> o DWARI-<n>");
        return raw;
    }

    private String git(Path root, String... args) throws Exception {
        List<String> command = new ArrayList<>(List.of("git", "-C", root.toString()));
        command.addAll(List.of(args));
        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        AtomicBoolean tooLarge = new AtomicBoolean();
        AtomicReference<IOException> readError = new AtomicReference<>();
        Thread reader = Thread.ofVirtual().start(() -> {
            try (var input = p.getInputStream()) {
                byte[] chunk = new byte[8192];
                int count;
                while ((count = input.read(chunk)) >= 0) {
                    int remaining = MAX_GIT_OUTPUT - captured.size();
                    if (remaining > 0) captured.write(chunk, 0, Math.min(count, remaining));
                    if (count > remaining) tooLarge.set(true);
                }
            } catch (IOException e) { readError.set(e); }
        });
        if (!p.waitFor(30, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            reader.join(1000);
            fail("Git excedió 30 segundos en " + args[0]);
        }
        reader.join();
        if (readError.get() != null) fail("No se pudo leer la salida Git de " + args[0]);
        if (p.exitValue() != 0) fail("Git rechazó " + args[0] + " (exit " + p.exitValue() + "); verificar repo/acceso sin compartir credenciales");
        if (tooLarge.get()) fail("Salida Git demasiado grande en " + args[0] + "; acotar operación");
        String out = captured.toString(java.nio.charset.StandardCharsets.UTF_8).trim();
        return out;
    }
    private void layout() throws Exception {
        if (!Files.isRegularFile(platform.resolve("AGENTS.md")) || !Files.isRegularFile(platform.resolve("orchestrator/AGENT-JARVIS.md")))
            fail("Faltan instrucciones WARI Agents");
        if (!Files.isDirectory(wari) || !Files.isDirectory(worktrees)) fail("Faltan repositorio WARI o worktrees hermanos");
        if (!wari.toRealPath().equals(Path.of(git(wari, "rev-parse", "--show-toplevel")).toRealPath())) fail("WARI no es raíz Git");
    }
    private List<Path> registered() throws Exception {
        String raw = git(wari, "worktree", "list", "--porcelain");
        List<Path> list = new ArrayList<>();
        for (String line : raw.split("\\R")) if (line.startsWith("worktree ")) list.add(Path.of(line.substring(9)).toRealPath());
        return list;
    }
    private Path selected(String id) throws Exception {
        layout();
        Path expected = worktrees.resolve(hu(id)).toAbsolutePath().normalize();
        if (!Files.isDirectory(expected)) fail("No existe worktree de " + id + "; crear requiere confirmación explícita");
        Path actual = expected.toRealPath();
        if (!actual.startsWith(worktrees.toRealPath()) || !actual.equals(expected)) fail("Worktree fuera de la ruta HU esperada");
        if (!registered().contains(actual)) fail("Worktree no registrado en Git WARI");
        if (!actual.equals(Path.of(git(actual, "rev-parse", "--show-toplevel")).toRealPath())) fail("Ruta no es raíz de worktree");
        return actual;
    }
    private Path workspace(Path tree, String id) throws Exception {
        Path path = tree.resolve("workspace").resolve(id);
        if (Files.isSymbolicLink(tree.resolve("workspace"))) fail("Workspace padre es enlace simbólico");
        if (Files.exists(path) && (!Files.isDirectory(path) || !path.toRealPath().equals(path.toAbsolutePath().normalize())))
            fail("Workspace inválido o enlace simbólico");
        return path;
    }
    private String branch(Path tree) throws Exception {
        String name = git(tree, "branch", "--show-current");
        if (name.isBlank() || name.equals("produccion")) fail("HEAD detached o rama produccion: WRITE bloqueado");
        return name;
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> state(Path space, String id) throws Exception {
        Path file = space.resolve("state.json");
        if (!Files.exists(file)) return null;
        if (Files.isSymbolicLink(file) || Files.size(file) > 65536) fail("state.json enlazado o demasiado grande");
        Object data = Json.parse(Files.readString(file));
        if (!(data instanceof Map<?, ?>)) fail("state.json inválido");
        Map<String, Object> state = (Map<String, Object>) data;
        if (!(state.get("schemaVersion") instanceof Number n) || n.doubleValue() != SCHEMA || !id.equals(state.get("hu")))
            fail("state.json de otra HU o versión incompatible");
        String expectedTree = "worktrees/" + id;
        String expectedSpace = expectedTree + "/workspace/" + id;
        if (!expectedTree.equals(state.get("worktree")) || !expectedSpace.equals(state.get("workspace")))
            fail("state.json apunta a otro worktree/workspace");
        if (!(state.get("branch") instanceof String)) fail("state.json sin rama acordada");
        return state;
    }
    private record Selection(Path tree, Path space, String branch, Map<String, Object> state) {}
    private Selection gate(String id, String requestedBranch) throws Exception {
        Path tree = selected(id);
        Path space = workspace(tree, id);
        Map<String, Object> s = state(space, id);
        String agreed = s == null ? (requestedBranch == null ? id : requestedBranch) : (String)s.get("branch");
        if (requestedBranch != null && !requestedBranch.equals(agreed)) fail("Rama solicitada distinta de la acordada para HU");
        if (!agreed.equals(branch(tree))) fail("HU/rama/worktree/workspace no coinciden: WRITE bloqueado");
        return new Selection(tree, space, agreed, s);
    }
    private void check() throws Exception {
        layout();
        System.out.println("PASS APP-WARI=" + app + "\nPASS platform=" + platform + "\nPASS WARI=" + wari + "\nPASS worktrees=" + worktrees);
    }
    private void list() throws Exception {
        layout();
        for (Path tree : registered()) {
            if (!tree.startsWith(worktrees.toRealPath()) || tree.equals(wari.toRealPath())) continue;
            String id = tree.getFileName().toString();
            if (!HU.matcher(id).matches()) continue;
            try {
                Selection s = gate(id, null);
                System.out.println(id + " | " + s.branch + " | " + (s.state == null ? "sin state.json" : s.state.getOrDefault("phase", "sin fase")));
            } catch (Exception ex) { System.out.println(id + " | BLOCKED: " + ex.getMessage()); }
        }
    }
    private void context(String id) throws Exception {
        Selection s = gate(id, null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("hu", id); out.put("branch", s.branch);
        out.put("worktree", "worktrees/" + id);
        out.put("workspace", "worktrees/" + id + "/workspace/" + id);
        String status = git(s.tree, "status", "--short", "--untracked-files=normal");
        List<String> statusLines = status.isBlank() ? List.of() : List.of(status.split("\\R"));
        out.put("gitChangedCount", statusLines.size());
        out.put("gitStatusSample", statusLines.stream().limit(20).toList());
        out.put("gitStatusTruncated", statusLines.size() > 20);
        String head = git(s.tree, "rev-parse", "HEAD");
        out.put("gitHead", head);
        out.put("headChangedSinceState", s.state != null && s.state.containsKey("gitHead") && !head.equals(s.state.get("gitHead")));
        out.put("state", s.state == null ? Map.of("phase", "new") : s.state);
        out.put("artifactsPresent", artifacts(s.space));
        List<String> capabilities = new ArrayList<>(List.of("JARVIS", "Git local"));
        for (String[] item : new String[][]{{"AN", "agents/analysis/AGENT-AN.md"}, {"DEV", "agents/development/AGENT-DEV.md"},
                {"QA", "agents/qa/AGENT-QA.md"}, {"DOC", "agents/doc/AGENT-DOC.md"}, {"INT", "agents/integration/AGENT-INT.md"}})
            if (Files.isRegularFile(platform.resolve(item[1]))) capabilities.add(item[0]);
        out.put("capabilities", capabilities);
        out.put("knowledgeIndex", "knowledge/wari/INDEX.md");
        out.put("externalIntegrations", "not checked by Runner");
        System.out.println(Json.write(out));
    }
    private void localIgnore() throws Exception {
        String raw = git(wari, "rev-parse", "--git-path", "info/exclude");
        Path file = Path.of(raw);
        if (!file.isAbsolute()) file = wari.resolve(file);
        file = file.normalize();
        String text = Files.exists(file) ? Files.readString(file) : "";
        if (!Arrays.asList(text.split("\\R")).contains("/workspace/*/")) {
            Files.createDirectories(file.getParent());
            Files.writeString(file, (text.isEmpty() || text.endsWith("\n") ? "" : "\n") + "\n# WARI Agents local HU state\n/workspace/*/\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }
    private Map<String, Object> fresh(String id, String branch) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("schemaVersion", SCHEMA); s.put("hu", id); s.put("branch", branch);
        s.put("worktree", "worktrees/" + id); s.put("workspace", "worktrees/" + id + "/workspace/" + id);
        s.put("phase", "new"); s.put("lastAgent", ""); s.put("lastStep", ""); s.put("nextAction", "");
        s.put("blockers", List.of()); s.put("evidenceRefs", List.of()); s.put("artifacts", List.of());
        s.put("updatedAt", Instant.now().toString());
        return s;
    }
    private void save(Selection selection, Map<String, Object> state, boolean merge) throws Exception {
        Path space = selection.space;
        String id = (String)state.get("hu");
        if (!selection.space.equals(gate(id, selection.branch).space)) fail("Workspace cambió antes de WRITE");
        Files.createDirectories(space);
        Path lockPath = space.resolve(".state.lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            if (lock == null) fail("Otra sesión escribe estado de esta HU");
            if (!selection.space.equals(gate(id, selection.branch).space)) fail("Workspace cambió durante WRITE");
            Map<String, Object> effective = state;
            if (merge) {
                Map<String, Object> current = state(space, (String)state.get("hu"));
                if (current == null) fail("Estado desapareció durante la operación");
                effective = new LinkedHashMap<>(current);
                effective.putAll(state);
            } else if (Files.exists(space.resolve("state.json"))) {
                fail("Estado ya existe; no sobrescribir durante init");
            }
            Path tmp = Files.createTempFile(space, ".state-", ".tmp");
            try {
                byte[] bytes = (Json.write(effective) + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                try (FileChannel out = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    while (buffer.hasRemaining()) out.write(buffer);
                    out.force(true);
                }
                Files.move(tmp, space.resolve("state.json"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } finally { Files.deleteIfExists(tmp); }
        }
    }
    private void init(String id, String requested) throws Exception {
        hu(id);
        Selection s = gate(id, requested);
        localIgnore();
        if (s.state == null) {
            save(s, fresh(id, s.branch), false);
            System.out.println("PASS workspace/state creados para " + id);
        } else System.out.println("PASS estado existente conservado para " + id);
    }
    private void gateCommand(String id, String requested, String destination) throws Exception {
        if (destination != null && (destination.equalsIgnoreCase("produccion") || destination.toLowerCase().endsWith("/produccion")))
            fail("Production Boundary: WRITE hacia produccion prohibido");
        Selection s = gate(id, requested);
        System.out.println("PASS HU=" + id + " BRANCH=" + s.branch + " WORKTREE=" + s.tree + " WORKSPACE=" + s.space);
    }
    private void record(String id, String[] args) throws Exception {
        Selection s = gate(id, option(args, "--branch", null));
        if (s.state == null) fail("Falta state.json; ejecute init primero");
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("hu", id);
        String phase = option(args, "--phase", null);
        if (phase == null) fail("record requiere --phase");
        update.put("phase", bounded(phase));
        String agent = option(args, "--agent", null);
        if (agent != null) { if (!AGENTS.contains(agent)) fail("Agente inválido"); update.put("lastAgent", agent); }
        for (String[] pair : new String[][]{{"--step","lastStep"},{"--next","nextAction"}}) {
            String value = option(args, pair[0], null); if (value != null) update.put(pair[1], bounded(value));
        }
        String block = option(args, "--block", null);
        if (block != null) update.put("blockers", block.isEmpty() ? List.of() : List.of(bounded(block)));
        String evidence = option(args, "--evidence", null);
        if (evidence != null) update.put("evidenceRefs", List.of(bounded(evidence)));
        update.put("artifacts", artifacts(s.space));
        update.put("gitHead", git(s.tree, "rev-parse", "HEAD"));
        update.put("updatedAt", Instant.now().toString());
        gate(id, s.branch);
        save(s, update, true);
        System.out.println("PASS estado actualizado para " + id);
    }

    private static String taskId(String raw) {
        if (raw == null || !TASK_ID.matcher(raw).matches()) fail("ID de tarea inválido");
        return raw;
    }
    private static List<String> csv(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        List<String> values = new ArrayList<>();
        for (String item : raw.split(",")) {
            String value = bounded(item.trim());
            if (!value.isBlank() && !values.contains(value)) values.add(value);
        }
        return values;
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> taskRegistry(Selection selection, String id) throws Exception {
        Path file = selection.space.resolve("orchestration/tasks.json");
        if (!Files.exists(file)) {
            Map<String, Object> fresh = new LinkedHashMap<>();
            fresh.put("schemaVersion", 1); fresh.put("hu", id);
            fresh.put("tasks", new ArrayList<Map<String, Object>>());
            fresh.put("updatedAt", Instant.now().toString());
            return fresh;
        }
        if (Files.isSymbolicLink(file) || Files.size(file) > 1024 * 1024) fail("tasks.json enlazado o demasiado grande");
        Object parsed = Json.parse(Files.readString(file));
        if (!(parsed instanceof Map<?, ?>)) fail("tasks.json inválido");
        Map<String, Object> registry = (Map<String, Object>)parsed;
        if (!(registry.get("schemaVersion") instanceof Number n) || n.doubleValue() != 1 || !id.equals(registry.get("hu")))
            fail("tasks.json de otra HU o versión incompatible");
        if (!(registry.get("tasks") instanceof List<?>)) fail("tasks.json sin lista de tareas");
        return registry;
    }
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> taskList(Map<String, Object> registry) {
        return (List<Map<String, Object>>)(List<?>)registry.get("tasks");
    }
    private static Map<String, Object> findTask(Map<String, Object> registry, String id) {
        for (Map<String, Object> task : taskList(registry)) if (id.equals(task.get("taskId"))) return task;
        fail("Tarea no encontrada: " + id); return null;
    }
    private static boolean allDone(Map<String, Object> registry, List<String> dependencies) {
        for (String dependency : dependencies) {
            Map<String, Object> found = null;
            for (Map<String, Object> task : taskList(registry)) if (dependency.equals(task.get("taskId"))) found = task;
            if (found == null || !"DONE".equals(found.get("state"))) return false;
        }
        return true;
    }
    @SuppressWarnings("unchecked")
    private static void refreshReady(Map<String, Object> registry) {
        for (Map<String, Object> task : taskList(registry)) {
            if (!"PENDING".equals(task.get("state"))) continue;
            List<String> dependencies = (List<String>)task.getOrDefault("dependencies", List.of());
            if (allDone(registry, dependencies)) task.put("state", "READY");
        }
    }
    private void saveTasks(Selection selection, Map<String, Object> registry) throws Exception {
        String id = (String)registry.get("hu");
        if (!selection.space.equals(gate(id, selection.branch).space)) fail("Workspace cambió antes de WRITE de tareas");
        Path directory = selection.space.resolve("orchestration");
        Files.createDirectories(directory);
        Path lockPath = directory.resolve(".tasks.lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            if (lock == null) fail("Otra sesión escribe tareas de esta HU");
            if (!selection.space.equals(gate(id, selection.branch).space)) fail("Workspace cambió durante WRITE de tareas");
            registry.put("updatedAt", Instant.now().toString());
            Path tmp = Files.createTempFile(directory, ".tasks-", ".tmp");
            try {
                byte[] bytes = (Json.write(registry) + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                try (FileChannel out = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    while (buffer.hasRemaining()) out.write(buffer);
                    out.force(true);
                }
                Files.move(tmp, directory.resolve("tasks.json"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } finally { Files.deleteIfExists(tmp); }
        }
    }
    private void tasksCommand(String id) throws Exception {
        Selection selection = gate(id, null);
        Map<String, Object> registry = taskRegistry(selection, id);
        refreshReady(registry);
        System.out.println(Json.write(registry));
    }
    @SuppressWarnings("unchecked")
    private void taskAdd(String id, String[] args) throws Exception {
        Selection selection = gate(id, option(args, "--branch", null));
        if (selection.state == null) fail("Falta state.json; ejecute init primero");
        Map<String, Object> registry = taskRegistry(selection, id);
        List<Map<String, Object>> tasks = taskList(registry);
        if (tasks.size() >= 20) fail("Límite de 20 tareas alcanzado; requiere revisión humana");
        String taskId = taskId(option(args, "--id", null));
        for (Map<String, Object> task : tasks) if (taskId.equals(task.get("taskId"))) fail("La tarea ya existe: " + taskId);
        String role = option(args, "--role", null);
        if (role == null) fail("task-add requiere --role");
        role = role.toUpperCase(Locale.ROOT);
        if (!TASK_ROLES.contains(role)) fail("Rol de tarea inválido");
        String title = bounded(option(args, "--title", null));
        if (title.isBlank()) fail("task-add requiere --title");
        List<String> dependencies = new ArrayList<>(csv(option(args, "--depends", null)));
        for (String dependency : dependencies) findTask(registry, taskId(dependency));
        String correctionOf = option(args, "--correction-of", null);
        String correctionRoot = null;
        int correctionCycle = 0;
        if (correctionOf != null) {
            Map<String, Object> parent = findTask(registry, taskId(correctionOf));
            correctionRoot = (String)parent.getOrDefault("correctionRoot", parent.get("taskId"));
            for (Map<String, Object> task : tasks) if (correctionRoot.equals(task.get("correctionRoot"))) correctionCycle++;
            if (correctionCycle >= 2) fail("Límite de dos ciclos automáticos de corrección; requiere revisión humana");
            correctionCycle++;
            if (!dependencies.contains(correctionOf)) dependencies.add(correctionOf);
        }
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskId", taskId); task.put("hu", id); task.put("role", role);
        task.put("title", title); task.put("dependencies", dependencies);
        task.put("scope", bounded(option(args, "--scope", "")));
        task.put("resources", csv(option(args, "--resources", null)));
        task.put("files", csv(option(args, "--files", null)));
        task.put("acceptanceCriteria", bounded(option(args, "--acceptance", "")));
        task.put("evidenceRequired", bounded(option(args, "--evidence-required", "")));
        task.put("state", allDone(registry, dependencies) ? "READY" : "PENDING");
        task.put("createdAt", Instant.now().toString());
        task.put("updatedAt", Instant.now().toString());
        task.put("evidence", List.of());
        if (correctionOf != null) {
            task.put("correctionOf", correctionOf); task.put("correctionRoot", correctionRoot);
            task.put("correctionCycle", correctionCycle);
        }
        tasks.add(task);
        saveTasks(selection, registry);
        System.out.println("PASS tarea " + taskId + " creada en " + task.get("state"));
    }
    @SuppressWarnings("unchecked")
    private void taskStart(String id, String[] args) throws Exception {
        Selection selection = gate(id, option(args, "--branch", null));
        Map<String, Object> registry = taskRegistry(selection, id);
        refreshReady(registry);
        Map<String, Object> target = findTask(registry, taskId(option(args, "--id", null)));
        if (!"READY".equals(target.get("state"))) fail("Solo una tarea READY puede iniciar");
        int active = 0;
        Set<String> resources = new LinkedHashSet<>((List<String>)target.getOrDefault("resources", List.of()));
        Set<String> files = new LinkedHashSet<>((List<String>)target.getOrDefault("files", List.of()));
        for (Map<String, Object> task : taskList(registry)) {
            if (!"IN_PROGRESS".equals(task.get("state"))) continue;
            active++;
            Set<String> otherResources = new LinkedHashSet<>((List<String>)task.getOrDefault("resources", List.of()));
            Set<String> otherFiles = new LinkedHashSet<>((List<String>)task.getOrDefault("files", List.of()));
            if (resources.stream().anyMatch(otherResources::contains) || files.stream().anyMatch(otherFiles::contains))
                fail("Conflicto de recurso con tarea activa " + task.get("taskId"));
        }
        if (active >= 3) fail("Máximo de tres tareas activas alcanzado");
        target.put("state", "IN_PROGRESS"); target.put("startedAt", Instant.now().toString());
        target.put("updatedAt", Instant.now().toString());
        saveTasks(selection, registry);
        System.out.println("PASS tarea " + target.get("taskId") + " iniciada");
    }
    private void taskReview(String id, String[] args) throws Exception {
        taskTerminal(id, args, "REVIEW", false);
    }
    private void taskComplete(String id, String[] args) throws Exception {
        taskTerminal(id, args, "DONE", true);
    }
    @SuppressWarnings("unchecked")
    private void taskTerminal(String id, String[] args, String nextState, boolean refresh) throws Exception {
        if (!TASK_STATES.contains(nextState)) fail("Estado de tarea inválido");
        Selection selection = gate(id, option(args, "--branch", null));
        Map<String, Object> registry = taskRegistry(selection, id);
        Map<String, Object> target = findTask(registry, taskId(option(args, "--id", null)));
        String current = (String)target.get("state");
        if (!("IN_PROGRESS".equals(current) || ("DONE".equals(nextState) && "REVIEW".equals(current))))
            fail("Transición de tarea no permitida: " + current + " -> " + nextState);
        String evidence = bounded(option(args, "--evidence", null));
        if (evidence.isBlank()) fail("La transición requiere --evidence");
        List<String> values = new ArrayList<>((List<String>)target.getOrDefault("evidence", List.of()));
        values.add(evidence); target.put("evidence", values); target.put("state", nextState);
        target.put("updatedAt", Instant.now().toString());
        if ("DONE".equals(nextState)) target.put("completedAt", Instant.now().toString());
        if (refresh) refreshReady(registry);
        saveTasks(selection, registry);
        System.out.println("PASS tarea " + target.get("taskId") + " -> " + nextState);
    }
    private void taskBlock(String id, String[] args) throws Exception {
        Selection selection = gate(id, option(args, "--branch", null));
        Map<String, Object> registry = taskRegistry(selection, id);
        Map<String, Object> target = findTask(registry, taskId(option(args, "--id", null)));
        if (!("READY".equals(target.get("state")) || "IN_PROGRESS".equals(target.get("state")) || "REVIEW".equals(target.get("state"))))
            fail("Solo una tarea activa o lista puede bloquearse");
        String reason = bounded(option(args, "--reason", null));
        if (reason.isBlank()) fail("task-block requiere --reason");
        String fingerprint = bounded(option(args, "--fingerprint", ""));
        target.put("state", "BLOCKED"); target.put("blockReason", reason);
        if (!fingerprint.isBlank()) target.put("failureFingerprint", fingerprint);
        target.put("updatedAt", Instant.now().toString());
        long repeated = fingerprint.isBlank() ? 0 : taskList(registry).stream().filter(t -> fingerprint.equals(t.get("failureFingerprint"))).count();
        if (repeated >= 2) target.put("requiresHumanReview", true);
        saveTasks(selection, registry);
        System.out.println("PASS tarea " + target.get("taskId") + " -> BLOCKED" + (repeated >= 2 ? "; falla repetida requiere revisión humana" : ""));
    }
    private List<String> artifacts(Path space) throws Exception {
        Path output = space.resolve("output");
        if (!Files.isDirectory(output) || Files.isSymbolicLink(output)) return List.of();
        try (var paths = Files.walk(output, 5)) {
            return paths.filter(Files::isRegularFile).filter(p -> !Files.isSymbolicLink(p))
                .limit(50).map(p -> space.relativize(p).toString().replace('\\', '/')).sorted().toList();
        }
    }
    private void create(String id, String name, boolean confirmed) throws Exception {
        hu(id); layout();
        if (name.equalsIgnoreCase("produccion") || name.toLowerCase().endsWith("/produccion"))
            fail("Production Boundary: rama de destino prohibida");
        System.out.println("Nueva rama: " + name + "\nBase: origin/produccion\nHU/worktree: " + id);
        if (!confirmed) fail("Confirmación requerida para crear; repetir con --confirm tras revisar la propuesta");
        git(wari, "check-ref-format", "--branch", name);
        Path target = worktrees.resolve(id);
        if (Files.exists(target)) fail("Worktree ya existe; no sobrescribir");
        if (!git(wari, "ls-remote", "origin", "refs/heads/" + name).isBlank())
            fail("La rama ya existe en origin; no crear otra con el mismo nombre");
        String remote = git(wari, "ls-remote", "origin", "refs/heads/produccion");
        if (remote.isBlank()) fail("No se pudo verificar origin/produccion");
        git(wari, "fetch", "origin", "refs/heads/produccion:refs/remotes/origin/produccion");
        String hash = git(wari, "rev-parse", "origin/produccion");
        if (!remote.startsWith(hash + "\t")) fail("origin/produccion cambió; reintentar tras revisión");
        if (git(wari, "branch", "--list", name).length() > 0) fail("Rama local ya existe; no sobrescribir");
        git(wari, "worktree", "add", "-b", name, target.toString(), "origin/produccion");
        System.out.println("PASS worktree " + target + " rama " + name + " base " + hash + "; ejecute init tras validar");
    }
    private void launch(String[] args) throws Exception {
        layout();
        boolean dry = has(args, "--dry-run");
        String id = args.length > 1 && !args[1].startsWith("--") ? args[1] : null;
        String prompt = option(args, "--prompt", null);
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        if (id == null && prompt == null && !dry) {
            System.out.print("¿En qué te puedo ayudar? ");
            prompt = input.readLine();
            if (prompt == null || prompt.isBlank()) fail("Solicitud vacía");
        }
        if (prompt == null) prompt = id == null ? "¿En qué me puedes ayudar?" : "Quiero trabajar en " + id + ".";
        var match = HU.matcher(prompt);
        Set<String> mentioned = new LinkedHashSet<>();
        while (match.find()) mentioned.add(match.group().toUpperCase(Locale.ROOT));
        if (mentioned.size() > 1) fail("La solicitud menciona varias HUs; iniciar una sesión por HU");
        if (id == null && !mentioned.isEmpty()) id = mentioned.iterator().next();
        if (id != null && !mentioned.isEmpty() && !mentioned.contains(id.toUpperCase(Locale.ROOT)))
            fail("La HU del prompt no coincide con la sesión seleccionada");
        String provider = option(args, "--provider", Files.readString(platform.resolve("providers/default-provider.txt")).trim());
        Path tree = null;
        if (id != null) {
            if (Files.isDirectory(worktrees.resolve(id))) {
                Selection s = gate(id, null);
                tree = s.tree;
                prompt += " Usa el contexto mínimo de Runner: java -jar runner/wari-agents-runner.jar context " + id + ".";
            } else {
                prompt += " No existe worktree para " + id + ". Puedes atender consultas con fuentes disponibles; antes de escribir, muestra rama y base origin/produccion, solicita confirmación y usa Runner create/init. No inventes estado local de la HU.";
            }
        }
        List<String> command = ProviderAdapters.select(provider).command(platform, tree, prompt);
        if (dry) { System.out.println(Json.write(command)); return; }
        Process p = new ProcessBuilder(command).directory(platform.toFile()).inheritIO().start();
        System.exit(p.waitFor());
    }
}
