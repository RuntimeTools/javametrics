package com.ibm.javametrics.codewind;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

public class WebPage extends HttpServlet {
	private static final long serialVersionUID = -4751096228274971485L;
	private DataHandler dataHandler = DataHandler.getInstance();
	@Override
	protected void doGet(HttpServletRequest reqest, HttpServletResponse response) 
			throws ServletException, IOException {
		// prevent caching so that new data is fetched every refresh
		response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("text/plain");
		PrintWriter write = response.getWriter();
		write.println("# HELP os_cpu_used_ratio The ratio of the systems CPU that is currently used (values are 0-1)\n");
    	write.println("# TYPE os_cpu_used_ratio gauge\n");
    	write.println("os_cpu_used_ratio " + dataHandler.getLatestCPUEventSystem() + "\n");
		write.println("# HELP process_cpu_used_ratio The ratio of the systems CPU that is currently being used by the monitored process (values are 0-1)\n");
		write.println("# TYPE process_cpu_used_ratio gauge\n");
		write.println("process_cpu_used_ratio " + dataHandler.getLatestCPUEventProcess() + "\n");
		write.println("# HELP os_cpu_used_ratio_average The average ratio of the systems CPU used since monitoring began (values are 0-1)\n");
    	write.println("# TYPE os_cpu_used_ratio_average gauge\n");
    	write.println("os_cpu_used_ratio_average " + dataHandler.getLatestCPUEventSystemMean() + "\n");
		write.println("# HELP process_cpu_used_ratio_average The average ratio of the systems CPU used by the monitored process since monitoring began (values are 0-1)\n");
		write.println("# TYPE process_cpu_used_ratio_average gauge\n");
		write.println("process_cpu_used_ratio_average " + dataHandler.getLatestCPUEventProcessMean() + "\n");
		write.println("# HELP process_native_memory_used_bytes The amount of native memory the monitored process is currently using (in bytes)\n");
    	write.println("# TYPE process_native_memory_used_bytes gauge\n");
    	write.println("process_native_memory_used_bytes " + dataHandler.getLatestMemEventUsedNative() + "\n");
		write.println("# HELP process_native_memory_used_max_bytes The greatest amount of native memory used by the monitored process since monitoring began (in bytes)\n");
		write.println("# TYPE process_native_memory_used_max_bytes gauge\n");
		write.println("process_native_memory_used_max_bytes " + dataHandler.getLatestMemEventUsedNativeMax() + "\n");
		write.println("# HELP process_heap_memory_used_bytes The amount of memory the monitored process's heap is currently using (in bytes)\n");
    	write.println("# TYPE process_heap_memory_used_bytes gauge\n");
    	write.println("process_heap_memory_used_bytes " + dataHandler.getLatestMemEventUsedHeap() + "\n");
		write.println("# HELP process_heap_memory_used_after_GC_bytes The amount of memory the monitored process's heap was using after the last garbage collection (in bytes)\n");
		write.println("# TYPE process_heap_memory_used_after_GC_bytes gauge\n");
		write.println("process_heap_memory_used_after_GC_bytes " + dataHandler.getLatestMemEventUsedHeapAfterGC() + "\n");
		write.println("# HELP process_heap_memory_used_after_GC_max_bytes The maximum amount of memory the monitored process's heap was using after a garbage collection since monitoring began (in bytes)\n");
		write.println("# TYPE process_heap_memory_used_after_GC_max_bytes gauge\n");
		write.println("process_heap_memory_used_after_GC_max_bytes " + dataHandler.getLatestMemEventUsedHeapAfterGCMax() + "\n");
		write.println("# HELP environment_variable a key-value pair stored in the property of the metric. Counter is always 1.\n");
		write.println("# TYPE environment_variable counter\n");
		// output Environment variables
		for (Map.Entry<String, String> entry : dataHandler.getLatestEnvMap().entrySet()) {
			write.println("environment_variable{" + entry.getKey().replaceAll(" ", "") + "=\"" + entry.getValue() + "\"} 1\n");
		}
		// output gc data
		write.println("# HELP overall_time_in_gc_percentage The overall amount of time spent performing garbage collection as a percentage of the overall runtime of the application \n");
		write.println("# TYPE overall_time_in_gc_percentage gauge\n");
		write.println("overall_time_in_gc_percentage " + dataHandler.getLatestGCEventGCTime() + "\n");
		// output http data
		synchronized (dataHandler.getAggregateHttpData()) {
			write.println("# HELP http_requests_total Total number of HTTP requests received in this snapshot.\n");
			write.println("# TYPE http_requests_total gauge\n");
			write.println("http_requests_total " + dataHandler.getAggregateHttpData().getTotalHits() + "\n");
			write.println("# HELP http_requests_duration_average_microseconds Average duration of HTTP requests received in this snapshot.\n");
			write.println("# TYPE http_requests_duration_average_microseconds gauge\n");
			write.println("http_requests_duration_average_microseconds " + dataHandler.getAggregateHttpData().getAverage() + "\n");
			write.println("# HELP http_requests_duration_max_microseconds Longest HTTP request received in this snapshot.\n");
			write.println("# TYPE http_requests_duration_max_microseconds gauge\n");
			write.println("http_requests_duration_max_microseconds{handler=\"" + dataHandler.getAggregateHttpData().getLongestUrl() + "\",method=\"" + dataHandler.getAggregateHttpData().getLongestMethod() + "\"} " + dataHandler.getAggregateHttpData().getLongest() + "\n");

		// Loop through httpURLDataList several times for different metrics
			write.println("# HELP http_request_duration_max_microseconds The maximum HTTP request latencies in microseconds.\n");
			write.println("# TYPE http_request_duration_max_microseconds gauge\n");
			Iterator<HttpUrlData> it = dataHandler.getAggregateHttpData().getUrlData().iterator();
			while (it.hasNext()) {
				HttpUrlData hud = it.next();
				write.println("http_request_duration_max_microseconds{handler=\"" + hud.getUrl() + "\",method=\"" + hud.getMethod() + "\"} " + hud.getLongestResponseTime() + "\n");
			}

			write.println("# HELP http_request_duration_avg_microseconds The average HTTP request latencies in microseconds.\n");
			write.println("# TYPE http_request_duration_avg_microseconds gauge\n");
			it = dataHandler.getAggregateHttpData().getUrlData().iterator();
			while (it.hasNext()) {
				HttpUrlData hud = it.next();
				write.println("http_request_duration_avg_microseconds{handler=\"" + hud.getUrl() + "\",method=\"" + hud.getMethod() + "\"} " + hud.getAverageResponseTime() + "\n");
			}

			write.println("# HELP http_request_total Total number of HTTP requests made for that URL.\n");
			write.println("# TYPE http_request_total counter\n");
			it = dataHandler.getAggregateHttpData().getUrlData().iterator();
			while (it.hasNext()) {
				HttpUrlData hud = it.next();
				write.println("http_request_total{handler=\"" + hud.getUrl() + "\",method=\"" + hud.getMethod() + "\"} " + hud.getHits() + "\n");
			}

			dataHandler.getAggregateHttpData().resetSummaryData();
		}
	}
	@Override
	public void init() throws ServletException {
		System.out.println("Servlet " + this.getServletName() + " has started");

	}
	@Override
	public void destroy() {
		System.out.println("Servlet " + this.getServletName() + " has stopped");
	}
}