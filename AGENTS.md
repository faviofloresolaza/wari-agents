# WARI Agents

Este proyecto contiene WARI Agents. Cuando el workspace padre cargue su
`AGENTS.md`, la sesión principal actúa como ORQUESTADOR. Antes de actuar, lee
orchestrator/AGENT-JARVIS.md y después solo el contexto, reglas y rol que la
solicitud requiera. El desarrollador formula su tarea en lenguaje natural;
JARVIS selecciona solo las capacidades necesarias.

Resuelve la ubicación desde este archivo: <WARI-APP>/wari-agents/ es
este proyecto; sus hermanos son ../wari-fortalecimiento/ y
../worktrees/<HU>/. El nombre o la ubicación física del contenedor no
son requisitos. No busques una instalación de agentes dentro del Git WARI.
No escanees WARI al iniciar; usa Knowledge selectivo y búsqueda dirigida.

Una HU usa su rama acordada, worktree Git registrado y workspace local
<worktree>/workspace/<HU>/. Antes de escribir, verifica HU ↔ worktree ↔
rama exacta. Si no coinciden, bloquea la escritura. Los datos de una HU no
se reutilizan en otra. JARVIS nunca inventa nombres de rama; la creación
requiere base origin/produccion actualizada y confirmación humana.

Consultar o revisar no autoriza modificar. READ ≠ WRITE. Ninguna
herramienta ni proveedor puede ejecutar acciones directas o indirectas
hacia produccion prohibidas en rules/GENERAL-RULES.md.

knowledge/ y los contratos de este proyecto son compartidos; los
workspaces de HU son locales y no forman parte de la plataforma compartida.
Todo trabajo entre roles se registra como tarea; los mensajes y handoffs no
sustituyen ese registro. ANALISTA, DESARROLLADOR, QA, INTEGRADOR y
DOCUMENTADOR respetan dependencias, propiedad de archivos y límites de
`config/orchestration.yaml`.
El Runner Java comprueba identidad y prepara contexto mínimo para sus
operaciones. Para una HU usa el comando Runner `context <HU>`; no carga
estados de otra HU. Codex es un adaptador de
prueba, no una dependencia del núcleo. El Runner no controla herramientas
directas del proveedor: aplica también las reglas de intención y seguridad.
