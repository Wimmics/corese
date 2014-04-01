/**
 * Javascript for displaying graph visulisation
 * The code is written by Alban Gaignard (alban.gaignard@cnrs.fr)
 * 
 * Fuqi Song Wimmics Inria-I3S use and adapts the code to this plugin
 * @date March 2014
 */

function renderD3(data, htmlCompId) {
    var d3Data = data.d3;
    var mappings = data.mappings;
    var sMaps = JSON.stringify(mappings);

    var width = $(htmlCompId).parent().width();
//        var height = $("svg").parent().height();
    var height = 400;
    var color = d3.scale.category20();

    var force = d3.layout.force()
            .charge(-200)
            .linkDistance(50)
//        .friction(.8)
            .size([width, height]);

    var svg = d3.select(htmlCompId).append("svg")
//    	.attr("width", width)
//    	.attr("height", height)
            .attr("viewBox", "0 0 800 600")
            .attr("width", "100%")
            .attr("height", 600)
            .attr("preserveAspectRatio", "xMidYMid")
            .style("background-color", "#F4F2F5");

    force.nodes(d3Data.nodes).links(d3Data.edges).start();

    var link = svg.selectAll(".link")
            .data(d3Data.edges)
            .enter().append("path")
            .attr("d", "M0,-5L10,0L0,5")
            // .enter().append("line")
            .attr("class", "link")
            .style("stroke-width", function(d) {
                if (d.label.indexOf("prov#") !== -1) {
                    return 4;
                }
                return 4;
            })
            .on("mouseout", function(d, i) {
                d3.select(this).style("stroke", " #a0a0a0");
            })
            .on("mouseover", function(d, i) {
                d3.select(this).style("stroke", " #000000");
            });

    link.append("title")
            .text(function(d) {
                return d.label;
            });


    var node_drag = d3.behavior.drag()
            .on("dragstart", dragstart)
            .on("drag", dragmove)
            .on("dragend", dragend);

    function dragstart(d, i) {
        force.stop() // stops the force auto positioning before you start dragging
    }

    function dragmove(d, i) {
        d.px += d3.event.dx;
        d.py += d3.event.dy;
        d.x += d3.event.dx;
        d.y += d3.event.dy;
        tick(); // this is the key to make it work together with updating both px,py,x,y on d !
    }

    function dragend(d, i) {
        d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
        tick();
        force.resume();
    }

    var node = svg.selectAll("g.node")
            .data(d3Data.nodes)
            .enter().append("g")
            .attr("class", "node")
            // .call(force.drag);
            .call(node_drag);

    node.append("title")
            .text(function(d) {
                return d.name;
            });

    node.append("circle")
            .attr("class", "node")
            .attr("r", function(d) {
                if (d.group === 0) {
                    return 6;
                }
                return 12;
            })
            .on("dblclick", function(d) {
                d.fixed = false;
            })
            .on("mouseover", fade(.1)).on("mouseout", fade(1))
            .style("stroke", function(d) {
                return color(d.group);
            })
            .style("stroke-width", 5)
            .style("stroke-width", function(d) {
                if (sMaps.indexOf(d.name) !== -1) {
                    return 8;
                }
                return 3;
            })
            // 	.style("stroke-dasharray",function(d) {
            // if (sMaps.indexOf(d.name) !== -1) {
            //   		return "5,5";
            // }
            // 		return "none";
            // 	})
            // .style("fill", "white")
            .style("fill", function(d) {
                return color(d.group);
            });
    // .on("mouseout", function(d, i) {
    //  	d3.select(this).style("fill", "white");
    // })
    // .on("mouseover", function(d, i) {
    //  	d3.select(this).style("fill", function(d) { return color(d.group); });
    // }) ;

    node.append("svg:text")
            .attr("text-anchor", "middle")
            // .attr("fill","white")
            .style("pointer-events", "none")
            .attr("font-size", "18px")
            .attr("font-weight", "200")
            .text(function(d) {
                if ((sMaps.indexOf(d.name) !== -1) && (d.group !== 0)) {
                    return d.name;
                }
            });


    var linkedByIndex = {};
    d3Data.edges.forEach(function(d) {
        linkedByIndex[d.source.index + "," + d.target.index] = 1;
    });

    function isConnected(a, b) {
        return linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index === b.index;
    }

    force.on("tick", tick);

    function tick() {
        link.attr("x1", function(d) {
            return d.source.x;
        })
                .attr("y1", function(d) {
                    return d.source.y;
                })
                .attr("x2", function(d) {
                    return d.target.x;
                })
                .attr("y2", function(d) {
                    return d.target.y;
                });

        node.attr("transform", function(d) {
            return "translate(" + d.x + "," + d.y + ")";
        });

        link.attr("d", function(d) {
            var dx = d.target.x - d.source.x,
                    dy = d.target.y - d.source.y,
                    dr = Math.sqrt(dx * dx + dy * dy);

            return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
        });
    }
    ;

    function fade(opacity) {
        return function(d) {
            node.style("stroke-opacity", function(o) {
                thisOpacity = isConnected(d, o) ? 1 : opacity;
                this.setAttribute('fill-opacity', thisOpacity);
                return thisOpacity;
            });

            link.style("stroke-opacity", function(o) {
                return o.source === d || o.target === d ? 1 : opacity;
            });
        };
    }
}

