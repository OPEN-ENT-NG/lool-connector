export type Metadata = {
    extension: string,
    'content-type': string
}

export interface IProvider {
    setCapabilities(capabilities: Array<Metadata>): void;

    setTemplates(templates: Array<String>): void;
}

export abstract class Provider implements IProvider {
    templates: Array<String>;
    capabilities: Array<Metadata>;

    constructor() {
        this.templates = []
    }

    setTemplates(templates: Array<String>): void {
        this.templates = templates;
    }

    abstract setCapabilities(capabilities: Array<Metadata>): void;

    abstract canBeOpen(metadata: Metadata): boolean;
}
