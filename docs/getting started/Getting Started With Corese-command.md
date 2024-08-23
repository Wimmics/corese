# Getting Started With Corese-Command

Corese-Command is a command-line interface (CLI) for the Corese Semantic Web Factory. It facilitates running SPARQL queries on RDF datasets and remote SPARQL endpoints, converting RDF files between different serialization formats, and validating RDF data against SHACL shapes directly from the command line. This makes it an indispensable tool for automated processing, quick console-based testing, and integration into larger scripts or applications.

Designed to simplify and streamline tasks related to querying, converting, and validating RDF data, Corese-Command is suitable for developers, data scientists, and anyone working with Semantic Web technologies.

## 1. Installation

Installations instructions are available on the [Corese-Command GitHub repository](https://github.com/Wimmics/corese).

## 2. The `sparql` Command

The `sparql` command allows you to run SPARQL queries on RDF datasets.

### 2.1. Basic Usage

Let's start with a simple example, executing a query on a local file:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl
```

```markdown
| ?s                                        | ?p                              | ?o                                      |
| ----------------------------------------- | ------------------------------- | --------------------------------------- |
| <http://corese.inria.fr/Please_Please_Me> | <http://corese.inria.fr/artist> | <http://corese.inria.fr/The_Beatles>    |
| <http://corese.inria.fr/McCartney>        | <http://corese.inria.fr/artist> | <http://corese.inria.fr/Paul_McCartney> |
```

In this example, the query is provided directly on the command line with the `-q` flag, and the input file is specified with the `-i` flag. The result is printed to the standard output with the default format, which is `markdown`.

### 2.2. Choose the Result Format

Let's try the same query as before, but this time with the `json` format as output:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -r json
```

```json
{
  "head": {
    "vars": [
      "s",
      "p",
      "o"
    ]
  },
  "results": {
    "bindings": [
      {
        "s": {
          "type": "uri",
          "value": "http://corese.inria.fr/Please_Please_Me"
        },
        "p": {
          "type": "uri",
          "value": "http://corese.inria.fr/artist"
        },
        "o": {
          "type": "uri",
          "value": "http://corese.inria.fr/The_Beatles"
        }
      },
      {
        "s": {
          "type": "uri",
          "value": "http://corese.inria.fr/McCartney"
        },
        "p": {
          "type": "uri",
          "value": "http://corese.inria.fr/artist"
        },
        "o": {
          "type": "uri",
          "value": "http://corese.inria.fr/Paul_McCartney"
        }
      }
    ]
  }
}
```

The result format can be specified with the `-r` or `-of` flag. The following formats are available:

- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`
- XML: `xml`, `srx` or `application/sparql-results+xml`
- Json: `json`, `srj` or `application/sparql-results+json`
- CSV: `csv` or `text/csv`
- TSV: `tsv` or `text/tab-separated-values`
- Markdown: `markdown`, `md` or `text/markdown`

Here is a table of available formats according to the type of request:

| Format   | select | ask | insert | insert-where | delete | delete-where | describe | construct |
| -------- | ------ | --- | ------ | ------------ | ------ | ------------ | -------- | --------- |
| rdfxml   | ❌      | ❌   | ✅      | ✅            | ✅      | ✅            | ✅        | ✅         |
| turtle   | ❌      | ❌   | ✅      | ✅            | ✅      | ✅            | ✅        | ✅         |
| trig     | ❌      | ❌   | ✅      | ✅            | ✅      | ✅            | ✅        | ✅         |
| jsonld   | ❌      | ❌   | ✅      | ✅            | ✅      | ✅            | ✅        | ✅         |
| ntriples | ❌      | ❌   | ✅      | ✅            | ✅      | ✅            | ✅        | ✅         |
| nquads   | ❌      | ❌   | ✅      | ✅            | ✅      | ✅            | ✅        | ✅         |
| xml      | ✅      | ✅   | ❌      | ❌            | ❌      | ❌            | ❌        | ❌         |
| json     | ✅      | ✅   | ❌      | ❌            | ❌      | ❌            | ❌        | ❌         |
| csv      | ✅      | ✅   | ❌      | ❌            | ❌      | ❌            | ❌        | ❌         |
| tsv      | ✅      | ✅   | ❌      | ❌            | ❌      | ❌            | ❌        | ❌         |
| markdown | ✅      | ✅   | ❌      | ❌            | ❌      | ❌            | ❌        | ❌         |

### 2.3. Different Types of Input

The input can be provided in different ways:

- **File Input:** The input file can be specified with the `-i` flag:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl
```

- **URL Input:** URLs can be specified with the `-i` flag:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i 'http://example.org/myData.ttl'
```

- **Standard Input:** If no input file is specified with `-i`, the program uses the standard input:

```shell
cat myData.ttl | corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -if turtle
```

> The input file format is automatically detected for file and URL inputs. If
> the input is provided on the standard input or you want to force the input
> format, you can use the `-f` or `-if` flag. Possible values are:
>
> - `rdfxml`, `rdf` or `application/rdf+xml`
> - `turtle`, `ttl` or `text/turtle`
> - `trig`, `application/trig`
> - `jsonld`, `application/ld+json`
> - `ntriples`, `nt` or `application/n-triples`
> - `nquads`, `nq`, or `application/n-quads`
> - `rdfa`, `html` or `application/xhtml+xml`

### 2.4. Different Types of Queries

The query can be provided in different ways:

- **Query String:** The query can be provided directly on the command line with the `-q` flag:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl
```

- **File Query:** The query can be provided in a file with the `-q` flag:

```shell
corese-command sparql -q myQuery.rq -i myData.ttl
```

- **URL Input:** URLs can be specified with the `-q` flag:

```shell
corese-command sparql -q 'http://example.org/myQuery.rq' -i myData.ttl
```

### 2.5. Multiple Input Files

- **Multiple Input:** It's possible to provide multiple input files by repeating the `-i` flag:

```shell
corese-command sparql -q myQuery.rq -i myData1.ttl -i myData2.ttl -i http://example.com/myData3.ttl
```

- **Shell Globbing:** It's also possible to use shell globbing to provide multiple input files:

```shell
corese-command sparql -q myQuery.rq -i *.ttl
```

```shell
corese-command sparql -q myQuery.rq -i myData?.ttl
```

- **Directory Input:** If you want to use a whole directory as input, you can do so.

```shell
corese-command sparql -q myQuery.rq -i ./myDirectory/
```

- **Directory Input Recursive:** If you want to use a whole directory as input, you can do so. The `-R` flag allows you to use the directory recursively.

```shell
corese-command sparql -q myQuery.rq -i ./myDirectory/ -R
```

### 2.6. Different Types of Output

If you want to save the result to a file, you can do so with the `-o` flag:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -r json -o myResult.json
```

If no `-o` flag is provided, the result is printed to the standard output.

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -r json | jq […] 
```

## 3. The `convert` Command

The `convert` command allows you to convert an RDF file from one serialization format to another.

### 3.1. Basic Usage

```shell
corese-command convert -i myFile.ttl -r jsonld
```

This example converts `myFile.ttl` from `turtle` to `jsonld`. The `-i` flag specifies the input file, and the `-r` flag specifies the output format.

### 3.2. Different Types of Input

The input can be provided in different ways:

- **File Input:** The input file can be specified with the `-i` flag:

```shell
corese-command convert -i myData.ttl -r jsonld
```

- **URL Input:** URLs can be specified with the `-i` flag:

```shell
corese-command convert -i 'http://example.org/myData.ttl'
```

- **Standard Input:** If no input file is specified with `-i`, the program uses the standard input:

```shell
cat myData.ttl | corese-command convert -r turtle -if turtle
```

> The input file format is automatically detected for file and URL inputs. If
> the input is provided on the standard input or you want to force the input
> format, you can use the `-f` or `-if` flag. Possible values are:
>
> - `rdfxml`, `rdf` or `application/rdf+xml`
> - `turtle`, `ttl` or `text/turtle`
> - `trig`, `application/trig`
> - `jsonld`, `application/ld+json`
> - `ntriples`, `nt` or `application/n-triples`
> - `nquads`, `nq`, or `application/n-quads`
> - `rdfa`, `html` or `application/xhtml+xml`

### 3.3. Different Types of Output

The output can be provided in different ways:

- **File Output:** The output file can be specified with the `-o` flag:

```shell
corese-command convert -i myData.ttl -r jsonld -o myData.jsonld
```

- **Standard Output:** If no output file is specified with `-o`, the program uses the standard output:

```shell
corese-command convert -i myData.ttl -r jsonld | jq […]
```

> The output file format can be specified with the `-r` flag. Possible values are:
>
> - RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
> - Turtle: `turtle`, `ttl` or `text/turtle`
> - TriG: `trig` or `application/trig`
> - JSON-LD: `jsonld` or `application/ld+json`
> - NTRIPLES: `ntriples`, `nt` or `application/n-triples`
> - NQUADS: `nquads`, `nq`, or `application/n-quads`

### 3.4. Summary of Available Formats

The `convert` command supports the following formats for input and output:

| Format   | Input Support | Output Support |
| -------- | ------------- | -------------- |
| RDFXML   | ✅             | ✅              |
| Turtle   | ✅             | ✅              |
| JSONLD   | ✅             | ✅              |
| TriG     | ✅             | ✅              |
| NTRIPLES | ✅             | ✅              |
| NQUADS   | ✅             | ✅              |
| RDFA     | ✅             | ❌              |

### 3.5. Multiple Input Files

- **Multiple Input:** It's possible to provide multiple input files by repeating the `-i` flag:

```shell
corese-command convert -i myData1.ttl -i myData2.ttl -r jsonld
```

- **Shell Globbing:** It's also possible to use shell globbing to provide multiple input files:

```shell
corese-command convert -i rdf/*.ttl -r jsonld
```

```shell
corese-command convert -i myData?.ttl -r jsonld
```

- **Directory Input:** If you want to use a whole directory as input, you can do so.

```shell
corese-command convert -i ./myDirectory/ -r jsonld
```

- **Directory Input Recursive:** If you want to use a whole directory as input, you can do so. The `-R` flag allows you to use the directory recursively.

```shell
corese-command convert -i ./myDirectory/ -r jsonld -R
```

> The command integrates all specified input files into a single dataset for processing. During conversion, these files are collectively transformed into the designated output format, effectively merging all data into one coherent file.

## 4. The `shacl` Command

The `shacl` command allows you to validate RDF data against SHACL shapes.

### 4.1. Basic Usage

```shell
corese-command shacl -i myData.ttl -s myShapes.ttl
```

This example validates `myData.ttl` against `myShapes.ttl`. The `-i` flag specifies the input file, and the `-s` flag specifies the shapes file.

### 4.2. Different Types of Input

The input can be provided in different ways:

- **File Input:** The input file can be specified with the `-i` flag:

```shell
corese-command shacl -i myData.ttl -s myShapes.ttl
```

- **URL Input:** URLs can be specified with the `-i` flag:

```shell
corese-command shacl -i 'http://example.org/myData.ttl' -s 'http://example.org/myShapes.ttl'
```

- **Standard Input:** If no input file is specified with `-i`, the program uses the standard input:

```shell
cat myData.ttl | corese-command shacl -s myShapes.ttl -if turtle
```

> The input file format is automatically detected for file and URL inputs. If
> the input is provided on the standard input or you want to force the input
> format, you can use the `-f` or `-if` flag for the data and the `-a` or `-sf` flag for the shapes. Possible values are:
>
> - `rdfxml`, `rdf` or `application/rdf+xml`
> - `turtle`, `ttl` or `text/turtle`
> - `trig`, `application/trig`
> - `jsonld`, `application/ld+json`
> - `ntriples`, `nt` or `application/n-triples`
> - `nquads`, `nq`, or `application/n-quads`
> - `rdfa`, `html` or `application/xhtml+xml`

### 4.3. Different Types of Output

The output report can be provided in different ways (the default format is `turtle`):

- **File Output:** The output file can be specified with the `-o` flag:

```shell
corese-command shacl -i myData.ttl -s myShapes.ttl -o myResult.ttl
```

- **Standard Output:** If no output file is specified with `-o`, the program uses the standard output:

```shell
corese-command shacl -i myData.ttl -s myShapes.ttl | other-command
```

> The output file format can be specified with the `-r` or `-of` flag. Possible values are:
>
> - RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
> - Turtle: `turtle`, `ttl` or `text/turtle`
> - TriG: `trig` or `application/trig`
> - JSON-LD: `jsonld` or `application/ld+json`
> - NTRIPLES: `ntriples`, `nt` or `application/n-triples`
> - NQUADS: `nquads`, `nq`, or `application/n-quads`

### 4.4. Multiple Input Files

- **Multiple Input:** It's possible to provide multiple input files by repeating the `-i` and `-s` flags:

```shell
corese-command shacl -i myData1.ttl -i myData2.ttl -s myShapes1.ttl -s myShapes2.ttl
```

- **Shell Globbing:** It's also possible to use shell globbing to provide multiple input files:

```shell
corese-command shacl -i rdf/*.ttl -s shapes/*.ttl
```

```shell
corese-command shacl -i myData?.ttl -s myShapes?.ttl
```

- **Directory Input:** If you want to use a whole directory as input, you can do so.

```shell
corese-command shacl -i ./myDirectory/ -s ./myShapes/
```

- **Directory Input Recursive:** If you want to use a whole directory as input, you can do so. The `-R` flag allows you to use the directory recursively.

```shell
corese-command shacl -i ./myDirectory/ -s ./myShapes/ -R
```

> All input files are loaded into the same dataset, and all shapes files are loaded into the same shapes graph. The dataset is validated against all shapes graphs.

## 5. The `remote-sparql` Command

The `remote-sparql` command allows you to run SPARQL queries on a remote SPARQL endpoint.

### 5.1. Basic Usage

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql"
```

This example executes a query on the remote SPARQL endpoint `http://example.org/sparql`. The `-q` flag specifies the query, and the `-e` flag specifies the endpoint.

### 5.2. Choose the Result Format

Let's try the same query as before, but this time with the `json` format as output:

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" -a "application/sparql-results+json"
```

The format of the result can be specified by using one of the following flags: `-a`, `-of`, or `--accept`. The available formats are determined by the remote SPARQL endpoint. Here are some common formats:

- XML: `application/sparql-results+xml`
- Json: `application/sparql-results+json`
- CSV: `text/csv`
- TSV: `text/tab-separated-values`

> If no `-a`, `-of`, or `--accept` flag is provided, the program uses 'text/csv' as the default format.

### 5.3. Different Types of Queries

The query can be provided in different ways:

- **Query String:** The query can be provided directly on the command line with the `-q` flag:

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql"
```

- **File Query:** The query can be provided in a file with the `-q` flag:

```shell
corese-command remote-sparql -q myQuery.rq -e "http://example.org/sparql"
```

- **URL Input:** URLs can be specified with the `-q` flag:

```shell
corese-command remote-sparql -q 'http://example.org/myQuery.rq' -e "http://example.org/sparql"
```

- **Standard Input:** If no input file is specified with `-q`, the program uses the standard input:

```shell
cat myQuery.rq | corese-command remote-sparql -e "http://example.org/sparql"
```

### 5.4. Different Types of Output

The output can be provided in different ways:

- **File Output:** The output file can be specified with the `-o` flag:

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" -o myResult.csv
```

- **Standard Output:** If no output file is specified with `-o`, the program uses the standard output:

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" -a "application/sparql-results+json" | jq […]
```

### 5.5. Different Types of Methods

In SPARQL 1.1, three different methods are defined for sending a SPARQL query to a SPARQL endpoint:

- **GET:** The query is sent as a URL parameter. This method is suitable for short queries. It's simple and easy to use, but has limitations on the length of the URL, which can be problematic for longer queries. [W3C SPARQL 1.1 Protocol](https://www.w3.org/TR/sparql11-protocol/#query-via-get)
  
- **POST-URLENCODED:** In this method, the query is sent in the body of the HTTP request, with the `application/x-www-form-urlencoded` media type. This method is suitable for longer queries that exceed the URL length limitations imposed on the GET method. [W3C SPARQL 1.1 Protocol](https://www.w3.org/TR/sparql11-protocol/#query-via-post-urlencoded)
  
- **POST-Direct:** The query is sent in the body of the HTTP request, with the `application/sparql-query` media type. This method is also suitable for longer queries, and provides a direct way to post the SPARQL query to the endpoint. [W3C SPARQL 1.1 Protocol](https://www.w3.org/TR/sparql11-protocol/#query-via-post-direct)

In the command line interface, the `-m` or `--request-method` flags are used to specify the HTTP request method to use. The default value is `GET`. The available options are `GET`, `POST-Encoded`, and `POST-Direct`, corresponding to the GET, POST-URLENCODED, and POST-Direct methods respectively.

### 5.6. Specifying Graphs

In SPARQL, the dataset to be queried can be specified using the `FROM` and `FROM NAMED` clauses in the query itself. However, you can also specify the default and named graphs using command line arguments when invoking the SPARQL processor. This can be particularly useful when you want to query multiple graphs without having to specify them within the query text.

#### 5.6.1. Default Graph

The default graph can be specified using the `-d` or `--default-graph` option. Each occurrence of this option represents a URI of a default graph. Multiple URIs can be specified by repeating this option.

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" -d http://example.org/graph1 -d http://example.org/graph2
```

#### 5.6.2. Named Graph

The named graph can be specified using the `-n` or `--named-graph` option. Each occurrence of this option represents a URI of a named graph. Multiple URIs can be specified by repeating this option.

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" -n http://example.org/graph1 -n http://example.org/graph2
```

### 5.7. Additional Request Configurations

The following options provide additional configurations for the HTTP request sent to the SPARQL endpoint. These configurations include setting custom headers, controlling redirections, and toggling query validation.

#### 5.7.1. Custom HTTP Headers

Custom HTTP headers can be added to the request using the `-H` or `--header` option. Each occurrence of this option represents a single header, with the header name and value separated by a colon `:`.

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" --header "Accept: application/sparql-results+json" --header "User-Agent: MyClient"
```

> When the `--accept` option is used alongside the `--header "Accept: …"` option, the request sent to the server will contain a list of MIME types in the `Accept` header. The MIME type specified by the `--accept` option will be placed first in this list, followed by the MIME types specified with the `--header "Accept: …"` option.

#### 5.7.2. Redirection Limit

The maximum number of HTTP redirections to follow can be specified using the `-r` or `--max-redirection` option. The default value is 5.

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" --max-redirection 10
```

#### 5.7.3. Query Validation

By default, the query is validated before being sent to the SPARQL endpoint. This can be disabled using the `-i` or `--ignore-query-validation` option.

```shell
corese-command remote-sparql -q 'SELECT * WHERE {?s ?p ?o}' -e "http://example.org/sparql" --ignore-query-validation
```

This option is useful when you want to send a query that is not valid according to the SPARQL grammar, but is still accepted by the SPARQL endpoint.

## 6. The `canonicalize` Command

The `canonicalize` command allows you to apply a specific canonicalization algorithm to RDF files.

### 6.1. Basic Usage

Use the following syntax to canonicalize an RDF file using the SHA-256 algorithm under the RDFC 1.0 specification:

```shell
corese-command canonicalize -i myData.ttl -r rdfc-1.0-sha256
```

This example canonicalizes `myData.ttl` to the `rdfc-1.0-sha256` (See [RDFC1.0](https://www.w3.org/TR/rdf-canon/)) canonical algorithm. The `-i` flag specifies the input file, and the `-r` flag specifies the canonical algorithm.

### 6.2. Different Types of Input

The input can be provided in different ways:

- **File Input:** The input file can be specified with the `-i` flag:

```shell
corese-command canonicalize -i myData.ttl -r rdfc-1.0-sha256
```

- **URL Input:** URLs can be specified with the `-i` flag:

```shell
corese-command canonicalize -i 'http://example.org/myData.ttl' -r rdfc-1.0-sha256
```

- **Standard Input:** If no input file is specified with `-i`, the program uses the standard input:

```shell
cat myData.ttl | corese-command canonicalize -r rdfc-1.0-sha256 -if turtle
```

> The input file format is automatically detected for file and URL inputs. If
> the input is provided on the standard input or you want to force the input
> format, you can use the `-f` or `-if` flag. Possible values are:
>
> - `rdfxml`, `rdf` or `application/rdf+xml`
> - `turtle`, `ttl` or `text/turtle`
> - `trig`, `application/trig`
> - `jsonld`, `application/ld+json`
> - `ntriples`, `nt` or `application/n-triples`
> - `nquads`, `nq`, or `application/n-quads`
> - `rdfa`, `html` or `application/xhtml+xml`

### 6.3. Different Types of Output

The output can be provided in different ways:

- **File Output:** The output file can be specified with the `-o` flag:

```shell
corese-command canonicalize -i myData.ttl -r rdfc-1.0-sha256 -o myResult.ttl
```

- **Standard Output:** If no output file is specified with `-o`, the program uses the standard output:

```shell
corese-command canonicalize -i myData.ttl -r rdfc-1.0-sha256 | other-command
```

### 6.4. Canonicalization Algorithms

The following canonicalization algorithms are available:

- [RDFC 1.0](https://www.w3.org/TR/rdf-canon/) with SHA-256.
- [RDFC 1.0](https://www.w3.org/TR/rdf-canon/) with SHA-384.

> The output file format can be specified with the `-r` flag. Possible values are:
>
> - `rdfc-1.0` or `rdfc-1.0-sha256` for [RDFC 1.0](https://www.w3.org/TR/rdf-canon/) with SHA-256
> - `rdfc-1.0-sha384` for [RDFC 1.0](https://www.w3.org/TR/rdf-canon/) with SHA-384

### 6.5. Multiple Input Files

- **Multiple Input:** It's possible to provide multiple input files by repeating the `-i` flag:

```shell
corese-command canonicalize -i myData1.ttl -i myData2.ttl -r rdfc-1.0-sha256
```

- **Shell Globbing:** It's also possible to use shell globbing to provide multiple input files:

```shell
corese-command canonicalize -i rdf/*.ttl -r rdfc-1.0-sha256
```

```shell
corese-command canonicalize -i myData?.ttl -r rdfc-1.0-sha256
```

- **Directory Input:** If you want to use a whole directory as input, you can do so.

```shell
corese-command canonicalize -i ./myDirectory/ -r rdfc-1.0-sha256
```

- **Directory Input Recursive:** If you want to use a whole directory as input, you can do so. The `-R` flag allows you to use the directory recursively.

```shell
corese-command canonicalize -i ./myDirectory/ -r rdfc-1.0-sha256 -R
```

> All input files are loaded into the same dataset. Canonicalization algorithms are applied to the entire dataset.

## 7. General Options

General options are available for all commands.

### 7.1. Configuration file

All interface of Corese (Gui, Server, Command) can be configured with a configuration file. The configuration file is a property file (See a example on [GitHub](https://github.com/Wimmics/corese/blob/master/corese-core/src/main/resources/data/corese/property.properties)).

In Corese-Command, the configuration file can be specified with the `-c`, `--config` or `--init` flag:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -c myConfig.properties
```

For exampample, you can disable the auto import of owl with the following property file (`myConfig.properties`):

```properties
DISABLE_OWL_AUTO_IMPORT = true
```

### 7.2. Verbose

The `-v` flag allows you to get more information about the execution of the command.

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -v
```

### 7.3. Version

The `-V` flag allows you to get the version of the command.

### 7.4. Get Help

For any command, you can use the `-h` or `--help` flag to get a description and the syntax. This is also available for the general `corese-command` and each specific sub-command.

```shell
corese-command -h
corese-command sparql -h
corese-command convert -h
corese-command shacl -h
```

### 7.5. Disabling OWL Auto Import

Corese-Command is configured to automatically import the vocabulary referenced in `owl:imports` statements by default. However, this behavior can be turned off by using the `-w` or `--no-owl-import` flag.

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -w
```

```shell
corese-command convert -i myData.ttl -r jsonld -w
```
  
```shell
corese-command shacl -i myData.ttl -s myShapes.ttl -w
```
