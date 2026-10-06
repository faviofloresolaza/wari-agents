# WARI Agents Runner v0.3

Java 21, CLI local, sin servidor, base de datos ni dependencias externas.
El JAR en este directorio se recompila de forma reproducible con
`bin/build-runner.sh` o `bin\build-runner.cmd`. No contiene WARI ni
credenciales; las entradas usan una fecha fija para evitar cambios binarios
cuando las clases no cambian.

El Runner determina `<WARI-APP>` como padre de `wari-agents/`; consulta
únicamente metadatos Git locales y archivos de la HU seleccionada.
`check` valida estructura, `list` muestra worktrees HU, `context <HU>`
entrega JSON compacto, `gate <HU>` verifica identidad, `init <HU>` crea
estado local, `record <HU> --phase ...` actualiza estado, `launch` inicia
el adaptador elegido, y `create <HU> [--branch <BRANCH>] --confirm` crea
rama/worktree solo después de verificar `origin/produccion` vigente.

El trabajo entre roles usa `tasks <HU>` y las operaciones `task-add`,
`task-start`, `task-review`, `task-complete` y `task-block`. El registro
`orchestration/tasks.json` vive dentro del workspace local de la HU. El Runner
valida dependencias, transiciones, evidencia de cierre, máximo de tres tareas
activas, colisiones de archivos/recursos, veinte tareas sin revisión y dos
ciclos automáticos de corrección. Las tareas DONE son inmutables.

`state.json` usa `schemaVersion: 1` y campos mecánicos `hu`, `branch`,
`worktree`, `workspace`, `phase`, `lastAgent`, `lastStep`, `nextAction`,
`blockers`, `evidenceRefs`, `artifacts`, `gitHead`, `updatedAt`. No almacena
autorización persistente, contenido completo de fuentes ni credenciales.
Los paths son relativos a `<WARI-APP>`; el estado vive exclusivamente en
`<worktree>/workspace/<HU>/`. Las escrituras usan lock por HU y reemplazo
atómico. `headChangedSinceState` solo detecta cambio de commit, no
cambios de diff con el mismo HEAD ni vigencia de Jira/Confluence.

Las operaciones gestionadas por el Runner revalidan la identidad. El
adaptador de proveedor reside en `runner/src/wari/agents/provider/` y
`providers/default-provider.txt`; por ahora solo Codex está disponible.
El Runner no intercepta herramientas que el proveedor ejecute directamente.
Esa frontera requiere certificación específica del proveedor.
