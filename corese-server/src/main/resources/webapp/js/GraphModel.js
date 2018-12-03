import {Observable} from "./Observable.mjs";

export class GraphModel extends Observable {
    constructor() {
        super();
        this.nodeRadius = 20;
        this.BNODE_ID = "bnode";
        this.URI_ID = "uri";
        this.LITERAL_ID = "literal";
        this.displayNodeLabelsValue = false;
        this.displayEdgeLabelsValue = false;
    }

    /**
     * /param display boolean value.
     */
    set displayNodeLabels( display ) {
        this.displayNodeLabelsValue = display;
        this.notififyObservers();
    }

    get displayNodeLabels() {
        return this.displayNodeLabelsValue;
    }

    /**
     * /param display boolean value.
     */
    set displayEdgeLabels( display ) {
        this.displayEdgeLabelsValue = display;
        this.notififyObservers();
    }

    get displayEdgeLabels() {
        return this.displayEdgeLabelsValue;
    }
}

