# SKILL-VALIDAR-CAMBIO

## Objetivo

Realizar una validación inicial de una implementación antes de entregarla
al desarrollador.

## Revisar

Aplicar `rules/GENERAL-RULES.md` y los criterios confirmados de la tarea;
usar `DEV-HANDOFF.md` si existe.
La validación y el diff pertenecen al worktree de la HU verificada; si
HU/worktree/rama no coinciden, no corregir código allí.
Validar el cambio y sus efectos, sin repetir el análisis completo de la HU.

### Ciclo obligatorio en la misma ejecución

Aplicar el ciclo de autorrevisión de `AGENT-DEV.md` antes de entregar, sin
esperar otra solicitud del usuario. Revisar el diff completo (también archivos
nuevos) contra el alcance autorizado y detectar cambios accidentales/fuera de alcance.
Tras compilación/pruebas dirigidas, corregir errores propios, repetir las
validaciones afectadas cuando sea posible y revisar nuevamente el diff final.
Preservar cambios preexistentes ajenos. Informar archivos modificados, alcance
implementado, validaciones, resultados, lo no validado con motivo y pendientes
o riesgos. La revisión humana y aprobación explícita siguen siendo necesarias
antes de commit/push/merge/deploy.

### Código

- errores evidentes;
- imports;
- tipos;
- variables;
- null;
- excepciones;
- recursos;
- transacciones.

### Funcionalidad

Comprobar que cada regla confirmada del requerimiento tenga una
implementación correspondiente.

### Impacto

Revisar posibles efectos sobre:

- flujos existentes;
- consultas;
- persistencia;
- integraciones;
- procesamiento batch;
- concurrencia;
- rendimiento.

### Build

Si existe acceso a las herramientas necesarias, intentar utilizar el
mecanismo de compilación definido por el proyecto.

Comenzar por el componente o módulo afectado cuando sea técnicamente posible.
Ampliar por dependencias, fallo encontrado, riesgo concreto o gate obligatorio.

### Tests

Ejecutar pruebas existentes relacionadas cuando sea posible.

Priorizar pruebas dirigidas; conservar las validaciones críticas de reglas,
transacciones e impacto. Detener ampliaciones opcionales al quedar cubierto
el riesgo concreto y registrar explícitamente lo no ejecutado.

No afirmar:

"Compila correctamente"

si la compilación no fue ejecutada.

No afirmar:

"Pruebas exitosas"

si las pruebas no fueron ejecutadas.

## Resultado

Clasificar cada validación como:

VALIDADO

ERROR

NO EJECUTADO

PENDIENTE
