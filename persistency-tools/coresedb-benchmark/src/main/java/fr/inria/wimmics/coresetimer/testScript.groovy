/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.wimmics.coresetimer

def testRoot = '/Users/edemairy/Developpement/Corese-master/persistency-tools/coreseTimer-common/'
def fileNames = ['result_input_1_query_0.xml']
def tester = new DbMemoryTest()
fileNames.each {
    def result = tester.checkEqual(testRoot + "DB/${it}", testRoot + "MEMORY/${it}")
    println "${it}: ${result}"
}
def name = 'edemairy'
