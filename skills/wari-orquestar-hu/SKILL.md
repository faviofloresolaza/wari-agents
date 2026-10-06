---
name: wari-orquestar-hu
description: Coordina solicitudes de una HU WARI mediante tareas trazables para ANALISTA, DESARROLLADOR, QA, INTEGRADOR y DOCUMENTADOR, con dependencias, aislamiento y límites anti-loop.
---

Usa este skill cuando el pedido implique trabajar una HU MEWARI o DWARI, o
coordinar más de un rol WARI. Para consultas pequeñas de solo lectura, responde
con el rol mínimo sin construir toda la cadena.

1. Lee `../../orchestrator/AGENT-JARVIS.md`,
   `../../config/orchestration.yaml` y la configuración local
   `<workspace>/.wari/environment.local.yaml`.
2. Identifica una HU inequívoca y recupera su contexto con Runner. No crea rama
   o worktree sin la autorización exigida.
3. Descompone el resultado en tareas con ID, rol, estado, dependencias, alcance,
   archivos permitidos/prohibidos, entradas, criterios y evidencia requerida.
4. Registra la tarea antes de delegarla. Un handoff es el resultado de una
   tarea y referencia la tarea siguiente; no es una autorización nueva.
5. Ejecuta en paralelo solo tareas READY sin recursos mutables compartidos.
6. Valida cada entrega antes de habilitar dependientes. Una corrección se
   registra como tarea hija y nunca reabre una tarea DONE.
7. Detén ciclos según `../../config/orchestration.yaml`; persiste WAITING_APPROVAL y
   no repite la misma solicitud de autorización.

No publiques ni integres por el solo hecho de completar la cadena. Nunca
delegues ni ejecutes un despliegue; solo permite preparar instrucciones para
ejecución humana.
