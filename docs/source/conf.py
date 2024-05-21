# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here.
import pathlib
import sys
import os

sys.path.insert(0, pathlib.Path(__file__).parents[1].resolve().as_posix())
sys.path.insert(0, pathlib.Path(__file__).parents[2].resolve().as_posix())
#sys.path.insert(0, pathlib.Path(__file__).parents[2].joinpath('code').resolve().as_posix())


project = 'CORESE'
copyright = '2024, WIMMICS'
author = 'WIMMICS'
release = '4.5'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = [
    'sphinx.ext.duration', # to display the duration of Sphinx processing
    # Uncomment the following lines if/when include the python code (not used in this project yet)
    #'sphinx.ext.doctest', # to test code snippets in the documentation
    #'sphinx.ext.autodoc', # to automatically generate documentation from docstrings
    #'sphinx.ext.autosummary', # this extension generates function/method/attribute summary lists
    #'sphinx.ext.autosectionlabel', # to automatically generate section labels
    'sphinx_design', # to render panels
    'myst_parser', # to parse markdown
    'sphinxcontrib.mermaid', # to render mermaid diagrams
    # Alternative ways to include markdown files, cannot be used together with myst_parser
    # advantages of sphynx_mdinclude/m2r3: it can include partial markdown files
    # 
    #'sphinx_mdinclude', # to include partial markdown files
    #'m2r3', # to include markdown files
    'sphinx_copybutton', # to add copy buttons to code blocks
    'breathe', # to include doxygen generated documentation for java code
    'exhale' # to process doxygen xml files
    ]

templates_path = ['_templates']
exclude_patterns = []

# The suffix(es) of source filenames.
source_suffix = ['.rst', '.md']

# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'pydata_sphinx_theme'
html_static_path = ['_static']

html_css_files = [
    "css/custom.css",
]
html_js_files = []

# Project logo, to place at the top of the sidebar.
html_logo = "_static/corese.svg"

# Icon to put in the browser tab.
html_favicon = "_static/corese.svg"

# Modify the title to get good social-media links
html_title = "CORESE"
html_short_title = "CORESE"

# -- Theme Options -----------------------------------------------------------
# Theme options are theme-specific and customize the look and feel of a theme
# further.  For a list of options available for each theme, see the
# documentation.
html_theme_options = {
     "logo": {
         "image_relative": "_static/corese.svg",
         "image_light": "_static/corese.svg",
         "image_dark": "_static/corese.svg",
     },
    "navbar_center": ["navbar-nav"],
    "icon_links": [
        {
            "name": "GitHub",
            "url": "https://github.com/Wimmics/corese",
            "icon": "fab fa-github-square",
        }
    ],
    #"navigation_depth": 0,
    "show_toc_level": 3
 }

# since the markdown files dont have TOC we can hide the Section Navigation bar (left)
html_sidebars = {
  "user_guide": [],
  "install": [],
}

# -- MySt-parcer extension Options -------------------------------------------
# https://myst-parser.readthedocs.io/en/latest/

myst_heading_anchors = 4
myst_fence_as_directive = ["mermaid"]

# -- Doxygen/breath/exhale extensions Options --------------------------------
# Setup absolute paths for communicating with breathe / exhale where
# items are expected / should be trimmed by.
# https://breathe.readthedocs.io/en/latest/quickstart.html
# https://exhale.readthedocs.io/en/latest/usage.html

this_file_dir = os.path.abspath(os.path.dirname(__file__))
repo_root = os.path.dirname(  # {repo_root}
            os.path.dirname(  # {repo_root}/docs
            this_file_dir     # {repo_root}/docs/source
        )
    ) # TODOD: delete in a final version if not used

# Setup the breathe extension 
# https://breathe.readthedocs.io/en/latest/
breathe_projects = {
    "corese": "../build/doxygen_xml"
}

breathe_default_project = "corese"

# Setup the exhale extension
exhale_args = {
    # These arguments are required
    "containmentFolder":     "./java_api",
    "rootFileName":          "library_root.rst",
    "doxygenStripFromPath":  repo_root, # "..",

    # Heavily encouraged optional argument (see docs)
    "rootFileTitle":         "Java API",

    # Suggested optional arguments
    "createTreeView":        True,
    # TIP: if using the sphinx-bootstrap-theme, you need
    # "treeViewIsBootstrap": True,
    "exhaleExecutesDoxygen": True,
    # all Doxygen configuration will be done in the Doxyfile
    "exhaleUseDoxyfile": True, 

    "verboseBuild": False,

    # Exclude the file view from the root page
    #"unabridgedOrphanKinds": ["file"],
}


# Tell sphinx what the primary language being documented is.
# Java is not one of the available options. Keep it as cpp.
primary_domain = 'cpp'

# Tell sphinx what the pygments highlight language should be.
# Java is not one of the available options. Keep it as cpp.
highlight_language = 'cpp'