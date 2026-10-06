# SKILL-CREAR-DT

## Objetivo

Generar un Documento Técnico (DT) para un requerimiento WARI.

## Información mínima

Antes de generar el documento intentar identificar:

- HU relacionada.
- Proceso.
- Sistema u opción.
- Impacto.
- Autor.
- Descripción funcional.
- Solución técnica.
- Componentes afectados.
- Consideraciones.
- Pruebas.

## Fuentes y contraste

Aplicar `AGENT-DOC.md` y reglas generales. JARVIS entrega HU/worktree/rama
verificados y evidencia validada de AN/DEV: requisito, análisis, diff,
archivos/commits y pruebas cuando correspondan. DOC transforma esa evidencia
en documento; no inspecciona código ni hace análisis funcional/técnico nuevo.
Ante un desarrollo manual sin handoff suficiente, devolver a JARVIS los datos
faltantes para reconstrucción dirigida de AN/DEV. Si falta fuente indispensable,
señalar el dato preciso sin inventar implementación. Registrar trazabilidad:
fuente de HU, rama/commits y referencias concretas entregadas. Distinguir
datos confirmados, inferidos y PENDIENTE DE CONFIRMAR.

## Estructura base

# DT [HU]

## Información general

HU Relacionados:

Proceso:

Sistema / opción:

Impacto:

Autor:

## Descripción funcional

Describir qué solicita el requerimiento desde el punto de vista funcional.

Evitar detalles técnicos innecesarios en esta sección.

## Solución técnica

Describir cómo se implementó o cómo se propone implementar la solución.

Indicar los componentes afectados cuando estén confirmados.

## Componentes afectados

Indicar únicamente componentes realmente identificados.

## Consideraciones

Registrar restricciones, comportamientos especiales, dependencias o
consideraciones relevantes.

## Pruebas

Separar pruebas realmente ejecutadas, con evidencia y resultados disponibles,
de pruebas propuestas o pendientes para comprobar el cambio. No inferir ejecución
ni éxito a partir del código, del diff o de una lista de criterios de validación.

## Reglas

No inventar información para completar campos.

Cuando un dato no esté disponible utilizar:

PENDIENTE DE CONFIRMAR

El documento generado inicialmente debe considerarse BORRADOR.
Si luego se solicita publicarlo en Confluence, aplicar el gate de
`AGENT-DOC.md`: ruta canónica explícita y confirmación final del nombre y
destino antes del WRITE. Generar el DT no autoriza subirlo.
