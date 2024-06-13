# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line, and also
# from the environment for the first two.
SPHINXOPTS    ?=
SPHINXBUILD   ?= sphinx-build
SOURCEDIR     = source
BUILDDIR      = build

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile 

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile symbolic_links
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

# As a prerequisite create symbolic links to the subdirectories 
# of /docs in the /docs/source directory. This is done to allow
# the Makefile to include all the MARKDOWN files in the /docs 
# directory in the documentation rendered as HTML by Sphinx.
symbolic_links:
	cd $(SOURCEDIR); \
	echo "Creating a symbolic link to README.md"; \
	if [ ! -e "user_guide.md" ]; then \
		ln -sfv "../README.md" "user_guide.md"; \
	else echo "--- user_guide.md already exists"; \
	fi; \
	\
	echo "Creating a symbolic link to sub-dirs"; \
	for dir in "getting started" "rdf4j" "corese-python" "federation" "storage" "advanced"; do \
	if [ ! -d "$$dir" ]; then \
		ln -sfv "../$$dir" "$$dir"; \
		echo "--- $$dir created"; \
	else echo "--- $$dir already exists"; \
	fi; \
	done; \
	\
	echo "Creating a symbolic link to docker-build"; \
	if [ ! -d "docker" ]; then \
		ln -sfv "../../corese-server/build-docker" "docker"; \
		echo "--- docker created"; \
	else echo "--- docker already exists"; \
	fi; \
	cd -
