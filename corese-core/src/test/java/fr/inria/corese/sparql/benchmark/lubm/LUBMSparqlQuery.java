package fr.inria.corese.sparql.benchmark.lubm;

/**
 * LUBMSparqlQuery.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 avr. 2014
 */
public final class LUBMSparqlQuery {

    public final static String[] Q = {
        "# Query1\n"
        + "# This query bears large input and high selectivity. It queries about just one class and\n"
        + "# one property and does not assume any hierarchy information or inference.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X	\n"
        + "WHERE\n"
        + "{?X rdf:type ub:GraduateStudent .\n"
        + "  ?X ub:takesCourse\n"
        + "<http://www.Department0.University0.edu/GraduateCourse0>}",
        //
        "# Query2\n"
        + "# This query increases in complexity: 3 classes and 3 properties are involved. Additionally, \n"
        + "# there is a triangular pattern of relationships between the objects involved.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X ?Y ?Z\n"
        + "WHERE\n"
        + "{?X rdf:type ub:GraduateStudent .\n"
        + "  ?Y rdf:type ub:University .\n"
        + "  ?Z rdf:type ub:Department .\n"
        + "  ?X ub:memberOf ?Z .\n"
        + "  ?Z ub:subOrganizationOf ?Y .\n"
        + "  ?X ub:undergraduateDegreeFrom ?Y}",
        //
        "# Query3\n"
        + "# This query is similar to Query 1 but class Publication has a wide hierarchy.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Publication .\n"
        + "  ?X ub:publicationAuthor \n"
        + "        <http://www.Department0.University0.edu/AssistantProfessor0>}",
        //
        "# Query4\n"
        + "# This query has small input and high selectivity. It assumes subClassOf relationship \n"
        + "# between Professor and its subclasses. Class Professor has a wide hierarchy. Another \n"
        + "# feature is that it queries about multiple properties of a single class.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X ?Y1 ?Y2 ?Y3\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Professor .\n"
        + "  ?X ub:worksFor <http://www.Department0.University0.edu> .\n"
        + "  ?X ub:name ?Y1 .\n"
        + "  ?X ub:emailAddress ?Y2 .\n"
        + "  ?X ub:telephone ?Y3}\n",
        //
        "# Query5\n"
        + "# This query assumes subClassOf relationship between Person and its subclasses\n"
        + "# and subPropertyOf relationship between memberOf and its subproperties.\n"
        + "# Moreover, class Person features a deep and wide hierarchy.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Person .\n"
        + "  ?X ub:memberOf <http://www.Department0.University0.edu>}\n",
        //
        "# Query6\n"
        + "# This query queries about only one class. But it assumes both the explicit\n"
        + "# subClassOf relationship between UndergraduateStudent and Student and the\n"
        + "# implicit one between GraduateStudent and Student. In addition, it has large\n"
        + "# input and low selectivity.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X WHERE {?X rdf:type ub:Student}\n",
        //
        "# Query7\n"
        + "# This query is similar to Query 6 in terms of class Student but it increases in the\n"
        + "# number of classes and properties and its selectivity is high.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X ?Y\n"
        + "WHERE \n"
        + "{?X rdf:type ub:Student .\n"
        + "  ?Y rdf:type ub:Course .\n"
        + "  ?X ub:takesCourse ?Y .\n"
        + "  <http://www.Department0.University0.edu/AssociateProfessor0> ub:teacherOf ?Y}\n",
        //
        "# Query8\n"
        + "# This query is further more complex than Query 7 by including one more property.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X ?Y ?Z\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Student .\n"
        + "  ?Y rdf:type ub:Department .\n"
        + "  ?X ub:memberOf ?Y .\n"
        + "  ?Y ub:subOrganizationOf <http://www.University0.edu> .\n"
        + "  ?X ub:emailAddress ?Z}\n",
        //
        "# Query9\n"
        + "# Besides the aforementioned features of class Student and the wide hierarchy of\n"
        + "# class Faculty, like Query 2, this query is characterized by the most classes and\n"
        + "# properties in the query set and there is a triangular pattern of relationships.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X ?Y ?Z\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Student .\n"
        + "  ?Y rdf:type ub:Faculty .\n"
        + "  ?Z rdf:type ub:Course .\n"
        + "  ?X ub:advisor ?Y .\n"
        + "  ?Y ub:teacherOf ?Z .\n"
        + "  ?X ub:takesCourse ?Z}\n",
        //
        "# Query10\n"
        + "# This query differs from Query 6, 7, 8 and 9 in that it only requires the\n"
        + "# (implicit) subClassOf relationship between GraduateStudent and Student, i.e., \n"
        + "#subClassOf rela-tionship between UndergraduateStudent and Student does not add\n"
        + "# to the results.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Student .\n"
        + "  ?X ub:takesCourse\n"
        + "<http://www.Department0.University0.edu/GraduateCourse0>}\n",
        //
        "# Query11\n"
        + "# Query 11, 12 and 13 are intended to verify the presence of certain OWL reasoning\n"
        + "# capabilities in the system. In this query, property subOrganizationOf is defined\n"
        + "# as transitive. Since in the benchmark data, instances of ResearchGroup are stated\n"
        + "# as a sub-organization of a Department individual and the later suborganization of \n"
        + "# a University individual, inference about the subOrgnizationOf relationship between\n"
        + "# instances of ResearchGroup and University is required to answer this query. \n"
        + "# Additionally, its input is small.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X\n"
        + "WHERE\n"
        + "{?X rdf:type ub:ResearchGroup .\n"
        + "  ?X ub:subOrganizationOf <http://www.University0.edu>}\n",
        //
        "# Query12\n"
        + "# The benchmark data do not produce any instances of class Chair. Instead, each\n"
        + "# Department individual is linked to the chair professor of that department by \n"
        + "# property headOf. Hence this query requires realization, i.e., inference that\n"
        + "# that professor is an instance of class Chair because he or she is the head of a\n"
        + "# department. Input of this query is small as well.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X ?Y\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Chair .\n"
        + "  ?Y rdf:type ub:Department .\n"
        + "  ?X ub:worksFor ?Y .\n"
        + "  ?Y ub:subOrganizationOf <http://www.University0.edu>}\n",
        //
        "# Query13\n"
        + "# Property hasAlumnus is defined in the benchmark ontology as the inverse of\n"
        + "# property degreeFrom, which has three subproperties: undergraduateDegreeFrom, \n"
        + "# mastersDegreeFrom, and doctoralDegreeFrom. The benchmark data state a person as\n"
        + "# an alumnus of a university using one of these three subproperties instead of\n"
        + "# hasAlumnus. Therefore, this query assumes subPropertyOf relationships between \n"
        + "# degreeFrom and its subproperties, and also requires inference about inverseOf.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X\n"
        + "WHERE\n"
        + "{?X rdf:type ub:Person .\n"
        + "  <http://www.University0.edu> ub:hasAlumnus ?X}\n",
        "# Query14\n"
        + "# This query is the simplest in the test set. This query represents those with large input and low selectivity and does not assume any hierarchy information or inference.\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX ub: <univ-bench.owl#>\n"
        + "SELECT ?X\n"
        + "WHERE {?X rdf:type ub:UndergraduateStudent}"};
}
