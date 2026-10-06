# SKILL-ANALIZAR-IMPACTO

## Objetivo

Identificar inicialmente qué partes de WARI podrían estar relacionadas
con un requerimiento.

Este análisis aporta impacto inicial reutilizable para AN o DEV;
no determina por sí solo qué archivos deben modificarse.

## Buscar

Solo después de confirmar el contrato de exploración de `AGENT-AN.md`, y
cuando exista acceso al repositorio WARI, revisar las categorías
relacionadas con la pregunta y la evidencia disponible:

- módulos;
- clases;
- métodos;
- configuraciones;
- archivos Hibernate;
- consultas;
- tablas referenciadas;
- servicios;
- integraciones;
- procesos relacionados.

## Estrategia

Leer el estado de la HU, proponer puntos de partida/ruta de búsqueda y esperar
confirmación. Luego aplicar el presupuesto de exploración de
`rules/GENERAL-RULES.md`: referencias existentes, búsquedas exactas y
fragmentos necesarios. No recorrer módulos completos por defecto ni
reinvestigar componentes confirmados sin contradicción o petición explícita.

Buscar inicialmente utilizando información proveniente del requerimiento:

- nombre del proceso;
- número de operación;
- nombre de tabla;
- nombre de pantalla;
- servicio;
- mensaje;
- campo;
- código conocido.

Seguir las referencias necesarias para responder con seguridad y detenerse
cuando exista evidencia suficiente para la consulta concreta o se alcance el
presupuesto. No abrir otro módulo ni ampliar el grafo de dependencias sin
mostrar primero la evidencia obtenida y recibir confirmación.

## Resultado

Clasificar componentes encontrados como:

DIRECTAMENTE RELACIONADO

POSIBLEMENTE RELACIONADO

DESCARTADO

## Restricciones

No modificar código. Persistir estado solo cuando la intención lo autorice,
dentro de `<worktree>/workspace/<HU>/output/` tras gate HU.
Una consulta de solo lectura no crea
estado ni handoff.

No implementar soluciones.

No afirmar que un componente será modificado únicamente porque contiene
una palabra relacionada.

Debe existir evidencia suficiente para relacionarlo con el flujo.
