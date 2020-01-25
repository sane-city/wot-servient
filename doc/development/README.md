# Development

## Release new Version

```bash
mvn clean -DskipTests -Darguments=-DskipTests release:prepare
```

**An additional call of `mvn release:perform` is not necessary!**

## Build dist

```bash
mvn -DskipTests -pl wot-servient-cli -am package
mkdir -p dist/wot-servient
cp -R wot-servient-cli/bin wot-servient-cli/target/lib wot-servient-cli/target/wot-servient-cli.jar dist/wot-servient
```

## Build and Push Docker Image

```bash
docker build -t git.informatik.uni-hamburg.de:4567/sane-public/wot-servient:latest .
docker push git.informatik.uni-hamburg.de:4567/sane-public/wot-servient:latest
```