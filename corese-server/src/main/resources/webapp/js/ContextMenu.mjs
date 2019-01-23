export class ContextMenu {
    constructor(id, root) {
        this.id = id;
        this.domNode = root.append("div")
            .attr("id", this.id)
            .attr("class", "dropdown-content")
            .style("display", "none")
    }

    /**
     *
     * @param label  What to display in the menu entry.
     * @param action Function containing the action to process when the entry is chosen.
     */
    addEntry(label, action) {
        this.action = action;
        this.domNode.append("a")
            .text(label)
            .attr("id", label)
            .attr("href", "#")
        ;
        var h = this.handler;
        let parameters = {"menu": this, "action": action};
        var bound = h.bind(parameters);
        document.getElementById(label).onclick = bound;
        return this;
    }

    handler() {
        this.action(this.menu.parameters);
    }

    setParameters(params) {
        this.parameters = params;
    }

    isDisplayOn() {
        return d3.select(`#${this.id}`)
            .style("display") === "block";
    }

    displayOn(parameters) {
        d3.select(`#${this.id}`)
            .style("display", "block")
            .style("top", d3.event.y + "px")
            .style("left", d3.event.x + "px");
    }

    displayOff() {
        d3.select(`#${this.id}`)
            .style("display", "none");
    }

    static create(root, id) {
        root.on("contextmenu", () => false);
        let result = d3.select(`${id}`);
        if (result.size() === 0) {
            let menu = new ContextMenu(id, root);
            menu.id = id;
            d3.select(`#${id}`).on("select", () => {
                console.log("select called.")
            });
            return menu;
        } else {
            return result;
        }
    }
}