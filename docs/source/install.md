## Download and install

### Corese-library

- Download from [maven-central](https://central.sonatype.com/namespace/fr.inria.corese)

```xml
<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-core</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- jena storage -->
<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-jena</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- rdf4j storage -->
<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-rdf4j</artifactId>
    <version>4.5.0</version>
</dependency>
```

- Documentation: [Getting Started With Corese-library](/getting%20started/Getting%20Started%20With%20Corese-library.md)

### Corese-server

- Download from [Docker-hub](https://hub.docker.com/r/wimmics/corese)

```sh
docker run --name my-corese \
    -p 8080:8080 \
    -d wimmics/corese
```

- Alternatively, download [Corese-server jar file](https://project.inria.fr/corese/jar/).

```sh
wget "https://github.com/Wimmics/corese/releases/download/release-4.5.0/corese-server-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-server-4.5.0.jar"
```

- Documentation:
  - [Getting Started With Corese-server](/getting%20started/Getting%20Started%20With%20Corese-server.md)
  - [Use Corese-server with Python](/corese-python/Corese-server%20with%20Python.md)

### Corese-GUI

- Download on Flathub
<!-- markdownlint-disable MD033 -->
<a href='https://flathub.org/apps/fr.inria.corese.CoreseGui'>
   <img src='https://dl.flathub.org/assets/badges/flathub-badge-en.png' alt='Download on Flathub' width='175'/>
</a>
<!-- markdownlint-enable MD033 -->

- Or download [Corese-gui jar file](https://project.inria.fr/corese/jar/).

```sh
wget "https://github.com/Wimmics/corese/releases/download/release-4.5.0/corese-gui-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-gui-4.5.0.jar"
```

### Corese-Command

- Download on Flathub
<!-- markdownlint-disable MD033 -->
<a href='https://flathub.org/apps/fr.inria.corese.CoreseCommand'>
   <img src='https://dl.flathub.org/assets/badges/flathub-badge-en.png' alt='Download on Flathub' width='175'/>
</a>
<!-- markdownlint-enable MD033 -->

- Or download [Corese-command jar file](https://project.inria.fr/corese/jar/).

```sh
wget "https://github.com/Wimmics/corese/releases/download/release-4.5.0/corese-command-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-command-4.5.0.jar"
```

- Alternatively, use the installation script for Linux and MacOS systems.

```sh
curl -sSL https://files.inria.fr/corese/distrib/script/install-corese-command.sh | bash
```

To uninstall:

```sh
curl -sSL https://files.inria.fr/corese/distrib/script/uninstall-corese-command.sh | bash
```

> If you're using zsh, replace `bash` with `zsh`.

- Documentation: [Getting Started With Corese-command](/getting%20started/Getting%20Started%20With%20Corese-command.md)

### Corese-Python (beta)

- Download [Corese-python jar file](https://project.inria.fr/corese/jar/).

```sh
wget "https://github.com/Wimmics/corese/releases/download/release-4.5.0/corese-library-python-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-library-python-4.5.0.jar"
```

- Documentation: [Getting Started With Corese-python](/corese-python/Corese-library%20with%20Python.md)

## Compilation from source

Download source code and compile.

```shell
git clone "https://github.com/Wimmics/corese.git"
cd corese
mvn clean install -DskipTests
```
