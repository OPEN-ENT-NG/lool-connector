import {Behaviours, idiom, ng, notify, workspace} from 'entcore';
import {Eventer} from "entcore-toolkit";
import {Element, Tree} from "entcore/types/src/ts/workspace/model";
import {safeApply} from "../utils/safe-apply.utils";

interface ViewModel {
    newDocument: {
        extension: string,
        name: string,
        folder: any
    },
    workspaceTrees: Tree[],
    listFolders: any,
    openedFolders: Array<Element>,
    selectedFolder: Element,
    documentTypeList: any[],
    eventer: Eventer,
    display: {
        warning: boolean,
        lightbox: {}
    }

    $onInit(): Promise<void>;

    $onDestroy(): Promise<void>;

    initDocument(): void;

    createDocument(Event, string): void;
}

const BEHAVIOURS_NAME = 'lool';


export const homeController = ng.controller('HomeController', ['$scope',
    function ($scope) {
        const vm: ViewModel = this;
        vm.newDocument = {
            extension: '',
            name: '',
            folder: null
        };
        vm.workspaceTrees = [];
        vm.listFolders = {};
        vm.openedFolders = [];
        vm.selectedFolder = null;
        vm.documentTypeList = [];
        vm.display = {
            warning: false,
            lightbox: false
        }

        vm.$onInit = async () : Promise<void> => {
            let trees = null;
            try {
                trees = await workspace.v2.service.fetchTrees({filter: "all", hierarchical: true});
                await Behaviours.applicationsBehaviours[BEHAVIOURS_NAME].init();
                vm.workspaceTrees[0] = trees.filter(tree => tree.filter == "owner")[0];
                vm.workspaceTrees[0].name = idiom.translate("workspace.myDocuments");
            } catch (err) {
                notify.error(idiom.translate('lool.create.failed.loading'));
                throw err;
            }

            vm.listFolders = {
                cssTree: "folders-tree",
                get trees() {
                    return vm.workspaceTrees;
                },
                isDisabled(folder) {
                    return false;
                },
                isOpenedFolder(folder: Element): boolean {
                    return vm.openedFolders.some((openFolder: Element) => openFolder === folder);
                },
                isSelectedFolder(folder: Element) {
                    return vm.selectedFolder === folder;
                },
                openFolder(folder: Element) {
                    vm.newDocument.folder = folder._id;
                    vm.selectedFolder = folder;
                    if (!this.isOpenedFolder(folder)) {
                        vm.openedFolders.push(folder);
                    }
                    else {
                        vm.openedFolders = vm.openedFolders.filter((e: Element) => e != folder);
                    }
                },
            }

            const {templates} = Behaviours.applicationsBehaviours[BEHAVIOURS_NAME].provider;
            for (let ext of templates) {
                vm.documentTypeList.push({
                    extension: ext,
                    title: idiom.translate(`lool.sniplet.create-document.type.${ext}`)
                })
            }

            vm.initDocument();
            safeApply($scope);
        };

        // Functions

        vm.initDocument = () : void => {
            vm.newDocument = {
                extension: vm.documentTypeList[0].extension,
                name: '',
                folder: null
            };
            vm.selectedFolder = vm.listFolders.trees[0];
            vm.display.warning = false;
            safeApply($scope);
        };

        vm.createDocument = (e, type) : void => {
            if (vm.newDocument.name === undefined || vm.newDocument.name.trim() === '') {
                vm.display.warning = true;
                return;
            }
            vm.newDocument.extension = type.extension;
            e.preventDefault();
            console.info(vm.newDocument);
            window.open(`/lool/document?type=${vm.newDocument.extension}&name=${vm.newDocument.name}${vm.newDocument.folder ? `&folder=${vm.newDocument.folder}` : ''}`);
            vm.initDocument();
        };
    }
]);