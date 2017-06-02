/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.persistency

import org.apache.tinkerpop.gremlin.orientdb._
import gremlin.scala._
import org.apache.commons.configuration.BaseConfiguration
import com.orientechnologies.orient.core.metadata.schema.OType
import org.apache.tinkerpop.gremlin.process.traversal._
import org.apache.tinkerpop.gremlin.process.traversal.step.filter._
import org.apache.tinkerpop.gremlin.process.traversal.step.map._
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect._
import org.apache.tinkerpop.gremlin.structure.T

val graph = new OrientGraphFactory("plocal:/Users/edemairy/btc_odb_1m")
val g = graph.getTx().traversal()
//g.E().fold().project("value", "context").by(__.range(Scope.local, 0, 2))
g.E().fold().project("value", "context").range(Scope.local, 0, 2)