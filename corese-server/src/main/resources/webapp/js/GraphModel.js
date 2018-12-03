import {Observable} from "./Observable.mjs";

export class GraphModel extends Observable {
    constructor() {
        super();
        this.nodeRadius = 20;
    }
}

GraphModel.BNODE_ID = "bnode";
GraphModel.URI_ID = "uri";
GraphModel.LITERAL_ID = "literal";