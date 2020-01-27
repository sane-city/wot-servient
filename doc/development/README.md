# Development

## Release new Version

```bash
mvn clean -DskipTests -Darguments=-DskipTests release:prepare
```

**An additional call of `mvn release:perform` is not necessary!**

### Add Asset to Release

```bash
curl --request POST \
     --header "PRIVATE-TOKEN: s3cr3tPassw0rd" \
     --data name="wot-servient-1.1.zip" \
     --data url="https://git.informatik.uni-hamburg.de/sane-public/wot-servient/-/jobs/102695/artifacts/download" \
     "https://git.informatik.uni-hamburg.de/api/v4/projects/2707/releases/1.1/assets/links"
```

## Build dist

```bash
mvn -DskipTests -pl wot-servient-cli -am package
# wot-servient-cli/target/wot-servient-*.zip generated
```

## Build and Push Docker Image

```bash
docker build -t git.informatik.uni-hamburg.de:4567/sane-public/wot-servient:latest .
docker push git.informatik.uni-hamburg.de:4567/sane-public/wot-servient:latest
```