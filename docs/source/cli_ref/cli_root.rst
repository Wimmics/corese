.. _cli-reference:

Command Line Interface Reference
=======================================

This document is a reference guide for commands available through the Corese CLI a.k.a. `Corese-Command`. `Corese-Command` provides the built-in Corese engine and a set of commands to interact with it. `Corese-Command` is designed to simplify and streamline tasks related to querying, converting, and validating RDF data, 

For the User Guide on how to set up and get started with `Corese-Command`, please refer to its `User Guide <../getting%20started/Getting%20Started%20With%20Corese-command.html>`_.

.. note::
    The installation instructions for non-Java users are not very clear.  


The general syntax for `Corese-Command` is as follows:

.. code-block:: bash

    corese-command [GLOBAL_OPTIONS] [COMMAND] [COMMAND_OPTIONS [ARGUMENT] ...] 

**Global Options**

Global options can be used without specifying a command and include:

- `-h`, `\-\-help` : Display a list of available commands.
- `-V`, `\-\-version` : Display version information.

.. code-block:: bash

    corese-command -V
    corese-command --help

.. note::
    The `--help` option can be used with any command to display detailed information about the command.

    .. code-block:: bash

        corese-command sparql --help

Commands
--------

.. list-table::
   :header-rows: 1

   * - Command
     - Summary
   * - :ref:`sparql <corese-command-sparql>`
     - Run a SPARQL query on RDF datasets.
   * - :ref:`convert <corese-command-convert>`
     - Convert RDF dataset from one format to another.
   * - :ref:`shacl <corese-command-shacl>`
     - Validate RDF dataset against SHACL shapes.
   * - :ref:`remote-sparql <corese-command-remote-sparql>`
     - Execute a SPARQL query on a remote endpoint.
   * - :ref:`remote-sparql <corese-command-canonicalize>`
     - Applying a specific canonicalization algorithm to RDF dataset.
     
.. toctree::
   :maxdepth: 1
   :hidden:

   cli_sparql
   cli_convert
   cli_shacl
   cli_remote_sparql
   cli_canonicalize


