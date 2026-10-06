# WARI CONTEXT

WARI es un sistema empresarial. El Git oficial del producto es
<WARI-APP>/wari-fortalecimiento/. Esta plataforma reside en el repositorio
hermano <WARI-APP>/wari-agents/; los worktrees HU del Git WARI viven en
<WARI-APP>/worktrees/<HU>/. Resuelve WARI-APP como el directorio padre de
este proyecto, sin asumir nombre físico ni ubicación absoluta. El
proyecto WARI no necesita contener agentes, Knowledge ni configuración
del proveedor. No asumir HU activa por existir una carpeta o un estado.

Entre los módulos observados pueden figurar wari-app, wari-common y
wari-cavaliWeb. Tecnologías posibles incluyen Java, Spring, Hibernate,
Struts, Oracle y Maven; comprobar por módulo antes de afirmar uso. La
implementación actual se determina desde código y Git, no desde esta lista.

JARVIS es el orquestador vigente; AGENT-ORQ.md conserva compatibilidad. Los
roles vigentes son ANALISTA, DESARROLLADOR, QA, INTEGRADOR y DOCUMENTADOR.
Cada HU se vincula a un worktree Git registrado y rama exacta <BRANCH>:
por defecto <HU>, salvo nombre explícito del desarrollador. Input/output
viven en <worktree>/workspace/<HU>/ y son locales; el framework
compartido vive aquí y puede empaquetarse/versionarse por separado.
El estado previo de cualquier HU, si existe, permanece local fuera de
Knowledge hasta migración individual autorizada. No cargar estado de
otra HU ni asumir que una HU concreta está activa.

La configuración de cada estación vive en
`<WARI-APP>/.wari/environment.local.yaml`, fuera del repositorio compartido.
Su plantilla es `config/environment.template.yaml`. Las rutas y comandos allí
declarados son la única fuente local para JDK, Maven, Git y compilación. No
existen comandos locales de pruebas ni empaquetado. WebSphere local es contexto
de ejecución de la aplicación, no una herramienta operada por los agentes; su
inicio, detención, configuración y despliegue quedan fuera de alcance. Un campo no configurado produce
`BLOCKED_ENVIRONMENT` solo para la tarea que lo necesita.

Knowledge persistente y versionable vive bajo knowledge/wari/; contiene
solo hechos reutilizables con clasificación y fuente. Context es el
subconjunto mínimo de Knowledge, estado y evidencia necesario para una
tarea. No copiar requerimientos completos ni documentación externa de
forma indiscriminada.

Fuentes posibles: Jira para HU y adjuntos; Confluence para CU/DT y
documentación; código y Git local para implementación y cambios; Git
remoto bvlperu/wari-fortalecimiento para ramas/PR reales. Jenkins y WebSphere
son contexto externo y no son operados por agentes. Cada integración requiere
configuración real. Ausencia de acceso se informa como PENDIENTE DE
CONFIGURACIÓN/VALIDACIÓN, sin fabricar contenido o PASS.

El proceso conocido usa rama acordada para la HU, alineada con
produccion, verifica alineamiento antes del PR y excluye commits/archivos
ajenos. El procedimiento oficial de alineamiento, formatos documentales
restantes y gates exactos de QC/QAM siguen pendientes. QA externa realiza
pruebas funcionales/integrales. Toda acción hacia producción queda manual y
fuera de WARI Agents.

Codex sirve para la prueba inicial. Las instrucciones deben ser
utilizables por otro proveedor compatible sin depender del historial
de Paseo; cada proveedor requiere certificación propia.

No escanear WARI al instalar, abrir VS Code o iniciar una consulta/HU.
Knowledge First, descubrimiento progresivo y búsqueda dirigida mantienen
el contexto mínimo. El PR apunta solo al destino explícito actual;
un PR previo no implica QAM. No eliminar ramas automáticamente.
