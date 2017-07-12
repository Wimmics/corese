/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.rdftograph

//val g = new OrientGraphFactory("plocal:/Users/edemairy/btc_orientdb_1000")
val graph = new OrientGraphFactory ("plocal:/Users/edemairy/btc_orientdb_10m").getNoTx ().asScala
val trav = t.traversal ()
trav.E ().has ("e_value", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type").limit (10)

val graph = g.getNoTx ().asScala
val key = Key[String] ("e_value")
val sel1 = graph.E.hasLabel ("E_rdf_edge").has (key, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type").limit (10).toList ()

//val sel1 = graph.E.has(key, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type").limit(10).toList()
