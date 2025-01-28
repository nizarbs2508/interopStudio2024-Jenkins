INTEROP STUDIO 2024

# 1. PRESENTACIÓN GENERAL

Interop Studio es una herramienta interna del equipo de
interoperabilidad de la DEI. Está en constante evolución y tiene como
objetivo agregar módulos utilitarios destinados a todo el equipo que
trabaja con herramientas de Art Décor, Gazelle o para generar ejemplos.
Interop Studio fue desarrollado en Java sobre la herramienta libre
Eclipse.

# 2. MÓDULO DE VALIDACIÓN CRUZADA 

## 2.1. Definición de validación cruzada

La validación cruzada consiste en cotejar las informaciones presentes en
el documento \"metadata\" de un fichero \"IHE_XDM.ZIP\" con aquellas
presentes en uno de los documentos CDA que contiene. Interop Studio
ofrece un módulo de cross-validation que permite: - una cross-validation
completa donde se evalúan todas las reglas y se produce un informe
global - una cross-validation atómica donde el usuario elige en una
lista la regla a evaluar

## 2.2. Cargando archivos CDA y METADATA

El módulo de validación cruzada de Interop Studio se encuentra en la
ventana principal de la aplicación. Es necesario, previamente, cargar el
CDA y el METADATA en el software. Tenga en cuenta que si ya ha utilizado
Interop Studio, los caminos de los archivos previamente cargados se
habrán memorizado y serán automáticamente recargados al iniciar la
aplicación. ◻ Pasos: - Seleccionar el archivo CDA. - Seleccionar el
archivo META. - Cross-Validar los archivos CDA y META a través del menú
Validación.

# 3. FUNCIONES DE CONTROL DEL CDA {#funciones-de-control-del-cda}

Interop Studio ofrece varias funciones de control y corrección del CDA.

## 3.1. Detección y corrección de UUID inválidos

Este módulo controla todos los UUID en el atributo id/@root de las
secciones y entradas y los corrige si es necesario generando un UUID
válido. El módulo se ejecutará pulsando el botón «Corrección de UUID de
elementos» que se encuentra en el cuadro «Controles del CDA» de la
ventana principal Las correcciones se realizan directamente en el
documento CDA. No se requiere ninguna otra acción. ◻ Pasos: -
Seleccionar el archivo CDA. - Corrección de los UUIDs de los elementos a
través del menú Controles del CDA.

## 3.2. Cálculo de hash

El algoritmo utilizado para calcular el hash es el SHA1 de Java. ◻
Pasos: - Seleccionar el archivo CDA. - Cálculo del hash con canonización
previa a través del menú Controles del CDA.

## 3.3. Control de los códigos Bio Loinc del CDA

Interop Studio permite un control de los códigos Bio Loinc presentes en
el CDA a través del botón «BIO LOINC» del cuadro «Controles del CDA» de
la ventana principal. El módulo controla los códigos LOINC presentes en
los
elementos//\*:observación/\*:code\[@codeSystem=\'2.16.840.1.113883.6.1\'\]/@code
del CDA. ◻ Pasos: - Seleccionar el archivo CDA. - Control de los códigos
BIO LOINC del CDA a través del menú Controles del CDA.

# 4. MÓDULO DE BÚSQUEDA EN MÚLTIPLES CDA

Este módulo evalúa una expresión Xpath en todos los documentos CDA de un
directorio dado. Permite, por ejemplo, buscar un error dado en un
conjunto de archivo CDA, o bien identificar los CDA que utilizan el
elemento priorityNumber etc\...

Este módulo es accesible a través del botón \"Módulo de búsqueda Xpath\"
en un directorio CDA del cuadro \"Búsqueda XPATH\" de la ventana
principal. El módulo almacena la última ruta de búsqueda utilizada, así
como la última expresión Xpath usada.

Esta información se almacena en el archivo xdmStudio.ini que se
encuentra en el directorio de la aplicación, en los elementos
\[MEMORY\]LAST-PATH-USED y \[DIAGNOSTIC\]LAST-REQUEST

Este módulo también permite buscar una expresión XPATH en un CDA o META.

## 4.1. Uso del módulo

El uso de este módulo requiere las siguientes operaciones: - Selección
del directorio en caso de que el directorio almacenado no sea
adecuado. - Entrada de una expresión booleana Xpath para probar en el
conjunto de archivos XML del CDA. ◻ Pasos: - Seleccione el archivo
CDA. - Módulo de búsqueda XPATH en un listado de CDA a través del menú
Buscar XPATH.

# 5. APIS

La ANS ofrece varios servicios de validación en línea, accesibles por
APIs. Interop Studio 2024 integra estos servicios en línea en sus
funcionalidades.

## 5.1. Validaciones simples

Los iconos de validaciones simples permiten validar en línea los
archivos CDA y Metadata seleccionados, o bien validarlos. ◻ Pasos: -
Seleccionar el archivo CDA y/o META. - Validar por API el archivo CDA. -
O Validar en línea el archivo META. - O Cross-Validar los archivos CDA y
META

## 5.2. Validación de masa

Después de seleccionar un directorio que contiene los CDA en su raíz, la
validación se inicia secuencialmente para cada uno de los CDA.

Los archivos validados se copian en un directorio \"- VALID_CDA - \"  
Los archivos en fallo (Validación fallida) se copian en el directorio
\" - INVALID_CDA - \" ◻ Pasos: - Seleccionar el directorio que contiene
los archivos CDA. - Validar por API el conjunto de los archivos CDA.

# 6. MÓDULO DE GENERACIÓN DE IHE_XDM

## 6.1. Generación masiva de archivos

Desde la ventana principal es posible generar archivos XDM en masa a
partir de un directorio que contiene un conjunto de archivos CDA. Se le
ofrecerá seleccionar el directorio en el que se encuentran sus archivos
CDA.

La generación de archivos correspondientes a los CDA del directorio se
hará según el siguiente proceso: - Recorrido de los CDA del directorio -
Generación del fichero Metadata correspondiente al CDA en curso. -
Validación en línea de los metadatos y Cross-Validation a través de las
API de la ANS. Si una de estas validaciones devuelve un error o la API
no está disponible, se cancela la generación del archivo. El archivo CDA
se copia en un subdirectorio llamado: \"- CDA_INVALIDES -\". - Si las
validaciones son positivas, se crea un subdirectorio con el nombre del
archivo CDA, que contendrá el archivo IHE_XDM.ZIP, así como n
subdirectorio llamado «Contenido del ZIP» en el cual se encontrará el
archivo descomprimido. - El resultado de este proceso se mostrará en la
consola de logs para cada uno de los CDA.

## 6.2. Presentación del módulo

Interop Studio ofrece un módulo de generación de metadatos y archivo ZIP
IHE_XDM.ZIP. El acceso a este módulo se hace mediante el botón «Meta»,
disponible en dos lugares de la ventana principal.

Al abrir el módulo, el CDA cargado en la ventana principal (ventana
anterior) se carga automáticamente en el módulo de generación XDM. El
cuadro rosa es una lista de los CDA que se incluirán en el archivo
IHE_XDM.ZIP final. Actualmente, el módulo funciona con varios CDA pero
no se ha realizado ninguna validación del Metadata finalmente producido.

## 6.3. Generación de metadatos

Le metadata produit peut être sauvegardé sous forme de fichier XML. A
l'issue de la sauvegarde dans le répertoire de votre choix, le fichier
sera ouvert dans votre éditeur XML paramétré par défaut dans Windows.

## 6.4. Generación completa de IHE_XDM.ZIP

- Etapas :
  - Selección del repertorio de generación.
  - Generación del ZIP.

# 7. MÓDULO DE PARAMETRAJE

## 7.1. Configuración de los caminos de aplicación

Esta interfaz depende de todos los caminos utilizados en la aplicación.
Estas luces están almacenadas en el archivo "config.properties" debajo del
Usuario del expediente. El camino del archivo se menciona en la base de la interfaz.

## 7.2 Mapeo de OID

Esta interfaz se utiliza para agregar, actualizar o eliminar
OID de un ejemplo CDA. Los données de esta interfaz están almacenados en
el archivo "config.properties" en el expediente del Usuario. El camino del
El archivo se menciona en la interfaz.

## 7.3 Parámetro interopStudio2024.ini

Esta interfaz se utiliza para explotar el archivo
interopStudio2024.ini que está almacenado en el expediente del usuario. Cette
interfaz permite el ajuste, la puesta en marcha y la supresión de una
propiedad registrada en este archivo.

# 8. ART DECOR

## 8.1 Supresión de elementos suprimidos en una plantilla Art Decor

Este módulo permite suprimir las estatuas 'retiradas' y 'canceladas' des
JDV, des entremeses y des secciones. ◻ Etapas: - Haga clic en el menú Arte
Decoración después del segundo menú Supresión de elementos superiores en un
plantilla de decoración de arte. - Elija el archivo de decoración artística. Validador.

## 8.2 Módulo Estadística

Este módulo calcula el número de estatuas diferentes en un archivo a partir de
de decoración artística. ◻ Pasos: - Haga clic en el menú Art Decor y luego en el sub
Menú del módulo de estadísticas. - Elija el archivo de decoración artística. -
Para validar.

# 9. Herramientas externas

Este módulo le permite iniciar cualquier jar/exe a través de la aplicación. Él
También le permite iniciar las diversas herramientas ANS a través del menú Herramientas ANS.
