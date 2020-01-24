# Development

## Release new Version

```bash
mvn clean -DskipTests -Darguments=-DskipTests release:prepare
```

**An additional call of `mvn release:perform` is not necessary!**

## Build and Push Docker Image

```bash
docker build -t git.informatik.uni-hamburg.de:4567/sane-public/wot-servient:latest .
docker push git.informatik.uni-hamburg.de:4567/sane-public/wot-servient:latest
```