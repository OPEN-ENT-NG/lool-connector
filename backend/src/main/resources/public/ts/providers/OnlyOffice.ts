import {Metadata, Provider} from './Provider'

export class OnlyOffice extends Provider {
    private computedCapabilities: Array<String> = [];

    canBeOpen(metadata: Metadata): boolean {
        return this.computedCapabilities.includes(metadata['content-type']);
    }

    setCapabilities(capabilities: Array<Metadata>): void {
        this.capabilities = capabilities;
        this.computedCapabilities = capabilities.map(cap => cap['content-type'])
    }
}