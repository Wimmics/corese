# Corese-server with Python

This document shows how to configure and query a Corese server from python.

## 1. Configure a Corese-Server

1. Download last version of [Corese-Server](https://project.inria.fr/corese/jar/).
2. Start the Corese-Server with command: `java -jar corese-server.jar -l "./file_1.ttl"`

> It's also possible to load data from several files or URL.
>
> E.g: `java -jar corese-server.jav -l "./file_1.ttl" -l "file_2.ttl" -l "http://file_3.ttl"`.

## 2. Query a Corese-server from python

### 2.1. Install dependencies

```shell
pip install --user SPARQLWrapper pandas
```

### 2.2. Execute a update query

```python
from SPARQLWrapper import JSON, POST, POSTDIRECTLY, SPARQLWrapper


def sparql_service_update(service, update_query):
    """
    Helper function to update (DELETE DATA, INSERT DATA, DELETE/INSERT) data.

    """
    sparql = SPARQLWrapper(service)
    sparql.setMethod(POST)
    sparql.setRequestMethod(POSTDIRECTLY)
    sparql.setQuery(update_query)
    sparql.query()

    # SPARQLWrapper is going to throw an exception if result.response.status != 200:

    return 'Done'


query = '''
PREFIX dc: <http://purl.org/dc/elements/1.1/>

INSERT DATA 
{ <http://example/book1>  dc:title  "Oliver Twist"
  <http://example/book2>  dc:title  "David Copperfield"
}
'''

wds_Corese = 'http://localhost:8080/sparql'

df = sparql_service_update(wds_Corese, query)
print(df)
```

Results :

```plaintext
Done
```

### 2.3. Execute a select query

```python
import json

import pandas as pd
from SPARQLWrapper import JSON, SPARQLWrapper


def sparql_service_to_dataframe(service, query):
    """
    Helper function to convert SPARQL results into a Pandas DataFrame.

    Credit to Ted Lawless https://lawlesst.github.io/notebook/sparql-dataframe.html
    """
    sparql = SPARQLWrapper(service)
    sparql.setQuery(query)
    sparql.setReturnFormat(JSON)
    result = sparql.query()

    processed_results = json.load(result.response)
    cols = processed_results['head']['vars']

    out = []
    for row in processed_results['results']['bindings']:
        item = []
        for c in cols:
            item.append(row.get(c, {}).get('value'))
        out.append(item)

    return pd.DataFrame(out, columns=cols)


query = '''
select *
{
  ?s ?p ?o
}
limit 10
'''

wds_Corese = 'http://localhost:8080/sparql'

df = sparql_service_to_dataframe(wds_Corese, query)
print(df.head(10))
```

Results :

```plaintext
                      s                                      p                  o
0  http://example/book1  http://purl.org/dc/elements/1.1/title       Oliver Twist
1  http://example/book2  http://purl.org/dc/elements/1.1/title  David Copperfield
```
