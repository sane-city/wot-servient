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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Cli {
    static final Logger log = LoggerFactory.getLogger(Cli.class);

    private final String CONF = "wot-servient.conf";
    private final String LOGLEVEL = "warn";

    public Cli(String[] args) throws ParseException {
        ScriptingManager.addEngine(new GroovyEngine());

        Options options = getOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("loglevel")) {
            setLogLevel(cmd.getOptionValue("loglevel"));
        }
        else {
            setLogLevel(LOGLEVEL);
        }

        if (cmd.hasOption("help")) {
            printHelp(options);
        }
        else if (cmd.hasOption("version")) {
            printVersion();
        }
        else {
            runScripts(cmd);
        }
    }

    private void setLogLevel(String value) {
        Level level = Level.valueOf(value);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLoggerList().forEach(f -> f.setLevel(level));
    }

    private void printVersion() {
        // FIXME: read correct version from pom.xml
        String version = "1.0-SNAPSHOT";
        System.out.println(version);
    }

    private void runScripts(CommandLine cmd) {
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

        Servient servient = getServient(cmd);
        servient.start().join();
        Wot wot = new DefaultWot(servient);

        for (File script : scripts) {
            log.info("Servient is running script '{}'", script);
            try {
                servient.runScript(script, wot);
            }
            catch (ServientException e) {
                log.error("Servient experienced error while reading script", e);
            }
        }
    }

    private Servient getServient(CommandLine cmd) {
        Config config;
        if (!cmd.hasOption("f")) {
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
            File file = new File(cmd.getOptionValue("f"));
            config = ConfigFactory.parseFile(file).withFallback(ConfigFactory.load());
            log.info("Servient is using configuration file '{}'", file);
        }

        Servient servient;
        if (!cmd.hasOption("c")) {
            servient = new Servient(config);
        }
        else {
            servient = Servient.clientOnly(config);
        }
        return servient;
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

    private Options getOptions() {
        Options options = new Options();
        Option.builder().argName("v").longOpt("version").desc("display version");

        Option version = Option.builder("v").longOpt("version").desc("display version").build();
        options.addOption(version);

        Option loglevel = Option.builder("l").longOpt("loglevel").hasArg().argName("level").desc("sets the log level (off, error, warn, info, debug, trace; default: warn)").build();
        options.addOption(loglevel);

        Option clientonly = Option.builder("c").longOpt("clientonly").desc("do not start any servers").build();
        options.addOption(clientonly);

        Option configfile = Option.builder("f").longOpt("configfile").hasArg().argName("file").desc("load configuration from specified file").build();
        options.addOption(configfile);

        Option help = Option.builder("h").longOpt("help").desc("show this file").build();
        options.addOption(help);

        return options;
    }

    public static void main(String[] args) throws ParseException {
        new Cli(args);
    }
}