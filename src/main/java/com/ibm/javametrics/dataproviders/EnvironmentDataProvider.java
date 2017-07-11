package com.ibm.javametrics.dataproviders;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class EnvironmentDataProvider {

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // Just return unknown host.
        }
        return "unknown host";
    }

    public static String getArchitecture() {
        return ManagementFactory.getOperatingSystemMXBean().getArch();
    }

    public static int getCPUCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static String getCommandLine() {
        StringBuffer args = new StringBuffer();
        args.append("java ");
        ManagementFactory.getRuntimeMXBean().getInputArguments().forEach(args::append);
        args.append(System.getProperty("sun.java.command"));
        return args.toString();
    }

}
