
export class Designer {
    displayName: string;
    designerHost: string;
    designerPort: number;
    designerPath: string;
    designerStateUrl: string;
    designerProtocol: string;
    designerButtonLocation: Array<string>;
    designerTabPresentation: Array<string>;
}

export type Designers = Array<Designer>;

export class DesignersConfiguration {
    static designers: Designers;
}
