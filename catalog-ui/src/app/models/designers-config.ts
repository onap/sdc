
export class Designer {
    designerId: string;
    designerHost: string;
    designerPort: number;
    designerPath: string;
    designerStateUrl: string;
    designerProtocol: string;
    designerDisplayOptions: Map<string, DesignerDisplayOptions>;
}

export class DesignerDisplayOptions {
    displayName: string;
    validResourceTypes: Array<string>;
}

export type Designers = Array<Designer>;

export class DesignersConfiguration {
    static designers: Designers;
}
