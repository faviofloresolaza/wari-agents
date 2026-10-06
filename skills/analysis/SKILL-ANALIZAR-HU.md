# SKILL-ANALIZAR-HU

## Objetivo

Analizar funcionalmente una Historia de Usuario o requerimiento WARI.

## Procedimiento

Aplicar `rules/GENERAL-RULES.md`. Verificar HU/worktree/rama y leer el
estado existente de esa HU en
`<worktree>/workspace/<HU>/output/ANALYSIS-STATE.md` si existe.
Antes de abrir o buscar código, aplicar el gate de `AGENT-AN.md`: completar
con el usuario el contrato de exploración y esperar su confirmación. Sin ella,
registrar `WAITING_APPROVAL` y detenerse. La confirmación habilita solo el
alcance y presupuesto mostrados.
El estado legado central de la misma HU es solo referencia histórica.
El procedimiento completo siguiente corresponde al análisis inicial.
Para una consulta incremental, trabajar solo la pregunta y los puntos
pendientes o contradichos, siguiendo el presupuesto de exploración común.
Detener la investigación al contar con evidencia suficiente o al alcanzar el
presupuesto, lo que ocurra primero. Toda expansión vuelve al usuario con
hallazgos parciales y una propuesta acotada.

### 1. Identificar el objetivo

Determinar qué necesidad intenta resolver el requerimiento.

Responder:

¿Qué se necesita conseguir?

### 2. Identificar la situación actual

Determinar cómo funciona actualmente el proceso cuando exista
información suficiente.

No asumir comportamiento no confirmado.

### 3. Identificar el comportamiento esperado

Determinar qué debe cambiar después de implementar el requerimiento.

### 4. Identificar actores

Identificar usuarios, sistemas, procesos o componentes que participan.

### 5. Identificar reglas de negocio

Extraer únicamente reglas que estén explícitamente indicadas o que
puedan demostrarse mediante fuentes confiables.

### 6. Identificar validaciones

Determinar qué condiciones deben cumplirse.

### 7. Detectar información faltante

Registrar preguntas o información necesaria para completar el análisis.

## Clasificación

Cada conclusión importante podrá clasificarse como:

CONFIRMADO

Existe evidencia suficiente.

HIPOTESIS

Existe evidencia parcial y debe verificarse.

PENDIENTE DE CONFIRMAR

No existe información suficiente.

BLOQUEADO

Falta información crítica que impide concluir o continuar ese punto.

## Resultado

Actualizar el estado actual compacto cuando la tarea autorice persistencia;
no crear un handoff DEV por defecto. Sustituir
decisiones vigentes y conservar las anteriores en DESCARTADO con motivo breve.
Responder hallazgo, evidencia, conclusión y bloqueo si existe; no reconstruir
un informe completo en cada consulta.
