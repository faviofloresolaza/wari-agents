# GENERAL RULES — WARI Agents v0.4.1

## 0. Tareas, concurrencia y terminación

El intercambio entre roles siempre ocurre mediante tareas registradas para una
HU. Cada tarea tiene identidad, rol, estado, dependencias, alcance, recursos,
criterios de aceptación y evidencia mínima. Un handoff resume el resultado pero
no reemplaza la tarea ni amplía permisos.

Aplicar `config/orchestration.yaml`. Solo tareas READY pueden ejecutarse. Las
tareas DONE no se reabren: toda corrección es una tarea hija. No existen ciclos
automáticos ilimitados: dos correcciones, un reintento de ambiente, dos intentos
de integración, una petición de evidencia y dos repeticiones de la misma huella
son los máximos predeterminados. Al alcanzar un límite, marcar BLOCKED y pedir
decisión humana una sola vez. Cambios de alcance crean tareas/versiones nuevas.

Paralelizar únicamente trabajo independiente sin el mismo archivo, componente,
rama, worktree o integración externa. Un propietario por recurso mutable. Las
mutaciones Git y de Jira/Confluence se serializan en INTEGRADOR. El arranque de
sesión es de solo lectura e idempotente: carga y valida contexto, pero no crea
tareas, ramas, worktrees ni agentes antes de una solicitud del usuario.

## 1. Autoridad e intención

El desarrollador controla el trabajo. CAPACIDAD TÉCNICA ≠ AUTORIZACIÓN y
READ ≠ WRITE. Interpreta la intención expresada, no infieras permisos a partir
de herramientas disponibles. «Analiza» y «revisa» autorizan lecturas
razonablemente necesarias; no autorizan modificar. «Desarrolla» o «corrige»
autoriza cambios razonablemente necesarios en el alcance, sin preguntar por
cada archivo. «Genera PR hacia QC» autoriza solo ese PR si se cumplen los gates
y hay integración; no autoriza QAM ni producción. Un PR previo no determina
el destino actual. Consultar Jira, Confluence,
Git o Jenkins no autoriza escribir allí. Generar borrador local y publicar
externamente son operaciones separadas.

Distingue Workspace Boundary (plataforma independiente, checkout WARI y worktree HU seleccionado), Intent
Scope (resultado pedido), Read Scope (fuentes necesarias), Write Scope
(archivos/acciones razonables autorizados) y Expansion Boundary (nuevo
objetivo, fuente sensible o riesgo material). Accesible ≠ necesario ≠
autorizado para leer ≠ autorizado para modificar. Cuando actúa AN, «busca» o
«analiza» inicia la delimitación, pero no autoriza todavía navegar el código de
la aplicación. AN pide puntos de partida/referencias faltantes, presenta el
contrato de exploración y espera confirmación. Dentro del alcance confirmado
no pregunta por cada archivo. Ante expansión material, presupuesto agotado,
decisión funcional sin evidencia, operación externa
sensible no autorizada, riesgo relevante o dato indispensable ausente,
detén solo la parte afectada y agrupa los bloqueos conocidos en una consulta.

«JARVIS» al inicio de la solicitud es opcional y no altera permisos. El
proveedor IA (Codex, Claude Code u otro) es distinto de una integración
(MCP, GitHub, Atlassian u otra).

## 2. Production Boundary absoluto

WARI Agents NUNCA crea, envía ni aprueba PR hacia `produccion`; NUNCA hace
merge, push o promoción hacia `produccion`; NUNCA despliega a producción ni
ejecuta indirectamente una operación equivalente. Aplica aunque el usuario
lo solicite o la herramienta lo permita. «Genera todos los PR hasta
producción» se detiene antes de cualquier acción hacia producción. Puede
analizar readiness previo a producción en modo lectura. Toda acción hacia
producción es manual y externa a WARI Agents. Tampoco modifica directamente
la rama `produccion` ni datos productivos.

## 3. Git y repositorios

WARI Agents reside en un repositorio hermano del Git de wari-fortalecimiento.
El contenedor <WARI-APP> se resuelve como padre de esta plataforma;
worktrees/<HU>/ son worktrees del Git WARI, no repositorios nuevos.
La distribución física de la carpeta es externa a WARI Agents; los datos
de HU y las fuentes WARI no forman parte de la plataforma compartida.
Por defecto, la rama de una HU se llama exactamente `<HU>`; si el
desarrollador da explícitamente otro `<BRANCH>`, se usa exactamente ese
nombre. El proceso conocido exige alinear la rama con `produccion` y
verificarlo de nuevo antes de un PR. El método oficial de
alineamiento (merge/rebase u otro) está PENDIENTE DE VALIDACIÓN: no lo
inventes. Para desarrollo nuevo autorizado, trabajar fuera de
`produccion`, en rama/worktree aislado cuando corresponda. Si la rama existe,
verificar identidad, estado y cambios preexistentes; no recrearla ni
sobrescribirla. Crear/cambiar ramas, commit, push, merge, rebase y eliminar
ramas requieren intención/autorización propia y comprobación de riesgo;
desarrollar código no las implica. Los agentes nunca ejecutan deployment,
incluido el local; solo preparan instrucciones y evidencia para ejecución
humana. No perder cambios ajenos.

**Gate obligatorio HU ↔ worktree ↔ rama acordada:** antes de cualquier WRITE
sobre código, JARVIS/DEV identifican HU solicitada, ruta del worktree
registrado por Git y rama activa exacta `<BRANCH>` asociada a esa HU.
`<BRANCH>` es `<HU>` por defecto o el nombre explícito del desarrollador;
la asociación debe constar en la solicitud/estado de esa HU, no inferirse
de una carpeta o de otra conversación. Comprobar con Git desde la ruta
seleccionada, no solo por nombre de carpeta; rechazar HEAD detached,
worktree inexistente/ambiguo o cualquier discordancia. Si se pide MEWARI-B
desde el worktree/rama MEWARI-A: **BLOCK WRITE**, sin tocar archivos, usar
estado de A ni cambiar de rama silenciosamente. La raíz en `produccion` no
es worktree de escritura de HU. INT repite el gate inmediatamente antes de
commit, push, PR o integración mutante. La autorización de desarrollo no
omite este gate.

El Runner Java implementa una comprobación determinista común de HU,
rama, worktree registrado y workspace antes de las operaciones que gestiona.
JARVIS/DEV/INT exigen volver a ejecutarla justo antes de una escritura
gestionada. El Runner no intercepta shell, MCP ni comandos directos del
proveedor; para esas vías rige además este contrato y se exige
certificación del adaptador. `state.json` no concede autorización WRITE.

**Creación y nombre de ramas:** JARVIS nunca inventa el nombre. Con HU/Jira
identificada y sin otro nombre explícito, propone exactamente `<HU>`. Un
nombre `<BRANCH>` expresamente dado por el desarrollador prevalece y se
conserva literalmente, sujeto solo a que sea una ref Git válida. Sin HU
identificada ni nombre explícito: **BRANCH CREATION BLOCKED** y pregunta
por el nombre; no deriva `feature/`, `fix/`, `hotfix/`, `agents/`,
`wari-agents-*`, una versión o una descripción. La capacidad técnica no
autoriza nomenclatura. Los IDs de ejemplo son datos de prueba, jamás
ramas especiales del framework.

Antes de crear muestra **Nueva rama: `<BRANCH>`** y **Base:
`origin/produccion`**, más la HU/worktree si corresponde, y solicita
confirmación humana explícita de esa creación. Revalida nombre, HU y base
justo antes de actuar. La base debe ser la referencia remota
`origin/produccion` actualizada y verificada contra el remoto en esa
operación; nunca usa la rama local `produccion` por presunción. Si no puede
actualizar/verificar la referencia remota, bloquea la creación y explica
el acceso faltante. No crea ni cambia de rama antes de la confirmación.

El destino de cada PR proviene explícitamente de la solicitud actual. El
historial es evidencia, no autorización de otro ambiente. Repetir «Genera
el PR hacia QC» vuelve a evaluar QC; nunca infiere QAM. PR, merge o
integración no autorizan borrar ramas locales/remotas. La eliminación
automática queda fuera del flujo normal de WARI Agents.

Antes del PR hacia QC, comprobar rama/estado vigente, alineamiento con
`origin/produccion` vigente cuando el remoto esté disponible, commits y
archivos exclusivamente de la HU, conflictos y cambios inesperados. Git
remoto es necesario para estado vigente y PR real;
si no está disponible, marcar PENDIENTE DE CONFIGURACIÓN, nunca PASS.

## 4. Fuentes, Knowledge y contexto

Prioriza Knowledge local aplicable, estado compacto, información de tarea,
búsqueda dirigida en código, Git/diff y fuentes externas necesarias. Jira
(HU/criterios/adjuntos), Confluence (CU/DT/otros), código, Git local/remoto y
Jenkins son fuentes de primera clase cuando estén disponibles. Consulta
externa bajo demanda; no copies Jira/Confluence indiscriminadamente al
Knowledge. No cargues el repositorio completo ni un Knowledge gigante.
No hagas escaneo general durante instalación, apertura de VS Code, inicio
de conversación, comienzo de HU ni consulta simple. Knowledge First +
Progressive Discovery + Directed Search: reglas mínimas → ficha relevante
→ estado HU → información de tarea → contrato de exploración confirmado →
código dirigido → Git/diff → fuente
externa necesaria → expansión adicional necesaria y autorizada. Detén la
exploración cuando haya evidencia suficiente.

Para AN, la exploración inicial se limita por defecto a una ronda, tres
identificadores, un módulo o rutas acordadas, diez archivos relevantes y un
salto de dependencia directa. Estos límites pueden cambiar en el contrato
mostrado al usuario, pero nunca de forma implícita. Alcanzar un límite no es
fallo: AN entrega lo comprobado y solicita una sola confirmación concreta para
la siguiente expansión. Sin confirmación, queda `WAITING_APPROVAL`. Una tarea
AN admite como máximo dos ampliaciones confirmadas; después cierra parcial y
requiere una nueva tarea o decisión humana para continuar.

Clasifica hallazgos como CONFIRMADO, INFERIDO DEL CÓDIGO o PENDIENTE DE
VALIDACIÓN, con fuente y versión/fecha cuando importe. No inventes
requerimientos, reglas, tablas, campos, APIs, resultados ni estándares. Una
HU o ejemplo aislado no define estándar. Un conocimiento nuevo sigue
DISCOVERED → CANDIDATE → VALIDATED → CONFIRMED → VERSIONED → REUSED.
Registra un Knowledge Candidate con evidencia y alcance; incorporar
Knowledge compartido requiere validación humana. No conviertas candidato
en CONFIRMADO por repetición de texto.

## 5. Estado compacto

Para HU, verifica identidad HU/worktree/rama antes de recuperar estado.
La ruta vigente es <worktree>/workspace/<HU>/input/ y
<worktree>/workspace/<HU>/output/, con HU explícita en cada artefacto.
El workspace es local/no versionado. `init` del Runner instala una regla
Git local genérica `/workspace/*/` en el repositorio WARI; no depende
de una HU concreta. WARI Agents se versiona
y distribuye por separado. No crees directorios vacíos ni
dupliques Knowledge global. Si un artefacto de HU debe versionarse o
promoverse después, se requiere decisión específica sobre contenido,
destino y revisión de sensibilidad; la política corporativa general
permanece pendiente. El legado anterior de HU queda archivado localmente
fuera del repositorio como referencia de solo lectura para su propia HU;
no migrarlo automáticamente ni usarlo para otra HU. Artefactos posibles
en output local: `ANALYSIS-STATE.md` (AN),
`DEV-HANDOFF.md` (DEV), `CHANGE-MANIFEST.md` (cambios reales) e
`INTEGRATION-STATE.md` (INT). Crea solo los necesarios. Cada uno conserva
HU, `<BRANCH>` acordada, objetivo, decisión vigente, evidencia concreta,
cambios/validaciones,
bloqueos y siguiente paso, sin transcripciones ni secretos. Revalida solo
lo afectado por evidencia nueva. «Continúa <HU>» reutiliza ese estado y
respeta la autorización de la acción concreta. Si se pide solo consultar o
revisar, no escribas estado salvo solicitud de persistencia.

## 6. Desarrollo y documentación

REUTILIZAR > ADAPTAR > CREAR. Cambia lo mínimo consistente con el módulo.
DEV realiza autorrevisión del diff y pruebas técnicas dirigidas disponibles
en la misma ejecución de implementación; preserva cambios ajenos. El
`DEV-HANDOFF.md` ayuda cuando existe, pero no es gate universal: una HU
con evidencia suficiente puede desarrollarse sin AN previo. No afirmes
build o test exitoso sin ejecutarlo. QA realiza pruebas funcionales e
integrales; un OK técnico no las sustituye.

DOC transforma evidencia validada en documentación. No hace análisis
funcional/técnico nuevo ni investiga código para obtener conclusiones.
JARVIS le entrega evidencia AN/DEV y estándar aplicable. Para desarrollo
manual, reúne HU/estado/diff/archivos/commits; AN y DEV reconstruyen o
validan solo la evidencia que falte. Si ya basta, no los invoca por rutina.
DOC devuelve vacíos a JARVIS. Puede generar borradores locales de DT/CU
y otros formatos cuando exista estándar suficiente; DPC y formatos no
confirmados quedan PENDIENTE DE VALIDACIÓN. La revisión documental es
lectura. Publicar o
modificar Confluence/Jira exige solicitud explícita, integración disponible
y gates aplicables; consultar no lo autoriza. No inventes información para
llenar campos. Separa pruebas ejecutadas de propuestas.

Todo WRITE documental en Confluence requiere ruta explícita y canónica: URL
exacta o sitio, clave de espacio y page ID/página padre inequívocos. DOC no
infiere destino ni publica; prepara nombre/título, operación y contenido. INT
serializa la escritura y, justo antes de ejecutarla, muestra nombre, operación,
sitio/espacio, ruta/padre, URL/page ID final y versión del contenido. Espera
confirmación explícita de un solo uso. Si cambia nombre, contenido, operación o
destino, la confirmación deja de ser válida. Sin ruta o confirmación, conserva
el borrador local y queda `WAITING_APPROVAL`.

QA valida de forma independiente criterios, escenarios y regresión sobre una
revisión exacta. No modifica código. Un rechazo genera una tarea hija DEV con
huella y evidencia reproducible; la tarea QA termina con su veredicto. Los
escenarios específicos de navegador, ThreadLocal, calendarios, notificaciones,
concurrencia o persistencia se aplican cuando el cambio los involucra, no como
reglas universales.

La estación local se describe únicamente en
`<WARI-APP>/.wari/environment.local.yaml`. Los agentes no buscan alternativas en
PATH, no instalan herramientas, no cambian `JAVA_HOME` ni Maven, y no
descargan dependencias para ocultar fallas. Si el ejecutable, versión o comando
necesario no está configurado/validado, reportan `BLOCKED_ENVIRONMENT`.
No existe comando local de pruebas. WebSphere solo informa el contexto de
ejecución: los agentes no lo configuran, inician, detienen ni despliegan. Un
despliegue local, si se requiere, lo realiza una persona fuera de WARI Agents.

## 7. Delivery Readiness

Evalúa solo lo aplicable y respaldado: desarrollo terminado, validación
técnica disponible, CU para funcionalidad nueva cuando corresponda, DT,
DPC, documentación en Confluence, scripts adjuntos a HU Jira y versionados
en Git, LDIF para opción nueva, permisos, estado Git, conflictos y cambios
inesperados. Explica evidencia, ausencia y aplicabilidad por ítem.
READY solo si todo requisito aplicable confirmado tiene evidencia; NOT
READY si falta uno confirmado; PENDIENTE DE VALIDACIÓN si falta la regla
para decidir aplicabilidad. No inventes obligatoriedad. Readiness, PR y
promoción son operaciones independientes; el deployment es siempre una
actividad humana externa. PR a QC no
promueve a QAM. Después de QC se requieren resultados de QA y
autorizaciones correspondientes; no simules su PASS.

Jenkins/WAS: referencia arquitectónica y operativa externa. WARI Agents puede
documentar dependencias y consideraciones respaldadas por evidencia, pero no
configura ni ejecuta Jenkins/WebSphere y no realiza despliegues. Nunca guardes
usuarios WAS, contraseñas, secretos ni tokens en Knowledge, estado,
prompts o repositorio.

## 8. Respuesta y pruebas

Reporta hallazgo, fuente, conclusión, resultado real, límites y siguiente
paso. Distingue IMPLEMENTADO, PROBADO, DOCUMENTADO PERO NO AUTOMATIZADO,
PENDIENTE DE CONFIGURACIÓN y PENDIENTE DE VALIDACIÓN. Un proveedor IA no
hereda la certificación PASS de otro. Codex es el proveedor inicial, sin
dependencia de Paseo. Una prueba conceptual valida una regla documentada,
no una integración real; marca por separado ejecución real y simulación.

## 9. Contrato de integración y seguridad

Aplicar `integrations/INTEGRATION-CONTRACT.md`: cada fuente declara
AVAILABLE/UNAVAILABLE, READ/WRITE efectivo y NOT AUTHORIZED según sesión
y solicitud. Capacidad del conector no concede permiso. Git local y GitHub
remoto son fuentes distintas; `origin` local no demuestra acceso remoto.
Si una integración falta, continuar con fuentes suficientes disponibles,
indicar bloqueo exacto y nunca inventar información o PASS. Ningún MCP,
API, CLI o conector puede evadir Production Boundary.

Nunca almacenar passwords, tokens, API keys, OAuth secrets, credenciales
WAS/Jira/GitHub ni secretos corporativos en Knowledge, STATE, prompts
persistidos, Git u outputs. Autenticación fuera del repositorio.

## 10. Rollback y reversión

«Haz rollback», «regresa al commit anterior», «vuelve a esta versión»,
«deshaz el último commit», «revierte estos cambios» y expresiones similares
inician **diagnóstico**, no ejecución inmediata. Antes de toda reversión,
JARVIS/INT identifica y comunica HU, workspace, worktree Git registrado,
rama exacta, HEAD y estado Git actuales, cambios locales sin commit,
commits afectados, commit/versión objetivo, estado de publicación remota
cuando se pueda verificar e impacto posible sobre commits posteriores y
otros desarrolladores. Si el remoto no es accesible, informa que su estado
es desconocido; no presupone que un commit es privado. Muestra diff/lista
de cambios afectados de forma dirigida. No infiere el objetivo solo del
contexto conversacional. Si falta HU/rama/objetivo inequívoco o aparecen
varias HUs, agrupa las aclaraciones en una consulta.

**Confirmación obligatoria:** presenta una propuesta concreta con rama,
HEAD, destino, commits intermedios, archivos/cambios afectados, método y
riesgos; solicita confirmación humana explícita **para esa reversión**.
Una orden inicial genérica o una respuesta sobre el objetivo no constituye
confirmación. Antes del WRITE, vuelve a comprobar HU ↔ worktree ↔ rama y
estado Git; si cambió algo material, actualiza la propuesta y confirma de
nuevo. Ante discordancia, **ROLLBACK BLOCKED**. Una HU = una rama = un
worktree = un workspace; no toca otro worktree, rama, workspace ni datos de
otra HU. Si la operación puede afectarlos, **ROLLBACK BLOCKED**.

Si hay cambios locales sin commit, informa qué archivos están afectados y
**bloquea** hasta recibir una decisión humana específica sobre cómo
preservarlos. No hace checkout, reset, clean ni otra operación que los
descarte automáticamente. Para un commit publicado o compartido, prefiere
una reversión trazable que conserve historial. No reescribe historial remoto
automáticamente. Reset destructivo, force push, eliminación de commits
publicados, reescritura remota, borrado de ramas o equivalentes exigen
tratamiento especial, exposición de impacto y confirmación adicional
específica; la confirmación de rollback genérico no los autoriza.

«Regresa al commit X» no autoriza force push. Informa rama, HEAD actual,
commit X, commits intermedios y publicación remota antes de proponer un
método. Si la reversión produce un commit, pide además confirmación expresa
para generar **ese commit en esa rama**; no lo presupone por haber
confirmado la reversión. Rollback tampoco autoriza push, PR, promoción ni
deployment. Si luego se pide PR, reinicia el flujo pre-PR con origen y
destino explícitos, alineamiento con `origin/produccion` cuando el remoto
esté disponible, validaciones y confirmación humana correspondiente.

Production Boundary también cubre rollback: puede leerse historial de
`produccion`, pero WARI Agents jamás ejecuta revert, reset, force push,
modificación de historial ni acción equivalente sobre/hacia `produccion`,
aunque se solicite o exista capacidad técnica. Es exclusivamente manual.

Tras una reversión autorizada, revisa solo el estado de la HU en
`<worktree>/workspace/<HU>/output/`. Marca como
desactualizados, actualiza o regenera según evidencia `ANALYSIS-STATE.md`,
`DEV-HANDOFF.md`, `CHANGE-MANIFEST.md` e `INTEGRATION-STATE.md` cuando
describan código revertido. Registra el alcance, commits/versión antes y
después y el punto válido de continuación. No reutiliza silenciosamente
estados obsoletos ni copia estados de otra HU.
