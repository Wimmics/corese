# Getting Started With Corese-Command

Corese-Command is a command-line interface (CLI) for the Corese Semantic Web Factory. It allows you to run SPARQL queries on RDF datasets and convert RDF files between various serialization formats directly from the command line. This makes it a powerful tool for automated processing, quick console-based testing, and integration into larger scripts or applications.

Corese-Command is designed to simplify and streamline tasks related to querying and converting RDF data. It is suitable for developers, data scientists, and anyone working with Semantic Web technologies.

## 1. Table of Contents

1. [Getting Started With Corese-Command](#getting-started-with-corese-command)
   1. [1. Table of Contents](#1-table-of-contents)
   2. [2. Installation](#2-installation)
   3. [3. The `sparql` Command](#3-the-sparql-command)
      1. [3.1. Basic Usage](#31-basic-usage)
      2. [3.2. Choose the Result Format](#32-choose-the-result-format)
      3. [3.3. Different Types of Input](#33-different-types-of-input)
      4. [3.4. Different Types of Queries](#34-different-types-of-queries)
      5. [3.5. Multiple Input Files](#35-multiple-input-files)
      6. [3.6. Different Types of Output](#36-different-types-of-output)
   4. [4. The `convert` Command](#4-the-convert-command)
      1. [4.1. Basic Usage](#41-basic-usage)
      2. [4.2. Different Types of Input](#42-different-types-of-input)
      3. [4.3. Different Types of Output](#43-different-types-of-output)
      4. [4.4. Resume of Available Formats](#44-resume-of-available-formats)
   5. [5. Get Help](#5-get-help)

## 2. Installation

Installations instructions are available on the [Corese-Command GitHub repository](https://github.com/Wimmics/corese).

## 3. The `sparql` Command

The `sparql` command allows you to run SPARQL queries on RDF datasets.

### 3.1. Basic Usage

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

### 3.2. Choose the Result Format

Let's try the same query as before, but this time with the `json` format:

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

The result format can be specified with the `-r` flag. The following formats are available:

- RDF/XML: `rdfxml` or `application/rdf+xml`
- Turtle: `turtle` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- XML: `xml` or `application/sparql-results+xml`
- Json: `json` or `application/sparql-results+json`
- CSV: `csv` or `text/csv`
- TSV: `tsv` or `text/tab-separated-values`
- Markdown: `markdown` or `md` or `text/markdown`

Here is a table of available formats according to the type of request:

| Format   | select | ask | insert | insert-where | delete | delete-where | describe | construct |
| -------- | ------ | --- | ------ | ------------ | ------ | ------------ | -------- | --------- |
| rdfxml   | ❌     | ❌  | ✅     | ✅           | ✅     | ✅           | ✅       | ✅        |
| turtle   | ❌     | ❌  | ✅     | ✅           | ✅     | ✅           | ✅       | ✅        |
| trig     | ❌     | ❌  | ✅     | ✅           | ✅     | ✅           | ✅       | ✅        |
| jsonld   | ❌     | ❌  | ✅     | ✅           | ✅     | ✅           | ✅       | ✅        |
| xml      | ✅     | ✅  | ❌     | ❌           | ❌     | ❌           | ❌       | ❌        |
| json     | ✅     | ✅  | ❌     | ❌           | ❌     | ❌           | ❌       | ❌        |
| csv      | ✅     | ✅  | ❌     | ❌           | ❌     | ❌           | ❌       | ❌        |
| tsv      | ✅     | ✅  | ❌     | ❌           | ❌     | ❌           | ❌       | ❌        |
| markdown | ✅     | ✅  | ❌     | ❌           | ❌     | ❌           | ❌       | ❌        |

### 3.3. Different Types of Input

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
cat myData.ttl | corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}'
```

> The input file format is automatically detected for file and URL inputs. If the input is provided on the standard input or you want to force the input format, you can use the `-f` flag. Possible values are:
>
> - `rdfxml`, `application/rdf+xml`
> - `turtle`, `text/turtle`
> - `trig`, `application/trig`
> - `jsonld`, `application/ld+json`
> - `n3`, `text/n3`
> - `ntriples`, `application/n-triples`
> - `nquads`, `application/n-quads`
> - `rdfa`, `application/xhtml+xml`

### 3.4. Different Types of Queries

The query can be provided in different ways:

- **Query String:** The query can be provided directly on the command line with the `-q` flag:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl
```

- **File Query:** The query can be provided in a file with the `-q` flag:

```shell
corese-command sparql -q myQuery.rq -i myData.ttl
```

### 3.5. Multiple Input Files

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

### 3.6. Different Types of Output

If you want to save the result to a file, you can do so with the `-o` flag:

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -r json -o myResult.json
```

If no `-o` flag is provided, the result is printed to the standard output.

```shell
corese-command sparql -q 'SELECT * WHERE {?s ?p ?o}' -i myData.ttl -r json | jq […] 
```

## 4. The `convert` Command

The `convert` command allows you to convert an RDF file from one serialization format to another.

### 4.1. Basic Usage

```shell
corese-command convert -i myFile.ttl -r jsonld
```

This example converts `myFile.ttl` from `turtle` to `jsonld`. The `-i` flag specifies the input file, and the `-r` flag specifies the output format.

### 4.2. Different Types of Input

The input can be provided in different ways:

- **File Input:** The input file can be specified with the `-i` flag:

```shell
corese-command convert -i myData.ttl -r jsonld
```

- **URL Input:** URLs can be specified with the `-i` flag:

```shell
corese-command convert -i 'http://example.org/myData.ttl' -r jsonld
```

- **Standard Input:** If no input file is specified with `-i`, the program uses the standard input:

```shell
cat myData.ttl | corese-command convert -r turtle
```

> The input file format is automatically detected for file and URL inputs. If the input is provided on the standard input or you want to force the input format, you can use the `-f` flag. Possible values are:
>
> - `rdfxml`, `application/rdf+xml`
> - `turtle`, `text/turtle`
> - `trig`, `application/trig`
> - `jsonld`, `application/ld+json`
> - `n3`, `text/n3`
> - `ntriples`, `application/n-triples`
> - `nquads`, `application/n-quads`
> - `rdfa`, `application/xhtml+xml`

### 4.3. Different Types of Output

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
> - `rdfxml`, `application/rdf+xml`
> - `turtle`, `text/turtle`
> - `trig`, `application/trig`
> - `jsonld`, `application/ld+json`

### 4.4. Resume of Available Formats

The `convert` command supports the following formats for input and output:

| Format   | Input Support | Output Support |
| -------- | ------------- | -------------- |
| RDFXML   | ✅             | ✅            |
| Turtle   | ✅             | ✅            |
| JSONLD   | ✅             | ✅            |
| TriG     | ✅             | ✅            |
| N3       | ✅             | ❌            |
| NTRIPLES | ✅             | ❌            |
| NQUADS   | ✅             | ❌            |
| RDFA     | ✅             | ❌            |

## 5. Get Help

For any command, you can use the `-h` or `--help` flag to get a description and the syntax. This is also available for the general `corese-command` and each specific sub-command.

```shell
corese-command -h
corese-command sparql -h
corese-command convert -h
```
