import {IProvider} from "./Provider";
import {OnlyOffice} from "./OnlyOffice";
import {LibreOfficeOnline} from "./LibreOfficeOnline";

export class ProviderFactory {
    static provider(name: string): IProvider {
        switch (name) {
            case "OnlyOffice":
                return new OnlyOffice();
            case "LibreOfficeOnline":
                return new LibreOfficeOnline();
        }
    }
}