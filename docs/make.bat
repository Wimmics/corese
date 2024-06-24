@ECHO OFF

pushd %~dp0

REM Command file for Sphinx documentation

if "%SPHINXBUILD%" == "" (
	set SPHINXBUILD=sphinx-build
)
set SOURCEDIR=source
set BUILDDIR=build

%SPHINXBUILD% >NUL 2>NUL
if errorlevel 9009 (
	echo.
	echo.The 'sphinx-build' command was not found. Make sure you have Sphinx
	echo.installed, then set the SPHINXBUILD environment variable to point
	echo.to the full path of the 'sphinx-build' executable. Alternatively you
	echo.may add the Sphinx directory to PATH.
	echo.
	echo.If you don't have Sphinx installed, grab it from
	echo.https://www.sphinx-doc.org/
	exit /b 1
)

if "%1" == "" goto help

if "%1" == "link" goto link 

%SPHINXBUILD% -M %1 %SOURCEDIR% %BUILDDIR% %SPHINXOPTS% %O%
goto end

:help
%SPHINXBUILD% -M help %SOURCEDIR% %BUILDDIR% %SPHINXOPTS% %O%

REM Link the README.md file to user_guide.md
:link
pushd %SOURCEDIR%

if not exist "user_guide.md" (
	mklink "user_guide.md" "..\README.md"
)

REM Link the docs/source/sub-directories to source directories to the docs/sub-directories
REM This is necessary to accomodate the sphinx build system

set "dirs=getting started;rdf4j;corese-python;federation;storage;advanced"

echo Linking docs directories to source directories

for %%i in ("%dirs:;=";"%") do (
	echo %%~i

	if not exist "%%~i" (
		mklink /D "%%~i" "..\%%~i"
	)
)

REM the markdown file for docker is now outside of the docs directory so we need to link it
REM Link the corese-server/build-docker directory to the docker directory
REM TODO: consider moving the docker directory to the docs directory

echo docker
if not exist "docker" (
	mklink /D "docker" "..\..\corese-server\build-docker"
)

popd
goto end

:end
popd
