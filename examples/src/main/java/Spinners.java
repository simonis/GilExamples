/**
 * Written by Gil Tene of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Gil Tene
 */

public class Spinners extends Thread {

    private final SpinnersConfiguration config;

    protected static class SpinnersConfiguration {
        public int concurrencyLevel = 1;
        public long runTimeMs = 0;
        public boolean helpRequested = false;

        public SpinnersConfiguration(final String[] args) {
            try {
                for (int i = 0; i < args.length; ++i) {
                    if (args[i].equals("-c")) {
                        concurrencyLevel = Integer.parseInt(args[++i]);
                    } else if (args[i].equals("-t")) {
                        runTimeMs = Long.parseLong(args[++i]);
                    } else if (args[i].equals("-h")) {
                        helpRequested = true;
                        throw new Exception("[Help]");
                    } else {
                        throw new Exception("Invalid args: " + args[i]);
                    }
                }
            } catch (Exception e) {
                if (helpRequested) {
                    System.err.println("Spinners: A simple spinning-threads CPU-burning utility.");

                } else {
                    String errorMessage = "Error: launched with the following args:\n";

                    for (String arg : args) {
                        errorMessage += arg + " ";
                    }
                    errorMessage += "\nWhich was parsed as an error, indicated by the following exception:\n" + e;
                    System.err.println(errorMessage);
                }

                String validArgs =
                        "\"[-h] [-t runTimeMs] [-c concurrencyLevel]\"\n";

                System.err.println("valid arguments = " + validArgs);

                System.err.println("" +
                        " [-h]                      help\n" +
                        " [-c concurrencyLevel]     Concurrency level (number of spinner threads [default: 1]\n" +
                        " [-t runTimeMs]            Limit run time [default: 0, for infinite]\n" +
                        "\n");

                System.exit(helpRequested ? 0 : 1);
            }
        }
    }

    protected static class SpinnerThread extends Thread {
        static volatile long count = 0;

        SpinnerThread() {
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                count++;
            }
        }
    }

    protected Spinners(final String[] args) {
        config = new SpinnersConfiguration(args);
        this.setDaemon(true);
    }

    @Override
    public void run() {
        long startTimeMsec = System.currentTimeMillis();
        try {
            // Create and start allocators:
            SpinnerThread[] allocators = new SpinnerThread[config.concurrencyLevel];
            for (int i = 0; i < config.concurrencyLevel; i++) {
                allocators[i] = new SpinnerThread();
                allocators[i].start();
            }

            // Start measurement:
            long startTime = System.currentTimeMillis();
            long endTime = (config.runTimeMs == 0) ?
                    Long.MAX_VALUE :
                    startTime + config.runTimeMs;

            while (System.currentTimeMillis() < endTime) {
                Thread.sleep(100);
            }

            // Output something that depends on the spinners count to make it impossible to optimize counts away:
            System.out.println("Done.." + ((SpinnerThread.count % 2 == 1) ? "." : ""));

        } catch (InterruptedException ex) {
        }
    }


    public static Spinners commonMain(final String[] args) {
        Spinners allocationRateExample = new Spinners(args);
        allocationRateExample.start();
        return allocationRateExample;
    }

    public static void premain(String argsString, java.lang.instrument.Instrumentation inst) {
        String[] args = (argsString != null) ? argsString.split("[ ,;]+") : new String[0];
        commonMain(args);
    }

    public static void main(final String[] args) {
        Spinners spinners = commonMain(args);

        if (spinners != null) {
            // The AllocationRateExample thread, on it's own, will not keep the JVM from exiting.
            // If nothing else is running (i.e. we we are the main class), keep main thread from
            // exiting until the AllocationRateExample thread does...
            try {
                spinners.join();
            } catch (InterruptedException e) {
            }
        }
    }
}
