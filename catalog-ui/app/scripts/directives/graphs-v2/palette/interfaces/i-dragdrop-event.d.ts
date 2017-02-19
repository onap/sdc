interface IDragDropEvent extends JQueryEventObject {
    dataTransfer: any;
    toElement: {
        naturalWidth: number;
        naturalHeight: number;
    }
}