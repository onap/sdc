
export class Designer {
    designerId: string;
    designerHost: string;
    designerPort: string;
    designerPath: string;
    designerStateUrl: string;
    designerProtocol: string;
    designerDisplayOptions: Map<string, DesignerDisplayOptions>;
}

export class DesignerDisplayOptions {
    displayName: string;
    displayContext: Array<string>;
}

export type Designers = Array<Designer>;

export class DesignersConfiguration {
    static designers: Designers;
}
