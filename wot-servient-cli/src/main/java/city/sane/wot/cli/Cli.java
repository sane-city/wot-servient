package city.sane.wot.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import city.sane.wot.DefaultWot;
import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.Wot;
import city.sane.wot.scripting.GroovyEngine;
import city.sane.wot.scripting.ScriptingManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class Cli {
    private static final Logger log = LoggerFactory.getLogger(Cli.class);
    private static final String CONF = "wot-servient.conf";
    private static final String LOGLEVEL = "info";
    private static final String OPT_VERSION = "version";
    private static final String OPT_LOGLEVEL = "loglevel";
    private static final String OPT_CLIENTONLY = "clientonly";
    private static final String OPT_CONFIGFILE = "configfile";
    private static final String OPT_HELP = "help";

    public Cli(String[] args) throws CliException {
        ScriptingManager.addEngine(new GroovyEngine());

        Options options = getOptions();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(OPT_LOGLEVEL)) {
                setLogLevel(cmd.getOptionValue(OPT_LOGLEVEL));
            }
            else {
                setLogLevel(LOGLEVEL);
            }

            if (cmd.hasOption(OPT_HELP)) {
                printHelp(options);
            }
            else if (cmd.hasOption(OPT_VERSION)) {
                printVersion();
            }
            else {
                runScripts(cmd);
            }
        }
        catch (ServientException | ParseException e) {
            throw new CliException(e);
        }
    }

    private Options getOptions() {
        Options options = new Options();

        Option version = Option.builder("v").longOpt(OPT_VERSION).desc("display version").build();
        options.addOption(version);

        Option loglevel = Option.builder("l").longOpt(OPT_LOGLEVEL).hasArg().argName("level").desc("sets the log level (off, error, warn, info, debug, trace; default: " + LOGLEVEL + ")").build();
        options.addOption(loglevel);

        Option clientonly = Option.builder("c").longOpt(OPT_CLIENTONLY).desc("do not start any servers").build();
        options.addOption(clientonly);

        Option configfile = Option.builder("f").longOpt(OPT_CONFIGFILE).hasArg().argName("file").desc("load configuration from specified file").build();
        options.addOption(configfile);

        Option help = Option.builder("h").longOpt(OPT_HELP).desc("show this file").build();
        options.addOption(help);

        return options;
    }

    private void setLogLevel(String value) {
        Level level = Level.valueOf(value);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLoggerList().stream().filter(l -> l.getName().startsWith("city.sane")).forEach(l -> l.setLevel(level));
    }

    private void printHelp(Options options) {
        String header = "" +
                "       wot-servient\n" +
                "       wot-servient examples/scripts/counter.groovy examples/scripts/example-event.groovy\n" +
                "       wot-servient -c counter-client.groovy\n" +
                "       wot-servient -f ~/mywot.conf examples/testthing/testthing.groovy\n" +
                "\n" +
                "Run a WoT Servient in the current directory.\n" +
                "If no SCRIPT is given, all .groovy files in the current directory are loaded.\n" +
                "If one or more SCRIPT is given, these files are loaded instead of the directory.\n" +
                "If the file '" + CONF + "' exists, that configuration is applied.\n" +
                "The WoT Servient can be accessed using the \"wot\" variable inside scripts.\n" +
                "\n" +
                "Options:";

        String footer = "\n" +
                CONF + " syntax:\n" +
                "wot {\n" +
                "  servient {\n" +
                "    http {\n" +
                "      bind-host = \"0.0.0.0\"\n" +
                "      bind-port = 8080\n" +
                "    }\n" +
                "\n" +
                "    coap {\n" +
                "      bind-port = 5683\n" +
                "    }\n" +
                "\n" +
                "    mqtt {\n" +
                "      broker = \"tcp://iot.eclipse.org\"\n" +
                "      # username = \"myusername\"\n" +
                "      # password = \"mysecretpassword\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.setSyntaxPrefix("Usage: ");
        formatter.printHelp("wot-servient [options] [SCRIPT]...", header, options, footer);
    }

    private void printVersion() {
        String version = Servient.getVersion();
        System.out.println(version);
    }

    private void runScripts(CommandLine cmd) throws ServientException {
        List<File> scripts = cmd.getArgList().stream().map(File::new).collect(Collectors.toList());
        if (!scripts.isEmpty()) {
            log.info("Servient is loading {} command line script(s)", scripts.size());
        }
        else {
            File currentWorkingDirectory = new File("").getAbsoluteFile();
            scripts = Arrays.stream(currentWorkingDirectory.listFiles((dir, name) -> name.toLowerCase()
                    .endsWith(".groovy"))).collect(Collectors.toList());
            log.info("Servient is using current directory with {} script(s)", scripts.size());
        }

        if (scripts.isEmpty()) {
            log.info("No scripts given. Nothing to do!");
            return;
        }

        List<Future> completionFutures = new ArrayList<>();
        final Servient servient = getServient(cmd);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stop all scripts and shutdown Servient");
            servient.shutdown().join();
        }));
        servient.start().join();
        Wot wot = new DefaultWot(servient);

        for (File script : scripts) {
            log.info("Servient is running script '{}'", script);
            Future completionFuture = servient.runScript(script, wot);
            completionFutures.add(completionFuture);
        }

        // wait for all scripts to complete
        completionFutures.forEach(future -> {
            try {
                future.get();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException | CancellationException e) {
                throw new RuntimeException(e);
            }
        });

        // Shutdown servient if we are in client-only mode and all scripts have been executed, otherwise wait for termination by user
        if (cmd.hasOption(OPT_CLIENTONLY)) {
            System.exit(0);
        }
    }

    private Servient getServient(CommandLine cmd) throws ServientException {
        Config config;
        if (!cmd.hasOption(OPT_CONFIGFILE)) {
            File defaultFile = new File(CONF);
            if (defaultFile.exists()) {
                log.info("Servient is using default configuration file '{}'", defaultFile);
                config = ConfigFactory.parseFile(defaultFile).withFallback(ConfigFactory.load());
            }
            else {
                log.info("Servient is using configuration defaults as '{}' does not exist", CONF);
                config = ConfigFactory.load();
            }
        }
        else {
            File file = new File(cmd.getOptionValue(OPT_CONFIGFILE));
            log.info("Servient is using configuration file '{}'", file);
            config = ConfigFactory.parseFile(file).withFallback(ConfigFactory.load());
        }

        Servient servient;
        if (!cmd.hasOption(OPT_CLIENTONLY)) {
            servient = new Servient(config);
        }
        else {
            servient = Servient.clientOnly(config);
        }
        return servient;
    }

    public static void main(String[] args) throws CliException {
        new Cli(args);
    }
}