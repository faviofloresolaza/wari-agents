# Entrega y ambientes — conocimiento v0.1

Fuente de los puntos confirmados: definiciones del desarrollador para MVP v0.1, 2026-09-30. Son el proceso conocido para evaluar, no una política corporativa completa.

## QC y Git

- **CONFIRMADO:** una HU tiene rama de trabajo; la rama debe alinearse con `produccion`, y su estado frente a esa referencia se verifica otra vez antes del PR. El PR debe contener solo cambios y commits de la HU; detectar archivos o commits inesperados.
- **CONFIRMADO:** readiness para QC considera, **cuando aplique**, desarrollo terminado, validación técnica disponible, CU para funcionalidad nueva, DT, DPC, documentación en Confluence, scripts adjuntos en Jira y versionados en Git, LDIF, permisos, estado Git, conflictos y cambios inesperados. Cada obligatoriedad concreta requiere evidencia; lo desconocido es PENDIENTE DE VALIDACIÓN.
- **CONFIRMADO:** una solicitud de PR hacia QC no autoriza promoción a QAM. QA u otras personas realizan las pruebas funcionales/integrales; un OK técnico no equivale a su aprobación.
- **CONFIRMADO — v0.1.1, definición del desarrollador:** el destino de PR se toma de la solicitud actual. Un PR previo a QC no instruye QAM; una nueva solicitud hacia QC apunta otra vez a QC. PR, merge o integración no autorizan eliminar ramas locales/remotas; no hay eliminación automática.
- **CONFIRMADO — nomenclatura/creación de ramas, definición del desarrollador:** JARVIS nunca inventa `<BRANCH>`. Con HU y sin nombre explícito propone exactamente `<HU>`; un nombre explícito prevalece; sin ambos, bloquea y pide nombre. Antes de crear muestra `<BRANCH>` y base `origin/produccion` actualizada/verificada, y solicita confirmación humana. Nunca presume que `produccion` local esté actualizada. Los identificadores de ejemplo no son reglas especiales.
- **CONFIRMADO — política WARI Agents, definición del desarrollador:** rollback de HU requiere diagnóstico de HU/worktree/rama/estado/objetivo/impacto y confirmación humana de la reversión; commit de reversión necesita confirmación expresa adicional. Cambios sin commit no se descartan; commits compartidos favorecen reversión trazable. Reset destructivo y reescritura remota tienen tratamiento y confirmación específica. No afecta otra HU, no implica PR y jamás opera sobre/hacia `produccion`. La ruta de estados HU es `<worktree>/workspace/<HU>/output/`; tras rollback, revalidarlos antes de reutilizar. Esto define el método de WARI Agents, no un procedimiento corporativo de Git.
- **PENDIENTE DE VALIDACIÓN:** procedimiento corporativo de alineamiento, ramas remotas, PR, gates por ambiente y matriz oficial de documentos. No declarar merge o rebase como política oficial.

## Producción — límite absoluto

- **CONFIRMADO:** WARI Agents puede analizar readiness previo a producción. Toda acción hacia producción es manual y externa a WARI Agents. Nunca crear, enviar o aprobar PR, mergear, hacer push/promoción o desplegar hacia `produccion`, ni ejecutar una operación indirecta equivalente, aun ante solicitud explícita o permisos técnicos.

## Runtime local

- **CONFIRMADO:** la aplicación se ejecuta localmente sobre IBM WebSphere.
- **CONFIRMADO — límite de alcance:** WebSphere se conserva únicamente como contexto arquitectónico. WARI Agents no configura perfiles o servidores, no inicia ni detiene WebSphere y no despliega artefactos.
- **CONFIRMADO:** no existe un comando local de pruebas que deba configurarse. QA documenta y ejecuta únicamente las validaciones disponibles para cada HU, diferenciando claramente pruebas ejecutadas de pruebas propuestas.
