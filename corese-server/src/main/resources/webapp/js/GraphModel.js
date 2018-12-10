import {Observable} from "./Observable.mjs";

export class GraphModel extends Observable {
    constructor(data) {
        super();
        this.nodeRadius = 20;
        this.BNODE_ID = "bnode";
        this.URI_ID = "uri";
        this.LITERAL_ID = "literal";
        this.ALL_NODES = "nodes";
        this.ALL_EDGES = "edges";
        this.ALL_ELEMENTS = [this.ALL_NODES, this.ALL_EDGES];

        this.displayAll ={};
        this.displayAll[ this.ALL_EDGES ] = false;
        this.displayAll[ this.ALL_NODES ] = false;
        this.displayEdgeSubsets = {};

        this.displayAll[this.ALL_NODES] = false;
        this.displayNodeSubsets = {};

        this.groups = [];
        this.groups[this.ALL_NODES] = {};
        this.groups[this.ALL_EDGES] = {};

        this.computeGroups(data.nodes).forEach(group => {
            this.groups[this.ALL_NODES][group] = false;
        });
        this.computeGroups(data.edges).forEach(group => {
            this.groups[this.ALL_EDGES][group] = false;
        });
    }

    setDisplayAll(element, value) {
        this.displayAll[element] = value;
        this.getGroups(element).forEach(
            group => this.groups[element][group] = value
        )
        this.notififyObservers();
    }

    getDisplayAll(element) {
        return this.displayAll[element];
    }

    getDisplayAllNodes() {
        this.getDisplayAll(this.ALL_NODES);
    }
    getDisplayAllNodes() {
        this.getDisplayAll(this.ALL_EDGES);
    }



    getDisplayGroup(element, group) {
        return this.groups[element][group];
    }

    getDisplayNodeGroup(group) {
        return this.getDisplayGroup(this.ALL_NODES, group);
    }

    getDisplayEdgeGroup(group) {
        return this.getDisplayGroup(this.ALL_EDGES, group);
    }

    setDisplayGroup(element, group, display) {
        this.groups[element][group] = display;
        let allEquals = true;
        Object.keys(this.groups[element]).forEach(
            group => allEquals = (allEquals && (this.groups[element][group] === display))
        )
        if (allEquals) {
            this.displayAll[element] = display;
        } else {
            this.displayAll[element] = false;
        }
        this.notififyObservers();
    }

    toggleDisplayAll(element) {
       this.displayAll[element] = !this.displayAll[element];
       this.getGroups(element).forEach(
           group => this.groups[element][group] = this.displayAll[element]
       );
       this.notififyObservers();
    }

    toggleDisplayGroup(element, group) {
        this.setDisplayGroup(element, group, !this.groups[element][group]);
    }

    toggleDisplayGroupNum(element, groupNum) {
        const group = Object.keys(this.groups[element])[groupNum];
        this.toggleDisplayGroup(element, group);
    }

    toggleDisplayNodeGroupNum(groupNum) {
        return this.toggleDisplayGroupNum(this.ALL_NODES, groupNum);
    }

    toggleDisplayEdgeGroupNum(groupNum) {
        return this.toggleDisplayGroup(this.ALL_EDGES, group);
    }

    getGroups(groupName) {
        if (groupName === this.ALL_NODES) {
            return Object.keys( this.groups[this.ALL_NODES] );
        } else if (groupName === this.ALL_EDGES) {
            return Object.keys( this.groups[this.ALL_EDGES] );
        } else {
            throw `incorrect groupName value = ${groupName}`;
        }
    }
    getNodeGroups() {
        return this.getGroups(this.ALL_NODES);
    }
    getEdgeGroups() {
        return this.getGroups(this.ALL_EDGES);
    }

    computeGroups(data) {
        var result = new Set();
        data.forEach(
            elem => {
                if (elem.group === undefined) {
                    elem.group = "default";
                }
                if (!result.has(elem.group)) {
                    result.add(elem.group);
                }
            }
        )
        console.log(`found groups: ${result}`)
        return result;
    }
}

