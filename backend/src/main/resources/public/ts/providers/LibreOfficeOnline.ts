import {Metadata, Provider} from './Provider'

export const drawExtensions = ['bmp', 'cdr', 'cmx', 'dxf', 'emf', 'eps', 'fodg', 'gif', 'jpe', 'jpeg', 'jpg', 'met', 'odg', 'otg',
    'pbm', 'pcd', 'pcx', 'pdf', 'pgm', 'png', 'ppm', 'psd', 'pub', 'ras', 'sda', 'sgf', 'sgv', 'svg', 'std', 'svm', 'sxd', 'tga', 'tif',
    'tiff', 'vsd', 'vdx', 'vsdm', 'vsdx', 'vst', 'wmf', 'xbm', 'xpm'];

export class LibreOfficeOnline extends Provider {
    private computedCapabilities = {};

    canBeOpen(metadata: Metadata): boolean {
        return drawExtensions.indexOf(metadata.extension) == -1 &&
            metadata.extension in this.computedCapabilities &&
            metadata["content-type"] === this.computedCapabilities[metadata.extension];
    }

    setCapabilities(capabilities: Array<Metadata>): void {
        this.capabilities = capabilities;
        capabilities.forEach((capability) => this.computedCapabilities[capability.extension] = capability['content-type']);
    }

}