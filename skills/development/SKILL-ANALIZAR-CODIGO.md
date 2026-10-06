# SKILL-ANALIZAR-CODIGO

## Objetivo

Comprender el código existente relacionado con un requerimiento antes
de modificarlo.

## Estrategia

Partir de Knowledge, estado y
`<worktree>/workspace/<HU>/output/DEV-HANDOFF.md` si
existen y son pertinentes. Su ausencia no bloquea una tarea con evidencia
suficiente.
Aplicar `rules/GENERAL-RULES.md`: no repetir el análisis completo ni buscar
de nuevo lo confirmado. Consultar el estado si falta contexto específico.
Para consultas incrementales, seguir el presupuesto de exploración común.

Buscar de forma exacta y dirigida, leyendo fragmentos relevantes, utilizando:

- clases;
- métodos;
- tablas;
- campos;
- pantallas;
- servicios;
- mensajes;
- procesos;
- configuraciones;
- términos del requerimiento.

## Seguir el flujo

Verificar las relaciones necesarias para implementar; una coincidencia
textual no demuestra participación en el flujo. No abrir todos los archivos
ni recorrer el repositorio completo por defecto.

Cuando sea necesario seguir:

entrada
  ->
controlador / action
  ->
servicio / negocio
  ->
DAO / persistencia
  ->
base de datos / integración

o el flujo equivalente utilizado por WARI.

## Buscar referencias existentes

Antes de diseñar algo nuevo, reutilizar las referencias ya identificadas;
si no bastan, buscar de forma dirigida:

- implementaciones similares;
- interfaces;
- clases base;
- utilitarios;
- DAOs;
- mappings Hibernate;
- queries;
- configuraciones;
- constantes;
- pruebas.

## Resultado

Registrar solo hallazgos nuevos, contradicciones o bloqueos respecto a la
evidencia de partida. Volver a AN únicamente cuando una decisión funcional
sin evidencia impida implementar correctamente.

Identificar:

- flujo actual;
- archivos relacionados;
- punto probable de modificación;
- dependencias;
- implementación similar reutilizable;
- riesgos;
- dudas pendientes.

## Regla

Encontrar una coincidencia textual no significa que el componente forme
parte del flujo.

Debe verificarse la relación real con el requerimiento.
