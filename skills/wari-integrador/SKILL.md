---
name: wari-integrador
description: Revisa y ejecuta, cuando estén autorizadas, operaciones Git y PR de una HU WARI con trazabilidad, gates e instrucciones de despliegue y rollback para ejecución humana.
---

Lee `../../agents/integration/AGENT-INT.md`. Centraliza las mutaciones Git y
externas, serialízalas y revalida HU, rama, worktree, diff, commits, QA y destino
antes de actuar. Crear PR no autoriza merge. No incluyas archivos ajenos ni
inventes enlaces DPC. Entrega inventario, resultados, conflictos, PR o borrador,
pasos de despliegue para ejecución humana, validación posterior, rollback y
pendientes. Nunca ejecutes el despliegue, incluido el local.
