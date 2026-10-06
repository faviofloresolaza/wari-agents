# WARI Agents v0.3 — candidato para revisión

WARI Agents ayuda a desarrolladores de WARI mediante lenguaje natural.
JARVIS interpreta la intención y coordina análisis (AN), desarrollo y
revisión (DEV), validación independiente (QA), documentación basada en
evidencia (DOC) e integración controlada (INT). El desarrollador no necesita
conocer esos nombres.

La plataforma es independiente del Git de WARI:

    WARI-APP/
    ├── wari-agents/          plataforma y Runner
    ├── wari-fortalecimiento/ fuentes WARI
    └── worktrees/<HU>/      rama y estado local de cada HU

`WARI-APP/` es únicamente la carpeta contenedora. `wari-fortalecimiento/` ya
existe como repositorio de la aplicación; `wari-agents/` es el único repositorio
nuevo. `worktrees/` contiene worktrees del Git de la aplicación y `.idea/`, si
aparece, es configuración local del IDE, no otro repositorio.

Repositorio oficial de la plataforma:
`https://github.com/faviofloresolaza/wari-agents.git`, rama principal `main`.

El [Runner Java](runner/) comprueba raíces, HU, rama, worktree y workspace;
mantiene `workspace/<HU>/state.json` y `orchestration/tasks.json`, lista HUs y
prepara contexto breve. Solo las tareas READY pueden iniciar; las correcciones
son tareas hijas acotadas por límites anti-loop.
No decide requisitos, alcance funcional ni documentación. Las decisiones
siguen en [JARVIS](orchestrator/AGENT-JARVIS.md) y los agentes. El Runner
solo controla operaciones que pasan por él; las herramientas directas de
Codex requieren certificación específica. Ver [seguridad](rules/GENERAL-RULES.md).

Knowledge confirmado se carga de forma selectiva desde
[knowledge/wari/INDEX.md](knowledge/wari/INDEX.md). Jira, Confluence y Git
son fuentes externas bajo demanda; disponibilidad técnica no autoriza
WRITE. Producción solo admite análisis de lectura desde WARI Agents.
Cada HU mantiene su propio estado y handoffs; no se mezclan sesiones.

## Empezar

Con Java 21, Git, Codex autorizado y los tres directorios hermanos, ejecutar
una vez `bin\install-workspace.cmd` y completar
`../.wari/environment.local.yaml`. Validar con `bin\check-environment.cmd`.
El instalador sincroniza seis entrypoints en `../.codex/skills/` para que
Codex pueda descubrir los skills versionados en `wari-agents/` en cada sesión
nueva, sin duplicar su fuente de verdad.
Después se abre directamente `<WARI-APP>` en Codex: su `AGENTS.md` activa al
ORQUESTADOR al recibir el primer pedido. No hace falta usar Paseo ni invocar
JARVIS. El Runner sigue disponible para diagnóstico y gestión de HU/tareas:

- Windows: `bin\wari-agents.cmd`
- Mac/Linux: `sh bin/wari-agents.sh`

El lanzador opcional pregunta qué se necesita; por ejemplo: «Quiero trabajar en
<HU>», sustituyendo `<HU>` por una HU real autorizada. También acepta
`check`, `list`, `context <HU>` y `init <HU>`
para diagnóstico. `tasks` y `task-*` administran el flujo entre roles. Una rama nueva exige confirmación expresa y
`origin/produccion` vigente. El Runner no hace commit, push ni PR.

El [manual](docs/setup/SETUP.md) explica preparación y autenticación
individual. [CERTIFICATION.md](docs/CERTIFICATION.md) permite a cualquier
certificador registrar PASS/FAIL con evidencia sin leer contratos internos.
La [matriz de validación](MVP-VALIDATION.md) distingue resultados locales
de la certificación externa pendiente.

La distribución física de la carpeta, incluso si alguien la comprime
manualmente, es externa a WARI Agents. El proyecto no genera ni valida ZIP.
Al copiarla a otra estación, conservar `runner/wari-agents-runner.jar` y las
fuentes; excluir `.git/`, `runner/build/` y archivos locales ignorados.
La evidencia histórica de migración no forma parte del funcionamiento
diario y no debe cargarse en cada solicitud.
