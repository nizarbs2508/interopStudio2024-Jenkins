1. GENERAL PRESENTATION 

InteropStudio 2024 is an internal tool for the DEII Interoperability team. 

It is in constant evolution and aims to aggregate utility modules intended for the entire team working on Art Décor and Gazelle tools, or even on the generation of examples.  

InteropStudio 2024 was developed in Java on the free Eclipse tool. 

InteropStudio 2024 is an application developed in three languages: 

- French  
- English 
- Spanish 

There is a “Home” button for resetting the app, a clock to display the current time and date. 

The ANS logo can be clicked redirecting to the ANS website <https://esante.gouv.fr/> 

InteropStudio 2024 presents a set of features which will be explained later in the document. Among these features, we find:

- The BOM service for removing BOM encoding from CDA and META documents.

- Opening CDA and META documents with an external editor.

- Generation of the PDF document of the chosen CDA. This PDF generation is done using the FOP tool which exists in the \TestContenuCDA-3-0-main\FeuilleDeStyle\FOP folder. When generating the PDF document for the first time, you must specify the FOP folder path via the “Application path settings” interface (FOP folder path). The path of FOP is in the form: 

  “PATH\_TO\_TEST\_CONTENT\_CDA\TestContentCDA-<version>\StyleSheet\FOP\fop-2.8\fop”

In the following sections, we will present the different main modules of InteropStudio.


2. CROSS-VALIDATION MODULE 

2.1.    Definition of cross-validation 

Cross-validation consists of cross-checking the information present in the “metadata” document of an IHE_XDM.ZIP file with that present in one of the CDA documents it contains. 

Interop Studio offers a cross-validation module allowing: 

 - a complete cross-validation where all the rules are evaluated and an overall report produced 

 - an atomic cross-validation where the user chooses the rule to evaluate from a list

2.2.    Loading CDA and METADATA files 

The Interop Studio cross-validation module is accessible in the main application window. It is necessary, beforehand, to load the CDA and the METADATA into the software. 

Note that if you have already used Interop Studio, the paths of the previously loaded files will have been memorized and will be automatically reloaded when the application is launched.

* Steps: 

 - Select the CDA file.
 
 - Select the META file.
 
 - Cross-Validate CDA and META files via the Validation menu.


3. CDA CONTROL FUNCTIONS 

Interop Studio provides several CDA monitoring and correction functions. 

3.1.    Detecting and fixing invalid UUIDs 

This module checks all UUIDs in the id/@root attribute of sections and entries and corrects them if necessary by generating a valid UUID. 

The module will be executed by pressing the “Fix UUIDs of <id> elements” button which is located in the “CDA Controls” frame of the main window 

Corrections are made directly on the CDA document. No further action is necessary. 

* Steps: 

 - Select the CDA file.
 
 - Fixed UUIDs of <id> elements via the CDA Controls menu.

3.2.    Hash calculation 

The algorithm used for calculating the hash is Java's SHA1. 

* Steps: 

 - Select the CDA file.
 
 - Hash calculation with prior canonicalization via the CDA Controls menu.

3.3.    Control of Bio Far from CDA codes 

Interop Studio allows control of the Bio Loinc codes present in the CDA via the “BIO LOINC” button in the “CDA Controls” frame of the main window. 

The module controls the LOINC codes present in the //\*:observation/\*:code[@codeSystem='2.16.840.1.113883.6.1']/@code elements of the CDA.

* Steps: 

 - Select the CDA file.
 
 - Control of CDA BIO LOINC codes via the CDA Controls menu.
 

4. SEARCH MODULE IN MULTIPLE CDAs 

This module allows you to evaluate an Xpath expression in all CDA documents in a given directory. 

It allows, for example, to search for a given error in a set of CDA files, or to list the CDAs which use the priorityNumber element, etc. 

This module is accessible via the “Xpath search module in a CDA directory” button in the “XPATH search” frame of the main window. 

The module remembers the last search directory used, as well as the last Xpath expression used. 

This information is stored in the xdmStudio.ini file which is located in the application directory, in the [MEMORY]LAST-PATH-USED and [DIAGNOSTIC]LAST-REQUEST elements 

4.1.    Using the module 

Using this module requires performing the following operations: 

 - Selection of the directory in case the stored directory is not suitable. 

 - Enter a Boolean Xpath expression to test in all CDA XML files.
 
 * Steps: 

 - Select the CDA file.
 
 - XPATH search module in a CDA directory via the XPATH Search menu.


5.APIS 

The ANS offers several online validation services, accessible via APIs. 

Interop Studio 2024 integrates these online services into its functionalities. 

5.1.    Simple validations 

The simple validation icons allow you to validate the selected CDA and Metadata files online, or to cross-validate them.

 * Steps: 

 - Select the CDA and/or META file.
 
 - Validate the CDA file using API.
 
 - OR Validate the META file online.
 
 - OR Cross-Validate CDA and META files
 
5.2.    Mass validation 

After selecting a directory containing the CDAs in its root, validation is launched sequentially for each of the CDAs. 

The validated files are copied to a directory “---VALID_CDA ---”  

Failed files (Validation Failed) are copied to the “---INVALID_CDA ---” directory

 * Steps: 

 - Select the directory containing the CDA files.
 
 - Validate all CDA files using API.


6. IHE_XDM GENERATION MODULE 

6.1.    Mass archive generation 

It is possible, from the main window, to mass generate XDM archives from a directory containing a set of CDA files. 

You will be asked to select the directory where your CDA files are located. 

The generation of archives corresponding to the CDAs in the directory will be done according to the following process: 

 - Browse the CDAs in the directory 

 - Generation of the Metadata file corresponding to the current CDA. 

 - Online validation of Metadata and Cross-Validation via ANS APIs. If one of these validations returns an error or the API is unavailable, the generation of this archive is canceled. 
 
   The CDA file is copied to a subdirectory called: “---CDA_INVALIDES ---”. 

 - If the validations are positive, a subdirectory bearing the name of the CDA file is created, which will contain the IHE_XDM.ZIP archive, as well as a subdirectory called “ZIP Content” in which we will find the decompressed archive . 

 - The result of this process will be displayed in the log console for each of the CDAs. 

6.2.    Module presentation 

Interop Studio offers a Metadata and zipped file generation module IHE_XDM.ZIP. 

Access to this module is via the “Meta” button, available in two places on the main window. 

When opening the module, the CDA loaded in the main window (previous window) is automatically loaded into the XDM generation module. 

The pink area is a list of CDAs to include in the final IHE_XDM.ZIP file. 

Currently, the module works with several CDAs but no validation of the Metadata finally produced has been carried out.

6.3.    Generation of Metadata 

The produced metadata can be saved as an XML file. 

After saving in the directory of your choice, the file will be opened in your XML editor set by default in Windows. 

6.4.    Complete generation of IHE_XDM.ZIP 
 
  * Steps:
  
 - Selection of the generation directory. 

 - Generation of the ZIP.
 
 - Generate XDM archives for all CDAs in a directory via the XDM Functions menu.
 
 
 7. PARAMETER MODULE
 
 7.1.    Setting up application paths
 
 This interface contains all the paths used in the application. These paths are stored in the config.properties file under the User folder. The file path is mentioned at the bottom of the interface.
 
 7.2 Mapping of OIDs
 
 This interface is used to add, update, or delete an OID from a CDA example. The data for this interface is stored in the config.properties file under the User folder. 
 
 The file path is mentioned at the bottom of the interface.
 
 7.3 InteropStudio2024.ini configuration
 
 This interface is used to operate the interopStudio2024.ini file which is stored in the User folder.
 
 This interface allows the addition, updating and deletion of a property saved in this file.
 
 8. ART DECOR 

8.1 Deleting deleted elements in an Art Decor template 

This module allows you to remove the 'retired' and 'cancelled' statues from JDVs, entrances and sections. 

 Steps : 

Click on the Art Decor menu then on the Deletion of deleted elements in an Art Decor template submenu. 

Choose the file from art decor. 

To validate. 

 
8.2 Statistics module 

This module calculates the number of different statues in a file from art decor. 

Steps : 

Click on the Art Decor menu then on the Statistics Module submenu. 

Choose the file from art decor. 

To validate. 

 
9. External Tools 

This module allows you to launch any jar/exe via the application. It also allows you to launch the various ANS tools via the ANS Tools menu.
 