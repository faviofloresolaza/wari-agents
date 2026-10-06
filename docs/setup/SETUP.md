# Manual de Preparación de una Estación WARI

## Estructura y requisitos

    <WARI-APP>/
    ├── AGENTS.md             activación automática de Codex
    ├── .wari/environment.local.yaml
    ├── .codex/config.toml    opcional; integración revisada por usuario
    ├── .codex/skills/        skills WARI descubiertos por Codex
    ├── wari-agents/
    ├── wari-fortalecimiento/  Git WARI
    └── worktrees/<HU>/       worktrees del mismo Git WARI

`<WARI-APP>` puede tener cualquier ruta física. Cada desarrollador obtiene
la carpeta `wari-agents/` mediante el mecanismo acordado por el equipo;
WARI Agents no genera paquetes. No copiar agentes dentro del Git WARI.
La raíz `<WARI-APP>` no es un repositorio: el único repositorio nuevo es
`wari-agents/`. `wari-fortalecimiento/` conserva su Git existente y
`worktrees/` contiene worktrees vinculados a ese mismo Git. Carpetas del IDE
como `.idea/` son metadata local y no forman parte de esta arquitectura.

Requisitos operativos: Java 21, Git y proveedor IA Codex instalado y
autorizado para esta certificación. Python no es necesario para uso diario.
Claude Code aún no tiene adaptador certificado. El uso del Runner requiere
Java 21; compilar fuentes o ejecutar `test-runner` requiere JDK 21.
El JAR `runner/wari-agents-runner.jar` está incluido para uso.
Comprobar `java -version` (debe indicar 21), `javac -version` si se
ejecutarán pruebas deterministas (debe indicar 21), `git --version` y
`codex --version`. Si falta alguno, registrar BLOCKED e instalarlo por
el procedimiento corporativo autorizado; la carpeta entregada no incluye
Java, Git ni Codex.

## Primera preparación sin asistencia

1. Crear una carpeta local de trabajo, por ejemplo `WARI-APP`, y clonar dentro
   el repositorio de agentes:
   `git clone https://github.com/faviofloresolaza/wari-agents.git wari-agents`.
   Debe existir `WARI-APP/wari-agents/AGENTS.md`. La rama principal es `main`.
2. Si todavía no se tiene WARI local y se cuenta con acceso corporativo,
   abrir terminal en `WARI-APP` y ejecutar
   `git clone https://github.com/bvlperu/wari-fortalecimiento wari-fortalecimiento`.
   Si el acceso privado falla, registrar BLOCKED y solicitar acceso por
   el canal corporativo; WARI Agents no proporciona credenciales.
3. Crear la carpeta hermana `worktrees/` (`mkdir worktrees`). No copiar
   allí fuentes de otra HU; el Runner usará Git worktrees del clon WARI.
4. En Windows, desde `wari-agents/`, ejecutar
   `bin\install-workspace.cmd`. El instalador crea únicamente la configuración
   faltante, preserva los archivos locales existentes y sincroniza en
   `.codex/skills/` seis entrypoints administrados hacia los skills versionados
   en `wari-agents/`. Completar después
   `<WARI-APP>/.wari/environment.local.yaml`; nunca escribir credenciales.
5. Ejecutar `bin\check-environment.cmd`. Debe informar PASS cuando las rutas,
   versiones, GitHub Enterprise y `clean compile` estén declarados. Un valor
   requerido ausente produce `BLOCKED_ENVIRONMENT`; un WARN bloquea solo la
   tarea que necesite el recurso advertido.
6. Abrir `<WARI-APP>` (la raíz, no solo `wari-agents/`) en Codex. Comprobar
   también `bin\wari-agents.cmd check` desde `wari-agents/`; debe informar PASS
   para plataforma, WARI y worktrees. Los skills sincronizados se descubren al
   abrir una sesión nueva; reiniciar la sesión si se ejecutó el instalador con
   Codex ya abierto.
7. Autenticar Codex con la identidad propia según política corporativa.
   La integración Atlassian se autentica aparte cuando esté autorizada.
   Una ausencia de permisos se registra como BLOCKED, no se sustituye
   copiando sesiones de otra persona.

Para Codex usar `codex login` y seguir el flujo permitido por la
organización. Para Atlassian, comprobar primero `codex mcp list`; si el
servidor `atlassian` está presente y el acceso fue autorizado, ejecutar
`codex mcp login atlassian` y completar el login local. Si falta permiso o
el método de autenticación, registrar BLOCKED. Nunca copiar códigos,
cookies, tokens o credenciales a WARI Agents.
Si el navegador corporativo impide el login y la organización lo
autoriza, Codex ofrece `codex login --device-auth` o
`codex login --with-api-key` (la clave se introduce por stdin, nunca como
argumento ni en este repositorio). La alternativa autentica al proveedor
Codex; no sustituye el OAuth Atlassian. Revisar y confiar en la
configuración compartida `.codex/config.toml` solo después de verificar
que pertenece a la carpeta recibida. La plantilla
`wari-agents/templates/workspace/.codex/config.toml` no se instala
automáticamente: copiarla a la raíz, revisar y reiniciar Codex únicamente
cuando se vaya a confiar y autenticar Atlassian. Un MCP no autenticado no debe
impedir el uso local del Runner y Git.

No hay que compilar el Runner para `check`, `list`, `context` o `launch`:
el JAR ejecutable viaja dentro de `wari-agents/runner/`. La compilación
solo se necesita para mantenimiento o para el test técnico, que la
ejecuta internamente.

## Iniciar

Abrir directamente `<WARI-APP>` como workspace de Codex y escribir el pedido en
lenguaje natural. El `AGENTS.md` raíz convierte la sesión principal en
ORQUESTADOR al recibir ese primer mensaje; no se requiere JARVIS, Paseo ni un
comando de lanzamiento. Una sesión recién abierta carga contexto, pero no crea
tareas, agentes, ramas ni worktrees hasta recibir el pedido.

El lanzador del Runner se conserva como alternativa y diagnóstico:

- Windows: `bin\wari-agents.cmd`
- Mac/Linux: `sh bin/wari-agents.sh`

El Runner pregunta por la solicitud natural. Ejemplos: «¿En qué me puedes
ayudar?», «Quiero trabajar en <HU>», «Continúa <HU>». Sustituir `<HU>`
por una HU real autorizada con identificador numérico; no escribir el
marcador literalmente.
Si se identifica una HU existente, valida el worktree Git y rama acordada,
añade solo ese worktree a la sesión Codex y pasa a JARVIS la instrucción
para consultar el contexto mínimo. Si no hay HU, abre una consulta general.

Si falta el worktree, la consulta puede comenzar con las fuentes disponibles
sin crear rama. Antes de escribir, JARVIS debe mostrar la rama `<HU>` y la
base `origin/produccion` y pedir confirmación; `create <HU>` muestra la
propuesta y `create <HU> --confirm` verifica/actualiza la referencia remota
antes de crear. Sin acceso remoto bloquea. Para rama con nombre
explícito distinto de la HU, usar `create <HU> --branch <BRANCH> --confirm`
solo después de revisar nombre/base y autorizar la creación; después
`init <HU> --branch <BRANCH>`. No se inventan prefijos.

`init` crea `workspace/<HU>/state.json` solo para un worktree/rama válidos
y asegura la exclusión local genérica `/workspace/*/` en el Git WARI.
`context <HU>` y `list` son lecturas; `gate <HU>` comprueba identidad.
`record` actualiza únicamente estado mecánico cuando la tarea autoriza
persistencia. `tasks` lista el registro; `task-add`, `task-start`,
`task-review`, `task-complete` y `task-block` gestionan trabajo entre roles.
Los handoffs analíticos siguen siendo Markdown de la HU.
No crear estado por una mera consulta.

## Identidad y credenciales

Cada desarrollador autentica Codex e integraciones con su propia identidad.
No guardar contraseñas, tokens, cookies ni sesiones en Git, Knowledge,
`state.json` o documentos. `.codex/config.toml` contiene únicamente
configuración compartible de Atlassian READ; la sesión OAuth es local.
Consultar `codex mcp list` y autenticar Atlassian solo cuando esté
autorizado. El login de Codex no autentica Atlassian automáticamente.

Codex puede iniciarse con su login autorizado; si la política corporativa
permite usar API key por stdin, mantenerla fuera de comandos, archivos y
chat. La autenticación es personal, no se distribuye con la carpeta.

## Comprobación y certificación

1. `bin\wari-agents.cmd check` en Windows o
   `sh bin/wari-agents.sh check` en Mac/Linux. No analiza WARI.
2. `list` debe mostrar solo las HUs con worktree registrado; una rama
   incorrecta aparece bloqueada. `context <HU>` entrega JSON breve.
3. Ejecutar [CERTIFICATION.md](../CERTIFICATION.md) y registrar resultado
   real por proveedor/máquina. La prueba desde extensión VS Code multirraíz
   y las escrituras directas del proveedor requieren certificación aparte.

Para reconstruir el JAR desde fuentes:
`bin\build-runner.cmd` en Windows o `sh bin/build-runner.sh` en Mac/Linux.
Para pruebas deterministas aisladas:
`bin\test-runner.cmd` o `sh bin/test-runner.sh`. Estas pruebas crean
repositorios Git ficticios temporales y no leen código WARI.

El Runner controla solo sus propios comandos. JARVIS y los contratos
controlan la intención de herramientas directas de Codex; una prohibición
en un prompt no equivale a una barrera técnica externa. Producción es
READ ONLY para WARI Agents en ambos niveles.
