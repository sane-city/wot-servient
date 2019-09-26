## Usage

### Builds

The current version of the CLI can be downloaded from the following address: https://git.informatik.uni-hamburg.de/sane/wot-servient/-/jobs/artifacts/master/download?job=build-dist

Show help:
```bash
bin/wot-servient -h
```

Execute all WoT scripts from the current directory
```bash
bin/wot-servient
```

##  Docker

Show help:
```bash
docker run --rm -ti git.informatik.uni-hamburg.de:4567/sane/wot-servient:latest -h
```

Execute all WoT scripts from the directory `./wot-scripts`
```bash
docker run --rm -ti -v ./wot-scripts:/wot-servient/ git.informatik.uni-hamburg.de:4567/sane/wot-servient:latest
```


## Usage

TODO