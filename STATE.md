# WARI Agents v0.4.1 — estado de producto

WARI Agents reside en `<WARI-APP>/wari-agents/`, independiente y hermano
de `wari-fortalecimiento/` y `worktrees/`. No requiere una rama, commit,
workspace ni conversación histórica de una HU concreta. La distribución
física de esta carpeta es externa al producto.
Su remoto oficial es `https://github.com/faviofloresolaza/wari-agents.git` y
su rama principal es `main`.

JARVIS coordina AN, DEV, QA, DOC e INT según intención. Knowledge First,
Progressive Discovery y Context Compaction reducen contexto. READ ≠ WRITE,
capacidad técnica ≠ autorización, destino de PR explícito, DOC evidencia
→ documentación y Production Boundary absoluto permanecen vigentes.
Desde v0.4, AN debe acordar con el usuario un contrato de exploración antes de
navegar código WARI. La tarea espera confirmación con presupuesto explícito;
las ampliaciones no son automáticas y tienen límite por tarea.
Desde v0.4.1, cualquier publicación documental en Confluence exige ruta
canónica explícita y confirmación final de nombre/título y destino. La
confirmación es de un solo uso y generar el documento no autoriza publicarlo.

El Runner Java 21 valida la identidad HU ↔ rama acordada ↔ worktree Git
registrado ↔ workspace local. `state.json` es mecánico y por HU;
`orchestration/tasks.json` controla dependencias, estados, recursos, evidencia,
concurrencia y ciclos de corrección; los
handoffs de razonamiento siguen en Markdown local. El Runner ofrece
`check`, `list`, `context`, `gate`, `init`, `record`, `tasks`, `task-*`,
`create` y `launch`.
Una consulta sobre una HU sin worktree no crea rama; JARVIS puede usar las
fuentes disponibles y debe solicitar confirmación antes de crearla para WRITE.
Codex es el único adaptador inicial. El Runner no intercepta herramientas
shell/MCP directas del proveedor: esa frontera requiere certificación
propia. Claude Code y otros proveedores siguen pendientes.

La compilación y 41 pruebas deterministas del Runner pasaron en Windows. Los
seis skills WARI y sus seis entrypoints repo-scoped pasaron
`quick_validate.py`; el instalador los sincroniza de forma idempotente bajo
`.codex/skills/`. El JDK de aplicación y Maven ya están declarados localmente.
El build del Runner es reproducible: recompilaciones sin cambios conservan el
SHA-256 del JAR versionado.
No existen comandos locales de pruebas ni empaquetado y WebSphere es solo
contexto, fuera del alcance operativo de los agentes. La compilación local usa
Maven `clean compile` y el chequeo del ambiente no tiene pendientes.
La certificación conversacional con Codex en v0.4.1 y una segunda máquina sigue
pendiente. Una sesión efímera Codex
READ fue bloqueada por revisión automática debido al posible envío de
contenido del repositorio al proveedor externo; no se reintentó. Cualquier
certificador debe seguir `docs/CERTIFICATION.md` y registrar solo
resultados observados.
Jira/Confluence y otras integraciones requieren autenticación individual;
los PASS previos de otra sesión/máquina no se heredan.
