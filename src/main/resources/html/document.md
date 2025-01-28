# Introduction 

InteropStudio 2024 est un outil interne pour l’équipe Interopérabilité de la DEII. 

Il est en évolution permanente et a vocation à agréger des modules utilitaires destinés à l’ensemble de l’équipe amenée à travailler sur l’outillage Art Décor, Gazelle, ou encore sur la génération d’exemples.  

InteropStudio 2024 a été développé en Java sur l’outil libre Eclipse. 

InteropStudio 2024 est une application développée en trois langues : 

- Français  
- Anglais 
- Espagnol 

Il existe un bouton « Accueil » pour la remise à zéro de l’application, une horloge pour afficher l’heure actuelle et la date. 

Le logo de l’ANS est cliquable redirigeant vers le site de l’ANS <https://esante.gouv.fr/> 

InteropStudio 2024 présente un ensemble de fonctionnalités qui seront expliquées dans la suite du document. Parmi ces fonctionnalités, on trouve :

- Le service BOM pour la suppression de l’encodage BOM des documents CDA et META.

- L’ouverture avec un éditeur externe des documents CDA et META.

- La génération du document PDF du CDA choisi. Cette génération de PDF se fait par l’outil FOP qui existe dans le dossier \TestContenuCDA-3-0-main\FeuilleDeStyle\FOP. Lors de la première génération du document PDF on doit spécifier le chemin du dossier FOP via l’interface “Paramétrage des chemins de l’application” (Chemin du dossier FOP). Le chemin de FOP est sous la  forme : 

  “PATH\_TO\_TEST\_CONTENU\_CDA\TestContenuCDA-<version>\FeuilleDeStyle\FOP\fop-2.8\fop”

Dans les sections qui suivent, on va présenter les différents principaux modules de InteropStudio.

# Module de validation 
## Validation du fichier CDA

L’ANS propose plusieurs services de validation en ligne, accessibles par APIs. InteropStudio 2024 intègre ces services en ligne dans ses fonctionnalités.

Via le menu Validation de la page principale, on peut trouver les icônes de validations simples, qui permettent de valider en ligne les fichiers CDA. 

Pour valider un fichier CDA, il faut suivre la procédure suivante : 

- Sélectionner le fichier CDA
- Cliquer sur le menu Validation
- Cliquer sur le sous-menu Valider par API le fichier CDA

  Le résultat de la validation apparaitra sur l’écran : les documents créés à gauche et le schematron de validation Gazelle à droite.

## Validation de l’ensemble des fichier CDA 

La validation par API de l’ensemble des fichiers CDA fait le même traitement que la validation d’un fichier CDA, mais elle prend en compte un dossier qui contient des fichiers CDA et non pas un seul fichier. 

Pour valider un ensemble de fichiers CDA, il faut suivre la procédure suivante : 

- Cliquer sur le menu Validation 
- Cliquer sur le sous-menu Valider par API l’ensemble des fichiers CDA
- Choisir le dossier qui contient les documents CDA à valider

  Le résultat de la validation apparaitra sur l’écran : les documents créés dans la partie gauche.

## Validation du fichier META 

Via le menu Validation de la page principale, on peut trouver les icônes de validations simples, qui permettent de valider en ligne les fichiers META. 

Pour valider un fichier META, il faut suivre la procédure suivante : 

- Sélectionner le fichier META
- Cliquer sur le menu Validation
- Cliquer sur le sous-menu Valider en ligne le fichier META

  Le résultat de la validation apparaitra sur l’écran : les documents créés à gauche et la validation Gazelle XDSMetadata à droite.


## Cross Validation CDA-META 

La cross-validation consiste à recouper les informations présentes dans le document metadata d’un fichier IHE\_XDM.ZIP avec celles présentes dans l’un des documents CDA qu’il contient. 

Pour cross-valider les fichiers CDA et META, il suffit de :  

- Sélectionner les fichiers CDA et META
- Cliquer sur le menu Validation
- Cross-Valider les fichiers CDA et META 

Le résultat de la cross validation apparaitra sur l’écran : les documents créés à gauche et la cross validation Gazelle à droite.

## Affichage des rapports de validation 


InteropStudio 2024 permet d’afficher le dernier rapport de validation (validation par API d’un fichier CDA, validation en ligne d’un fichier META ou cross-validation). 

Pour afficher un rapport de validation, il suffit de : 

- Cliquer sur le menu Validation 
- Cliquer sur le sous-menu Afficher le dernier rapport de validation

  Le résultat apparaitra sur l’écran : les documents déjà créés lors de la dernière validation à gauche et la schematron de validation Gazelle à droite.

## Ouverture des derniers rapports de validation 

InteropStudio 2024 permet d’afficher les deniers rapport de validation sur l’API. 

Pour les consulter, il suffit de :

- Cliquer sur le menu Validation 
- Cliquer sur le sous-menu ouvrir les derniers rapports de validation
- Une nouvelle fenêtre qui contient les quatre rapports de validation apparaitra.

# Fonctions XDM 
## Génération du métadata 

InteropStudio 2024 propose un module de génération de métadata et de fichier zippé IHE\_XDM.ZIP. 

Pour générer le fichier IHE\_XDM.ZIP, il suffit de : 

- Choisir un fichier CDA
- Cliquer sur le menu Fonctions XDM
- Cliquer sur le sous-menu Générer un fichier de métadonnées XDM :
- Une nouvelle fenêtre apparaitra
- Cliquer sur le bouton « Générer » le métadata :
- Le métadata sera affiché sur l’écran. Cette fenêtre permet d’ajouter ou de supprimer un fichier CDA pour générer un métadata pour un ensemble de fichier CDA. Elle permet aussi de sauvegarder le fichier généré, de générer le fichier IHE\_XDM.ZIP complet, de valider le fichier META ou de cross-valider le fichier CDA et le fichier META. Elle permet aussi de vérifier la présence d’erreurs dans le métadata et de générer un fichier PDF de document CDA, ce document qui résulte de la transformation xml du document CDA.  


## Génération des archives XDM 

InteropStudio 2024 propose un module de génération des archives XDM pour l’ensemble des CDA d’un répertoire. 

La procédure de génération est la suivante : 

- Cliquer sur le menu Fonctions XDM
- Cliquer sur le sous-menu Générer des archives XDM pour l’ensemble des CDA d’un répertoire
- Choisir le dossier qui contient les fichiers CDA
- Une nouvelle fenêtre de validation apparaitra avec deux options de validations (Valider le métadata en ligne, cross-Valider le métadata et le CDA en ligne)
- Valider en cliquant sur le bouton « Générer »

  Un nouveau dossier IHE\_XDM sera créé sous la racine du dossier choisi. Ce dossier contient les CDA valides et les CDA invalides.  

  Pour les CDA valides un document PDF est généré, ce document qui résulte de la transformation xml du document CDA valide.  

## Sélectionner une archive XDM 

Via ce menu, on peut sélectionner un fichier CDA et un fichier META, il suffit de : 

- Cliquer sur le menu Fonctions XDM
- Cliquer sur le sous-menu Sélectionner une archive XDM
- Sélectionner un fichier IHE\_XDM.ZIP

  Les fichiers CDA et META, déjà présents dans le fichier ZIP, seront sélectionnés dans les champs de texte relatifs au CDA et au META. 


## Affichage de l’arborescence CDA/META 


Ce menu permet d’afficher l’arborescence des fichiers CDA et META, il suffit de : 

- Choisir les fichiers CDA et META 
- Cliquer sur le menu Fonctions XDM
- Cliquer sur le sous-menu Arborescence CDA/META

  Une nouvelle fenêtre affichant l’arborescence CDA/META sera ouverte.

# Contrôles CDA 
## Correction des UUID des éléments <id> 

InteropStudio 2024 propose plusieurs fonctions de contrôle et de correction du CDA. 

Ce module contrôle l'ensemble des UUID dans l'attribut id/@root des sections et des entrées, les corrige si nécessaire en générant un UUID valide.

Pour corriger les UUID il faut : 

- Choisir un fichier CDA dans la fenêtre principale
- Cliquer sur le menu Contrôles CDA
- Cliquer sur le sous-menu Correction des UUID des éléments <id>

  La liste des UUID valides et invalides (déjà corrigés) sera affichée sur l’écran


## Calcul du hash avec canonisation préalable 

Cette opération consiste à effectuer un calcul du hash du CDA chargé dans l’outil InteropStudio 2024, il suffit de : 

- Charger le fichier CDA
- Cliquer sur le menu Contrôles CDA
- Cliquer sur le sous-menu Calcul du hash avec canonisation préalable et valider

  Le résultat du hash et de sa taille sera affiché sur l’écran principal. 


## Contrôle des codes BIO LOINC du CDA 


Le module contrôle les codes LOINC présents dans les éléments [//*:observation/*:code\[@codeSystem='2.16.840.1.113883.6.1'\]/@code](mailto://*:observation/*:code%5b@codeSystem='2.16.840.1.113883.6.1'%5d/@code) du CDA. 

Pour effectuer le contrôle des codes BIO LOINC du CDA, il faut : 

- Charger le fichier CDA
- Cliquer sur le menu Contrôles CDA
- Cliquer sur le sous-menu Contrôle des codes BIO LOINC du CDA

  La liste des codes LOINC connus et inconnus sera affichée dans l’écran principal de l’application.

 
## Supprimer l'encodage BOM de tous les fichiers XML d'un répertoire 


Ce module permet de supprimer l’encodage BOM de tous les fichiers XML d’un répertoire. 

Cette fonctionnalité est effectuée en suivant les étapes suivantes :  

- Charger le fichier CDA
- Cliquer sur le menu Contrôles CDA
- Cliquer sur le sous-menu Supprimer l'encodage BOM de tous les fichiers XML d'un répertoire
- Choisir le répertoire contenant les fichiers CDA

  Le message de validation est affiché sur l’écran principal de InteropStudio 2024. 


# Module XPATH 
## Module de recherche Xpath dans un répertoire de CDA 

Ce module permet d’évaluer une expression Xpath dans l’ensemble des documents CDA d’un répertoire donné. Il permet, par exemple, de rechercher une erreur donnée dans un ensemble de fichier CDA. L’expression XPATH doit renvoyer un booléen. 

- Cliquer sur le menu Module Xpath 
- Cliquer sur le sous-menu Module de recherche XPATH dans un répertoire de CDA 
- Une nouvelle fenêtre sera affichée
- Sélectionner le répertoire de recherche
- Choisir l’expression Xpath via la liste déroulante, si elle existe ou l’écrire dans le champ expression Xpath à tester
- Choisir l’option « valide » pour la recherche (valide ou pas l’expression Xpath)
- Cliquer sur le bouton « recherche »

  Le résultat des fichiers validant ou pas l’expression Xpath, sera affiché dans la table en bas de la fenêtre. 


## Recherche XPATH dans un fichier CDA 

Ce module permet de rechercher une expression Xpath valide dans un fichier CDA.

- Cliquer sur le menu Module Xpath
- Cliquer sur le sous-menu Recherche XPATH dans un fichier CDA
- Ecrire l’expression Xpath dans le champ expression Xpath à tester
- Cliquer sur le bouton « recherche »

  Si l’expression Xpath est valide, le curseur sera déplacé sur la ligne correspondante dans la partie gauche et sur la partie droite la ligne recherchée sera affichée. 

## Recherche XPATH dans un fichier META 


Ce module permet de rechercher une expression Xpath valide dans un fichier META. 

Il vous suffit de suivre les étapes suivantes :

- Cliquer sur le menu Module Xpath
- Cliquer sur le sous-menu Recherche XPATH dans un fichier META
- Ecrire l’expression Xpath dans le champ expression Xpath à tester
- Cliquer sur le bouton « recherche »

  Si l’expression Xpath est valide, le curseur sera déplacé sur la ligne correspondante dans la partie gauche et sur la partie droite la ligne recherchée sera affichée. 


# Module Art Decor (Module KOUDOU) 
## Accès au module de retraitement des schématrons 


Le module de retraitement permet d'effectuer différentes actions sur un ou plusieurs <a name="_int_r9sbklis"></a>packages de schematrons issus d'Art Decor. Si on souhaite travailler sur un seul package de schematrons, il faudra sélectionner manuellement le fichier \*.sch du package. Si, en revanche, on souhaite effectuer des opérations sur tous les packages d'un répertoire Windows, il suffira de sélectionner le répertoire avec le bouton adéquat. 


Pour effectuer cette manipulation, il suffit de :

- Cliquer sur le menu Art Decor
- Cliquer sur le sous-menu Accès au module de retraitement des schématrons
- Choisir le fichier d'entrée du schématron
- Cocher le choix de traitement à effectuer (adresse et/ou JDV)
- Cliquer sur le bouton « Retraiter » 

  Le résultat sera affiché dans la liste en bas de la fenêtre. 

## Suppression des éléments supprimés dans un template Art Decor 

- Cliquer sur le menu Art Decor
- Cliquer sur le sous-menu Suppression des éléments supprimés dans un template Art Decor
- Choisir le fichier à traiter (.xml), puis valider

  Le fichier généré sera affiché dans l’écran principal de l’application. 


## Module Statistiques 

Ce module permet d’effectuer les statistiques entre fichiers art decor. 

Pour faire l’analyse de ces fichiers, il faut : 

- Cliquer sur le menu Art Decor
- Cliquer sur le sous- menu Module Statistiques
- Choisir le fichier approprié

  Le résultat sera affiché sur l’écran principal de l’API. Il est sous forme de nombre de JDV selon le statut, nombre d’entrées selon le statut, nombre de sections selon le statut et nombre d’autres éléments selon le statut.

 
# Outils externes 

Ce module permet de lancer n’importe quel “.jar” ou “.exe” sur le poste de travail ou même lancer l’Outils ANS. 

## Outils ANS 

Ce module permet de lancer l’Outils ANS.

- Cliquer sur le menu Outils Externes
- Cliquer sur le sous-menu Outils ANS

  L’Outils ANS sera lancé. 


## Lancement outil (jar/exe) 

Ce module permet de lancer n’importe quel fichier jar ou exe. 

- Cliquer sur le menu Outils Externes
- Cliquer sur le sous-menu Lancement outil (jar/exe)

  L’outil sera lancé. 

# Module de paramétrages 
## Paramétrage des chemins de l’application 

Ce module permet de paramétrer les chemins en local des modules validations. Ces chemins sont stockés dans le fichier “config.properties”.

- Cliquer sur le menu Paramétrages 
- Cliquer sur le sous-menu Paramétrage des chemins de l'application
- Une nouvelle fenêtre d’affiche
- Mettre à jour les champs de texte
- Cliquer sur le bouton « Enregistrer » 

La mise à jour sera enregistrée dans le fichier “config.properties”. 


## Mapping des OIDs 

Ce module permet de mettre à jour, de supprimer ou d’ajouter un OID.

- Cliquer sur le menu Paramétrages
- Cliquer sur le sous-menu Mapping des OIDs
- Une nouvelle fenêtre s’affiche
- Ajouter, mettre à jour ou supprimer un OID (OID + Nom du service gazelle), puis valider

## Paramétrage InteropStudio2024.ini 

Ce module permet d’exposer le fichier InteropStudio2024.ini sous forme de sections ainsi que le fichier complet.

- Cliquer sur le menu Paramétrages 
- Cliquer sur le sous-menu Paramétrage InteropStudio2024.ini
- Une nouvelle fenêtre sera affichée
- Modifier, ajouter ou supprimer la donnée au niveau de la dernière section, là où il y a le fichier “.ini” complet et cliquer sur le bouton « Mettre à jour »

  La mise à jour sera effectuée dans le fichier InteropStudio2024.ini ainsi que sur l’écran au niveau des sections. 


## Fichier LOG 

Ce module permet d’afficher le fichier log des validations.

- Cliquer sur le menu Paramétrages
- Cliquer sur le sous-menu Fichier LOG

# Module de documentation 
## Documentation de l’application 

Ce module permet d’afficher la documentation sous forme html de l’application.

- Cliquer sur le menu Documentation 
- Cliquer sur le sous-menu Documentation de l’application 

  La documentation sera affichée dans la partie droite de l’écran. 


## Lisez-moi 

Ce module permet d’afficher le lisez-moi de l’application. 

- Cliquer sur le menu Documentation 
- Cliquer sur le sous-menu Lisez-moi

  Le lisez-moi sera affiché dans une nouvelle fenêtre. 