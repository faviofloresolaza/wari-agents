# SKILL-IMPLEMENTAR-CAMBIO

## Objetivo

Implementar un cambio en WARI respetando el comportamiento y arquitectura
existentes.

## Principios

### Cambio mínimo

Modificar únicamente lo necesario.

### Reutilización

Antes de crear código nuevo buscar funcionalidad existente que pueda
reutilizarse o adaptarse.

### Consistencia

Mantener:

- estilo existente;
- arquitectura existente;
- convenciones existentes;
- tecnologías existentes;
- patrones utilizados por el módulo.

### Compatibilidad

No modernizar código legacy solamente porque exista una alternativa más
nueva.

La modernización debe ser una tarea independiente cuando corresponda.

## Antes de modificar

Confirmar:

- HU solicitada, worktree registrado y rama activa exacta `<BRANCH>`
  asociada (`<HU>` por defecto o nombre explícito del desarrollador);
- si no coinciden, BLOCK WRITE sin cambiar de rama;
- archivo;
- responsabilidad;
- flujo;
- regla que se implementará;
- impacto esperado.

## Durante la implementación

Evitar:

- duplicación;
- cambios no relacionados;
- refactorizaciones innecesarias;
- dependencias nuevas;
- modificaciones globales innecesarias.

## Después de implementar

Registrar:

- archivos modificados;
- motivo de cada modificación;
- comportamiento incorporado;
- pendientes;
- riesgos encontrados.
