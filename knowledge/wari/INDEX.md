# Knowledge WARI — índice vigente

Este directorio contiene conocimiento reutilizable. Cargar solo la ficha pertinente a la intención; `context/WARI-CONTEXT.md` es un índice mínimo, no la fuente de todos los detalles. Antes de ampliar una búsqueda, consultar también el estado compacto de la HU si existe.
Para HU, JARVIS verifica primero worktree/rama; el estado nuevo se lee desde ese worktree. No escanear WARI para cargar Knowledge.

| Tema | Ficha | Estado de la fuente |
| --- | --- | --- |
| Workspace, repositorios y componentes conocidos | `architecture/WORKSPACE.md` | Contexto existente; componentes por verificar en el módulo consultado |
| Entrega, QC, producción y runtime WebSphere local | `process/DELIVERY.md` | Alcance confirmado por el desarrollador; WebSphere es solo contexto y no se opera |

## Clasificación y promoción

Cada afirmación de una ficha debe indicar **CONFIRMADO**, **INFERIDO DEL CÓDIGO** o **PENDIENTE DE VALIDACIÓN**, junto con su fuente. «Confirmado» se limita a lo que la fuente demuestra: un proceso comunicado por el desarrollador no certifica un procedimiento corporativo completo. Una HU o ejemplo aislado no establece un estándar general.

Un hallazgo reusable nuevo sigue `DISCOVERED → CANDIDATE → VALIDATED → CONFIRMED → VERSIONED → REUSED`. Registrar el candidato, fuente, alcance y evidencia en el estado de la tarea; no editar Knowledge compartido como consecuencia automática de una HU. La validación humana de incorporación y la política de versionado siguen pendientes del responsable designado por el equipo. Si se aprueba la incorporación, agregar la ficha o actualizarla con fuente y fecha; las afirmaciones sin evidencia permanecen pendientes.
