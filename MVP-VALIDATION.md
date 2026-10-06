# WARI Agents v0.4.1 — matriz vigente

La [guía de certificación](docs/CERTIFICATION.md) contiene
precondición, acción, resultado y evidencia de diez casos. Registrar
PASS/FAIL/BLOCKED/NOT RUN únicamente por ejecución observada. La matriz
v0.1.1 permanece archivada por el equipo como evidencia histórica; no
es gate operativo de v0.4.1.

| Área | Estado local v0.4.1 | Evidencia |
| --- | --- | --- |
| Compilación Java 21 | PASS en esta máquina | `bin\test-runner.cmd` recompiló el JAR sin dependencias externas |
| Build reproducible del Runner | PASS | Dos compilaciones consecutivas produjeron el mismo SHA-256 del JAR |
| Dos HUs independientes | PASS en fixture Git local | Incluido en `test-runner`: estados y contextos separados dentro de 41 comprobaciones ejecutadas |
| Tareas y anti-loop | PASS en fixture Git local | Suite Windows: dependencias, transición READY, evidencia, exclusión de recursos y tercera corrección bloqueada; 41 comprobaciones ejecutadas |
| Skills por rol | PASS estructural y descubrimiento local | Seis fuentes y seis entrypoints `.codex/skills/` validados con `quick_validate.py`; instalador idempotente |
| Ambiente local | PASS | JDK y Maven validados; compilación configurada y verificada con `clean compile` |
| Reanudación | PASS mecánico | Nuevo proceso Runner leyó fase/siguiente paso de A |
| Mismatch | PASS mecánico | Gate bloqueó rama acordada/activa incorrecta sin cambiar estado |
| Lock por HU | PASS mecánico | Escritura concurrente de estado bloqueada |
| Production Boundary gestionado | PASS mecánico | Destino `origin/produccion` bloqueado |
| Adaptador Codex | PASS de construcción de comando; sesión real BLOCKED | `launch <HU> --dry-run` añade solo worktree elegido. La revisión automática rechazó la sesión efímera READ por posible envío de contenido del repositorio al proveedor sin autorización del payload concreto. |
| Prueba Windows | PASS en esta máquina | `bin\test-runner.cmd`: `RESULT PASS 41 checks` |
| Gate previo de ANALISTA | NOT RUN | Requiere sesión nueva: prueba 11 de `CERTIFICATION.md` |
| Gate de publicación Confluence | NOT RUN | Requiere sesión con integración autorizada o simulación segura: prueba 12 de `CERTIFICATION.md` |
| JARVIS/capacidades en sesión real v0.4.1 | BLOCKED en esta sesión | Requiere abrir una sesión nueva desde la raíz y ejecutar CERTIFICATION.md |
| Escrituras directas del proveedor | NOT RUN | El Runner no las intercepta; certificar adaptador |

Estas pruebas no consultaron WARI funcional, Jira ni Confluence. Los PASS
anteriores de v0.1.1 no se trasladan automáticamente a v0.4.1 u otra PC.
