# À propos de Connecteur WOPI pour LOOL
* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Région Nouvelle Aquitaine
* Développeur(s) : CGI
* Financeur(s) : Région Nouvelle Aquitaine
* Description : Module permettant de se connecter à Lool.

## Présentation du module
Cet outil permet de créer, modifier des documents de type texte, classeur et présentation. Plusieurs personnes peuvent intervenir sur un même document en simultané. L’accès à l’outil peut aussi se faire à partir de l’espace documentaire (Documents).

## Configuration
<pre>
 {
  "config": {
    ...
    "wopi": {
        "provider": {
            "type": "",
            "url": ""
        },
        "hour-duration-token": 10,
        "templates": ["odt", "odp", "ods"],
        "server_capabilities": {
            "DisableCopy": false,
            "DisablePrint": false,
            "DisableExport": false,
            "HideExportOption": false,
            "DisableInactiveMessages": false,
            "HideUserList": false,
            "HideSaveOption": false,
            "EnableShare": false,
            "EnableInsertRemoteImage": true,
            "HidePrintOption": false,
            "UserCanNotWriteRelative": true,
            "EnableOwnerTermination": false
        }
    }
  }
}
</pre>