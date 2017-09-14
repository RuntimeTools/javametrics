package com.ibm.javametrics.prometheus;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import io.prometheus.client.exporter.MetricsServlet;

@WebListener
public class MetricsContextListener implements ServletContextListener {

    private Metrics metrics;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        metrics.deRegister();
    }

    /* This should initialise the prometheus metrics when the
     * server starts up instead of the first time /metrics is
     * queried.
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Uncomment the next line to enable the default hotspot events.
        // DefaultExports.initialize();

        metrics = new Metrics();
        ServletContext context = event.getServletContext();
        context.addServlet("Metrics", new MetricsServlet()).addMapping("");
    }

}
