# À propos de Connecteur WOPI pour LOOL et OnlyOffice
* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Région Nouvelle Aquitaine
* Développeur(s) : CGI, Edifice
* Financeur(s) : Région Nouvelle Aquitaine
* Description : Module permettant de se connecter à Lool ou OnlyOffice.

## Présentation du module
Cet outil permet de créer, modifier des documents de type texte, classeur et présentation. Plusieurs personnes peuvent intervenir sur un même document en simultané. L'accès à l'outil peut aussi se faire à partir de l'espace documentaire (Documents).

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
        },
        "onlyoffice": {
            "editorConfig": {
                "customization": {
                    "compactHeader": true,
                    "compactToolbar": true,
                    "hideRulers": true,
                    "hideRightMenu": true,
                    "statusBar": false,
                    "plugins": false,
                    "layout": {
                        "statusBar": false,
                        "toolbar": {
                            "home": true,
                            "insert": true,
                            "draw": true,
                            "layout": true,
                            "references": true,
                            "collaboration": false,
                            "protection": false,
                            "view": true
                        }
                    }
                }
            }
        }
    }
  }
}
</pre>

### Personnalisation de l'éditeur OnlyOffice

La section `wopi.onlyoffice.editorConfig.customization` permet de configurer l'apparence de l'éditeur OnlyOffice. Les paramètres sont transmis via le champ `docs_api_config` dans le formulaire POST de la page hôte WOPI ([doc officielle](https://api.onlyoffice.com/docs/docs-api/using-wopi/host-page/)).

#### Paramètres Community (standard)

> 📎 Doc : [Customization - Standard branding](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-standard-branding/)

| Paramètre | Type | Défaut | Description |
|---|---|---|---|
| `compactHeader` | booléen | `true` | En-tête compact ([doc](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-standard-branding/#compactheader)) |
| `compactToolbar` | booléen | `true` | Barre d'outils compacte ([doc](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-standard-branding/#compacttoolbar)) |
| `hideRulers` | booléen | `true` | Masquer les règles ([doc](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-standard-branding/#hiderulers)) |
| `hideRightMenu` | booléen | `true` | Masquer le panneau droit ([doc](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-standard-branding/#hiderightmenu)) |
| `statusBar` | booléen | `false` | ⚠️ Non documenté en standard. Doublé par `layout.statusBar` en White Label. |
| `plugins` | booléen | `false` | Désactive les modules complémentaires et masque l'onglet ([doc](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-standard-branding/#plugins)) |

#### Paramètres Developer Edition + White Label

> 📎 Doc : [Customization - White label](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-white-label/) — nécessite la licence Developer Edition avec White Label. Ignorés en Community.

| Paramètre | Type | Défaut | Description |
|---|---|---|---|
| `layout.statusBar` | booléen | `false` | Barre d'état ([doc](https://api.onlyoffice.com/docs/docs-api/usage-api/config/editor/customization/customization-white-label/#layoutstatusbar)) |
| `layout.toolbar.home` | booléen | `true` | Onglet Accueil. (peut être déprécié) |
| `layout.toolbar.insert` | booléen | `true` | Onglet Insertion (éditeur de document) |
| `layout.toolbar.draw` | booléen | `true` | Onglet Dessiner |
| `layout.toolbar.layout` | booléen | `true` | Onglet Mise en page (document et tableur) |
| `layout.toolbar.references` | booléen | `true` | Onglet Références (éditeur de document) |
| `layout.toolbar.collaboration` | booléen | `false` | Onglet Collaboration |
| `layout.toolbar.protection` | booléen | `false` | Onglet Protection |
| `layout.toolbar.view` | booléen | `true` | Onglet Affichage |

> **Note :** Si la section `onlyoffice` est absente, aucun paramètre n'est envoyé → OnlyOffice utilise ses défauts. Rétrocompatible.
