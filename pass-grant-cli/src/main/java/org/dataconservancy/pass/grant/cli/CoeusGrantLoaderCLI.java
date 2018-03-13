/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.pass.grant.cli;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

public class CoeusGrantLoaderCLI {

    /**
     * Arguments - just the property file containing the submission elements
     */
    @Argument(required = true, index = 0, metaVar = "[Home Directory]", usage = "Absolute path of the directory which is " +
            "readable and writable by the user executing this application, " +
            "containing the encrypted properties file for the COEUS connection (\"connection.properties\") and " +
            "the plain text smtp properties (\"mail.properties\"), " +
            "and where the log file and the update_timestamps files will be written.")

    public static File coeusLoaderHome = null;

    /**
     *
     * General Options
     */

    /** Request for help/usage documentation */
    @Option(name = "-h", aliases = { "-help", "--help" }, usage = "print help message")
    public boolean help = false;

    /** Requests the current version number of the cli application. */
    @Option(name = "-v", aliases = { "-version", "--version" }, usage = "print version information")
    public boolean version = false;


    public static void main(String[] args) {

        final CoeusGrantLoaderCLI application = new CoeusGrantLoaderCLI();
        CmdLineParser parser = new CmdLineParser(application);

        String emailMessageBody = "";

        try {
            parser.parseArgument(args);
            /* Handle general options such as help, version */
            if (application.help) {
                parser.printUsage(System.err);
                System.err.println();
                System.exit(0);
            } else if (application.version) {
                System.err.println(CoeusCliException.class.getPackage()
                        .getImplementationVersion());
                System.exit(0);
            }

            /* Run the package generation application proper */
            CoeusGrantLoaderApp app = new CoeusGrantLoaderApp(coeusLoaderHome);
            app.run();
            System.exit((0));
        } catch (CmdLineException e) {
            /**
             * This is an error in command line args, just print out usage data
             * and description of the error.
             */
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        } catch (CoeusCliException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
