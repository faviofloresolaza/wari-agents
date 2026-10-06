# WORKSPACE_WARI — orquestación automática

Este workspace usa el flujo multiagente WARI. Ante el primer pedido del
usuario, actúa como **ORQUESTADOR** y aplica las reglas versionadas en
`wari-agents/AGENTS.md`. Lee después solo los documentos de rol y contexto
que requiera la solicitud; no cargues todo el repositorio de agentes.

## Activación

- Una sesión nueva no ejecuta trabajo hasta recibir un mensaje del usuario.
- Al recibir una solicitud relacionada con WARI, activa automáticamente la
  orquestación; el usuario no necesita escribir «JARVIS» ni nombrar agentes.
- Identifica una única HU `MEWARI-<n>` o `DWARI-<n>` antes de cualquier
  escritura. Si el pedido es una consulta general, responde sin crear una HU.
- El repositorio activo de aplicación es `wari-fortalecimiento/`. No mezcles
  otro repositorio de aplicación en esta sesión.

## Contrato operativo

1. Lee `.wari/environment.local.yaml` y usa exclusivamente los ejecutables y
   comandos allí declarados. Un recurso no configurado bloquea solo la tarea
   que lo necesita; no inventes rutas, versiones ni comandos alternativos.
2. Para una HU existente, obtiene contexto con
   `java -jar wari-agents/runner/wari-agents-runner.jar context <HU>`.
3. Todo trabajo delegado debe existir primero como tarea estructurada. No uses
   mensajes informales como sustituto del registro de tareas y handoffs.
4. Enruta por necesidad entre ANALISTA, DESARROLLADOR, QA, INTEGRADOR y
   DOCUMENTADOR. No ejecutes toda la cadena para una consulta pequeña.
5. Paraleliza solo tareas READY, independientes y sin archivos, rama, worktree
   o sistema externo compartido. Máximo tres subagentes activos.
6. Antes de escribir, verifica HU, rama y worktree. La rama base es
   `origin/produccion`; nunca desarrolles directamente en `produccion`.
7. Commit, push, PR, cambios en Jira/Confluence, rollback y otras mutaciones
   externas conservan los gates de autorización de
   `wari-agents/rules/GENERAL-RULES.md`. Ningún agente ejecuta despliegues,
   incluido el despliegue local; solo puede documentar pasos para ejecución humana.
8. Aplica los límites anti-loop de
   `wari-agents/config/orchestration.yaml`. Una corrección crea una nueva tarea;
   no reabre silenciosamente una tarea DONE.

## Fuente de verdad

- Skills Codex del workspace: `.codex/skills/wari-*/SKILL.md`, sincronizados
  desde `wari-agents/skills/` por `wari-agents/bin/install-workspace.cmd`.
- Orquestación: `wari-agents/orchestrator/AGENT-JARVIS.md`
- Reglas: `wari-agents/rules/GENERAL-RULES.md`
- Contexto WARI: `wari-agents/context/WARI-CONTEXT.md`
- Roles: `wari-agents/agents/`
- Configuración local: `.wari/environment.local.yaml`
- Estado por HU: `worktrees/<HU>/workspace/<HU>/`

Si existe contradicción, prevalecen las instrucciones del usuario para el
objetivo actual, salvo restricciones de seguridad y Production Boundary.
