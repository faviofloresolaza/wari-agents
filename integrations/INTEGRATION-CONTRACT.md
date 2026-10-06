# Contrato de integración v0.1.1

JARVIS solicita una **capacidad**; el adaptador disponible del proveedor IA
resuelve la herramienta concreta. AN, DEV, QA, DOC e INT no dependen del nombre
de un MCP, comando o API. Git local, GitHub remoto, Jira, Confluence y un
conector futuro cumplen el mismo contrato de disponibilidad y autorización.

## Resolución por operación

1. Definir fuente, objeto exacto (HU, rama, documento) y operación READ o
   WRITE desde la intención vigente.
2. Comprobar herramienta y sesión: `AVAILABLE` si responde con acceso real;
   `UNAVAILABLE` si no existe/no responde. Si existe pero falta login o
   permiso, `PENDIENTE DE CONFIGURACIÓN/AUTORIZACIÓN`.
3. Comprobar permiso efectivo para esa operación: `READ` o `WRITE` según
   conector **y** autorización de la solicitud. `NOT AUTHORIZED` prevalece
   aunque la herramienta permita WRITE. Leer nunca autoriza escribir.
4. Para Git mutante, aplicar gate HU ↔ worktree ↔ rama justo antes de la
   operación. Para PR, exigir destino explícito actual. Production Boundary
   bloquea acciones directas e indirectas sin excepción.
5. Para Confluence WRITE, exigir nombre/título exacto, operación, sitio,
   espacio, ruta o página padre y URL/page ID canónico. Mostrar el destino final
   y obtener confirmación explícita inmediatamente antes de escribir. La
   confirmación es de un solo uso y se invalida si cambia contenido o destino.
6. Ejecutar solo la herramienta pertinente. Guardar referencia, fecha y
   resultado sin copiar indiscriminadamente contenido externo ni secretos.
   Si falla, continuar con fuentes disponibles cuando basten; señalar el
   dato bloqueado. Nunca simular PASS.

## Fuentes

| Fuente | READ inicial | WRITE futuro |
| --- | --- | --- |
| Jira | HU, descripción, criterios y adjuntos pertinentes | Solo petición explícita, permiso y gate documental aplicable |
| Confluence | Buscar/leer página relacionada y estándar | Solo con petición explícita, ruta canónica, nombre/operación visibles, revisión aplicable, permiso y confirmación final de un solo uso |
| Git local | Worktrees, ramas, status, diff, commits e historial | Acción Git específicamente autorizada y gate de HU |
| GitHub remoto | Ref/estado remoto, comparación y PR existente | PR/demás operación explícita, gate y destino permitido |
| Jenkins/WAS | Contexto arquitectónico únicamente | Fuera de alcance; sin operaciones ni despliegues |
| Conector futuro | Declarar capacidades y comprobarlas en sesión | Misma regla de intención, gate y Production Boundary |

La conexión técnica y autenticación son externas a este contrato. Los
adaptadores pueden ser MCP oficial, conector del proveedor, CLI o API
autorizada; ninguna opción cambia las reglas de JARVIS. Proveedor IA y
fuente de datos son ejes distintos. `PASS` se registra por proveedor,
conector, ambiente, fecha y operación realmente probada.
